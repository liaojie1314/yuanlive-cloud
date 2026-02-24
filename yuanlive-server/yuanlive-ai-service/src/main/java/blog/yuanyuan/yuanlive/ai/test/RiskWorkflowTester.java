package blog.yuanyuan.yuanlive.ai.test;

import blog.yuanyuan.yuanlive.ai.strategy.RiskStrategies;
import cn.hutool.core.util.IdUtil;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.state.StateSnapshot;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

//@Component
@Slf4j
public class RiskWorkflowTester implements CommandLineRunner {
    @Resource
    private CompiledGraph riskWorkflow; // 注入我们在 WorkflowConfig 中定义的 Bean
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void run(String... args) throws Exception {
        // 执行高风险模拟测试
        testHighRiskScenario("790546539380737");
    }

    /**
     * 模拟高风险场景：AI 判定需封禁，流程中断，等待管理员审批后恢复
     */
    private void testHighRiskScenario(String roomId) throws Exception {
        log.info(">>>>>> [测试开始] 房间号: {}", roomId);

        // 1. 准备初始状态
        Map<String, Object> inputData = Map.of(
                RiskStrategies.ROOM_ID, roomId,
                RiskStrategies.CHAT_HISTORY, List.of("我操你妈", "我操你妈", "我操你妈", "我操你妈")
        );

        // 2. 配置执行上下文 (threadId 是持久化的 Key)
        RunnableConfig config = RunnableConfig.builder()
                .threadId(roomId)
                .build();

        // 3. 第一次执行：运行到中断点 (admin_review)
        log.info("--- 阶段 1: AI 正在进行深度分析 ---");
        riskWorkflow.stream(inputData, config)
                .doOnNext(output -> log.info("节点 [{}] 运行完毕", output.node()))
                .blockLast(); // 阻塞直到遇到中断或结束

        // 4. 获取当前快照，模拟管理员查看后台
        StateSnapshot snapshot = riskWorkflow.getState(config);
        Integer score = (Integer) snapshot.state().data().get(RiskStrategies.RISK_SCORE);
        log.info("AI 判定分数: {}，当前所处位置: {}", score, snapshot.next());

        // 5. 模拟管理员介入：注入审批结果
        if (snapshot.next().contains("admin_review")) {
            log.info("--- 阶段 2: 模拟管理员介入，执行审批 ---");

            // 使用 updateState 更新状态，传入 null 作为节点 ID（官网标准：仅更新数据，不移动指针）
            RunnableConfig updatedConfig = riskWorkflow.updateState(config, Map.of(
                    RiskStrategies.ADMIN_APPROVED, true,
                    "admin_memo", "确认违规，准予封禁"
            ), null);

            // 6. 恢复执行：从中断点继续往下走
            log.info("--- 阶段 3: 流程恢复，执行最终动作 ---");

            // 官网技巧：第一个参数传 null，代表从 checkpoint 加载状态继续
            riskWorkflow.stream(null, updatedConfig)
                    .doOnNext(output -> log.info("节点 [{}] 运行完毕", output.node()))
                    .doOnComplete(() -> log.info(">>>>>> [测试结束] 流程处理完毕"))
                    .blockLast();

            // 7. 最终状态确认
            StateSnapshot finalState = riskWorkflow.getState(config);
            log.info("最终执行决策: {}", finalState.state().data().get("last_action"));
        }
        RiskStrategies.cleanRedis(roomId, stringRedisTemplate);
    }
}
