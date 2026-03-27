package blog.yuanyuan.yuanlive.ai.nodes;

import blog.yuanyuan.yuanlive.ai.strategy.RiskStrategies;
import com.alibaba.cloud.ai.graph.action.NodeAction;
//import com.alibaba.cloud.ai.mcp.discovery.client.tool.DistributedAsyncMcpToolCallbackProvider;
import com.alibaba.cloud.ai.mcp.discovery.client.tool.DistributedAsyncMcpToolCallbackProvider;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.mcp.AsyncMcpToolCallbackProvider;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class RiskActionNodes {
    @Resource
    private ChatModel nvidiaModel;
    @Resource
    private DistributedAsyncMcpToolCallbackProvider distributedAsyncMcpToolCallbackProvider;

    private ChatClient chatClient;

    public static final String systemPrompt = """
    你是一名经验丰富的直播间风控专家。你的任务是分析对话内容并给出【违规程度分值】。
    
    ### 核心判定标准（高分项）：
    1. 政治敏感：涉及政治人物、政策攻击、非法集会等（直接 90-100分）。
    2. 色情低俗：露骨的性暗示、招嫖信息、淫秽色情描述（直接 85-100分）。
    3. 恶意辱骂：针对他人的严重人格侮辱、诅咒、仇恨言论（70-90分）。注意：普通的调侃、兄弟间的口嗨、无恶意的脏话不在此列。
    4. 违规网址：引导至非法博彩、诈骗、杀猪盘或站外交易的链接/联系方式（80-100分）。

    ### 噪音过滤标准（低分项）：
    - 幽默调侃：观众间的互动玩笑、梗、不带恶意的吐槽，即使包含轻微粗俗词汇，也应判定在 20分以下。
    - 情绪表达：如“我操这球进了”，属于语气助词，不属于辱骂，应判定为 0分。

    ### 输出要求：
    - 必须忽略轻微的互动噪音。
    - 必须对恶意行为严厉打击。
    - 只返回一个 0 到 100 之间的整数，不要任何解释。
    """;

    @PostConstruct
    public void init() {
        this.chatClient = ChatClient.builder(nvidiaModel)
                .defaultToolCallbacks(distributedAsyncMcpToolCallbackProvider.getToolCallbacks())
                .build();

        List<ToolDefinition> collect = Arrays.stream(distributedAsyncMcpToolCallbackProvider.getToolCallbacks())
                .map(callback -> callback.getToolDefinition())
                .collect(Collectors.toList());
        log.info("spring nacos工具为:{}", collect.toString());


    }

    public NodeAction analyze() {
        return state -> {
            String history = state.value(RiskStrategies.CHAT_HISTORY)
                    .map(Object::toString).orElse("");
            String result = chatClient.prompt()
                    .system(systemPrompt)
                    .user(history)
                    .call()
                    .content();
            int score = Integer.parseInt(result.replaceAll("[^0-9]", ""));
            return Map.of(RiskStrategies.RISK_SCORE, score);
        };
    }

    // 2. 警告主播
    public NodeAction warn() {
        return state -> {
            String roomId = state.value(RiskStrategies.ROOM_ID).map(Object::toString).orElse("unknown");
            log.info(">>> [Async] 正在调用远程 warnAnchor 工具...,房间号{}", roomId);

            // 异步触发远程调用
            Prompt prompt = new Prompt(new UserMessage("执行 warnAnchor，房间号：" + roomId));
            ChatResponse response = chatClient.prompt(prompt)
                    .call()
                    .chatResponse();
            log.info("ai 响应内容 {}", response.getResult().getOutput());

            return Map.of("last_action", "WARNED");
        };
    }

    // 3. 直播间公告
    public NodeAction broadcast() {
        return state -> {
            String roomId = state.value(RiskStrategies.ROOM_ID).map(Object::toString).orElse("unknown");
            log.info(">>> [Async] 正在调用远程 broadcast 工具...");

            ChatResponse response = chatClient.prompt("执行 broadcast，房间号：" + roomId)
                    .call()
                    .chatResponse();
            log.info("ai 响应内容 {}", response.getResult().getOutput());

            return Map.of("last_action", "BROADCASTED");
        };
    }

    // 4. 封禁直播间
    public NodeAction ban() {
        return state -> {
            String roomId = state.value(RiskStrategies.ROOM_ID).map(Object::toString)
                    .orElse("unknown");
            log.info(">>> [Async] 正在调用远程 banRoom 工具..., 封禁房间号{}", roomId);

            ChatResponse response = chatClient.prompt("执行banRoom, 房间号 " + roomId)
                    .call()
                    .chatResponse();
            log.info("ai 响应内容 {}", response.getResult().getOutput());
            return Map.of("last_action", "BAN");
        };
    }
}
