package blog.yuanyuan.yuanlive.ai.listener;

import blog.yuanyuan.yuanlive.ai.strategy.RiskStrategies;
import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.state.StateSnapshot;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.scheduler.Schedulers;

import java.util.Map;

@Component
@Slf4j
public class ChatDetectListener {
    @Resource(name = "riskWorkflow")
    private CompiledGraph riskWorkflow;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "${live.mq.ai-detect.queue}", durable = "true"),
            exchange = @Exchange(value = "${live.mq.ai-detect.exchange}", type = ExchangeTypes.DIRECT),
            key = "${live.mq.ai-detect.routing-key}" // 必须和发送端的 Routing Key 一致
    ))
    public void onRiskMessage(String messageStr) {
        log.info("收到风控审计任务: {}", messageStr);
        try {
            Map<String, Object> data = JSONUtil.parseObj(messageStr);
            String roomId = (String) data.get("roomId");
            String history = (String) data.get("history");

            // 准备初始状态（对应 RiskStrategies 定义的 Key）
            Map<String, Object> inputData = Map.of(
                    RiskStrategies.ROOM_ID, roomId,
                    RiskStrategies.CHAT_HISTORY, history // 这里传入收割到的整段文本
            );
            // 配置执行上下文
            RunnableConfig config = RunnableConfig.builder()
                    .threadId(roomId)
                    .build();
            log.debug("--- 房间 [{}] 开始执行风控工作流 ---", roomId);
            // 使用 stream 并 blockLast 确保异步节点也能顺序执行完毕，直到遇到 END 或中断点
            riskWorkflow.stream(inputData, config)
                    .doOnNext(output ->
                            log.debug("房间 [{}] 节点 [{}] 执行完毕", roomId, output.node()))
                    .doOnComplete(() -> {
                        StateSnapshot snapshot = riskWorkflow.getState(config);
                        Integer score = (Integer) snapshot.state().data().get(RiskStrategies.RISK_SCORE);
                        log.info("AI 判定分数: {}，当前所处位置: {}", score, snapshot.next());

                        // TODO 先直接中断直播, 后续完善人工介入逻辑
                        if (snapshot.next().contains("admin_review")) {
                            try {
                                adminProcess(config, true, roomId);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            log.info("房间 [{}] 风控流程正常结束,分数 {}, 最终执行决策 {}",
                                    roomId,
                                    score,
                                    snapshot.state().data().get("last_action"));
                            RiskStrategies.cleanRedis(roomId, stringRedisTemplate);
                        }
                    })
                    .subscribe();


        } catch (Exception e) {
            log.error("风控工作流执行异常", e);
        }
    }

    private void adminProcess(RunnableConfig config, boolean approve, String roomId) throws Exception {
        // 使用 updateState 更新状态
        RunnableConfig updatedConfig = riskWorkflow.updateState(config, Map.of(
                RiskStrategies.ADMIN_APPROVED, approve
        ), null);
        log.info("--- 阶段 3: 流程恢复，执行最终动作 ---");
        // 第一个参数传 null，代表从 checkpoint 加载状态继续
        riskWorkflow.stream(null, updatedConfig)
                .doOnNext(output -> log.info("节点 [{}] 运行完毕", output.node()))
                .doOnComplete(() -> {
                    log.info(">>>>>> [测试结束] 流程处理完毕");
                    // 最终状态确认
                    StateSnapshot finalState = riskWorkflow.getState(config);
                    log.info("最终执行决策: {}", finalState.state().data().get("last_action"));
                    RiskStrategies.cleanRedis(roomId, stringRedisTemplate);
                })
                .subscribe();
    }
}
