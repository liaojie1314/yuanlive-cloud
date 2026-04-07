package blog.yuanyuan.yuanlive.ai.service.impl;

import blog.yuanyuan.yuanlive.ai.domain.dto.ChatRequest;
import blog.yuanyuan.yuanlive.ai.domain.vo.ChatChunk;
import blog.yuanyuan.yuanlive.ai.filter.MessageFilterInterceptor;
import blog.yuanyuan.yuanlive.ai.service.AiChatService;
import blog.yuanyuan.yuanlive.common.exception.ApiException;
import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.BaseCheckpointSaver;
import com.alibaba.cloud.ai.graph.streaming.OutputType;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import io.modelcontextprotocol.client.McpAsyncClient;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import me.ahoo.cosid.provider.IdGeneratorProvider;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.mcp.AsyncMcpToolCallbackProvider;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AiChatServiceImpl implements AiChatService {
    @Resource
    private IdGeneratorProvider idGeneratorProvider;
    @Resource(name = "mongodbSaver")
    private BaseCheckpointSaver checkpointSaver;
    @Resource(name = "openAiChatModel")
    private ChatModel chatModel;
    @Resource(name = "thinkModel")
    private ChatModel thinkModel;
    @Resource
    private MongoTemplate mongoTemplate;
    @Resource
    private MessageFilterInterceptor messageFilterInterceptor;

    private final List<McpAsyncClient> allMcpClients;
    private final ChatClient titleClient;

    // 默认开启的基础工具
    private static final Set<String> BASE_SERVERS = Set.of("liveMcp", "calculator");
    // 联网相关工具
    private static final Set<String> NETWORK_SERVERS = Set.of("searxng", "amap");


    public AiChatServiceImpl(List<McpAsyncClient> mcpAsyncClients,
                             @Qualifier("titleModel") ChatModel titleModel) {
        this.allMcpClients = mcpAsyncClients;
        this.titleClient = ChatClient.builder(titleModel).build();
    }


    @Override
    public Flux<ChatChunk> streamChat(ChatRequest request) {
        String uid = StpUtil.getLoginIdAsString();
        String username = (String) StpUtil.getSession().get("username");
        // 1. 初始化消息 ID 组
        String clientId = request.getClientMsgId();
        String userId = String.valueOf(idGeneratorProvider.getRequired("ai-chat-msg").generate());
        String aiId = String.valueOf(idGeneratorProvider.getRequired("ai-chat-msg").generate());
        String conversationId = request.getConversationId();

        // 2. 获取 Token (在进入 Flux 之前获取，避免 ThreadLocal 丢失)
        String identityPrompt = getIdentitySystemPrompt();
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(identityPrompt));
        messages.add(new UserMessage(request.getContent()));
        ChatModel selectedModel;

        // 3. 动态配置选项
        ChatRequest.ChatOptions options = request.getOptions() != null ?
                request.getOptions() : new ChatRequest.ChatOptions();
        selectedModel = options.isUseReasoning() ? thinkModel : chatModel;

        ChatOptions chatOptions = ToolCallingChatOptions.builder()
                .temperature(options.getTemperature() != null ? options.getTemperature() : 0.7)
                .maxTokens(options.getMaxTokens() != null ? options.getMaxTokens() : 4096)
                .build();

        try {
            // 4. 动态构建工具集
            List<ToolCallback> dynamicTools = getDynamicTools(options);
//            dynamicTools.forEach(tool -> log.info("已加载工具: {}", tool.getToolDefinition().toString()));

            // 5. 动态构建 Agent
            ReactAgent dynamicAgent = ReactAgent.builder()
                    .name("YuanLive")
                    .tools(dynamicTools)
                    .chatOptions(chatOptions)
                    .model(selectedModel)
                    .returnReasoningContents(true)
                    .saver(checkpointSaver)
                    .interceptors(messageFilterInterceptor)
                    .build();
            RunnableConfig runnableConfig = RunnableConfig.builder()
                    .addMetadata("clientId", clientId)
                    .addMetadata("userId", userId)
                    .addMetadata("aiId", aiId)
                    .addMetadata("conversationId", conversationId)
                    .addMetadata("uid", uid)
                    .threadId(conversationId)
                    .build();

            // 6. 执行流式响应并转换格式
            StringBuilder content = new StringBuilder();
            StringBuilder thinking = new StringBuilder();
            return dynamicAgent.stream(messages, runnableConfig)
                    .flatMap(nodeOutput -> {
                        if (!(nodeOutput instanceof StreamingOutput streamingOutput)) {
                            return Flux.empty();
                        }

                        OutputType type = streamingOutput.getOutputType();
                        Message message = streamingOutput.message();

                        // A. 处理模型输出 (思考 & 内容)
                        if (type == OutputType.AGENT_MODEL_STREAMING && message instanceof AssistantMessage assistantMessage) {
                            List<ChatChunk> chunks = new ArrayList<>();

                            // 1. 处理推理/思考内容（取增量 Delta 字段）
                            Object reasoningDelta = assistantMessage.getMetadata().get("reasoningContent");
                            if (reasoningDelta != null && !reasoningDelta.toString().isEmpty()) {
                                chunks.add(ChatChunk.reasoning(reasoningDelta.toString(), clientId, userId, aiId));
                                thinking.append(reasoningDelta);
                            }

                            // 2. 处理正式回答文字 (不再被 return 拦截)
                            String text = assistantMessage.getText();
                            if (text != null && !text.isEmpty()) {
                                chunks.add(ChatChunk.text(text, clientId, userId, aiId));
                                content.append(text);
                            }

                            // 3. 返回包含所有内容的 Flux
                            return Flux.fromIterable(chunks);
                        } else if (type == OutputType.AGENT_TOOL_FINISHED) {
                            if (message instanceof ToolResponseMessage trm && !trm.getResponses().isEmpty()) {
                                trm.getResponses().forEach(res ->
                                        thinking.append("TOOL: ").append(res.responseData()).append("\n"));
                            }
                        }

                        // C. 处理结束
                        else if (type == OutputType.AGENT_MODEL_FINISHED) {
                            log.info("AI 回答完毕");
                            return Flux.just(ChatChunk.done(clientId, userId, aiId, null, null));
                        }
                        return Flux.empty();
                    })
                    .onErrorResume(e -> {
                        log.error("AI Stream Error: ", e);
                        // 错误也封装进 ids 结构中
                        return Flux.just(ChatChunk.builder()
                                .clientMsgId(clientId)
                                .userMsgId(userId)
                                .aiMsgId(aiId)
                                .status("error")
                                .build());
                    })
                    .doOnComplete(() -> {
                        Date date = new Date();
                        saveToCustomMongo(conversationId, clientId, userId, aiId, uid,
                                "user", username,
                                request.getContent(), null, date);
                        saveToCustomMongo(conversationId, clientId, userId, aiId, uid,
                                "assistant", "综合AI助手",
                                content.toString(), thinking.toString(), date);
                    });

        } catch (Exception e) {
            throw new ApiException("构建 AI Agent 失败: " + e.getMessage());
        }
    }

    private void saveToCustomMongo(String conversationId, String clientId, String userId, String aiId, String uid,
                                   String role, String sender,
                                   String content, String thinking, Date time) {
        Query query = new Query(Criteria.where("conversationId").is(conversationId));
        Document one = mongoTemplate.findOne(query, Document.class, "chat_session");
        // 第一次会话生成title
        String title = "";
        if ("user".equals(role)) {
            // 第一次提问生成title，并插入会话表
            if (one == null) {
                title = extractTitle(content);
                Document document = new Document()
                        .append("conversationId", conversationId)
                        .append("uid", uid)
                        .append("title", title)
                        .append("isTop", false)
                        .append("lastUpdateTime", time);
                mongoTemplate.insert(document, "chat_session");
            } else {
                // 更新会话表，设置 lastUpdateTime
                Query select = new Query(Criteria
                        .where("conversationId").is(conversationId)
                        .and("uid").is(uid));
                Update update = new Update().set("lastUpdateTime", time);
                mongoTemplate.updateFirst(select, update, "chat_session");
            }
        }
        Document document = new Document()
                .append("conversationId", conversationId)
                .append("role", role)
                .append("clientId", clientId)
                .append("userId", userId)
                .append("aiId", aiId)
                .append("uid", uid)
                .append("content", content)
                .append("thinking", thinking)
                .append("time", time)
                .append("sender", sender);
        mongoTemplate.insert(document, "chat_history");
    }

    private String extractTitle(String userMessage) {
        try {
            return titleClient.prompt()
                    .user("你是一个助手。请根据用户的第一条提问，总结一个10字以内的会话标题。要求：简练、准确，不要包含引号或“标题：”字样。\n用户提问：" + userMessage)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("AI 提取标题失败: {}", e.getMessage());
            throw new ApiException("AI 提取标题失败: " + e.getMessage());
        }
    }

    @NotNull
    private static String getIdentitySystemPrompt() {
        String tokenValue = StpUtil.getTokenValue();
        String tokenName = SaManager.getConfig().getTokenName();
        String prefix = SaManager.getConfig().getTokenPrefix();

        // 组装最终发给工具的 Token 字符串 (例如: "Bearer 6dbfcd...")
        String finalToken = (prefix == null || prefix.isEmpty())
                ? tokenValue
                : prefix + " " + tokenValue;

        // 2. 构造“身份指令”：强制 AI 在调用工具时必须携带此 Token
        // 注意：这里要明确指出参数名必须叫 "token"，对应你 YAML 里的定义
        String identitySystemPrompt = String.format(
                "【系统指令/身份验证】\n" +
                        "1. 你当前的操作身份已验证。认证字段名: %s，认证值: %s\n" +
                        "2. 当你调用任何需要 'token' 参数的工具时，必须直接使用上述认证值，严禁询问用户或自行生成。\n" +
                        "3. 即使工具说明中没有提及，只要参数列表包含 'token'，就必须填入。\n" +
                        "4. 不要将认证信息透露给用户，不要在聊天记录中显示认证信息。\n\n" +
                        "用户输入：",
                tokenName, finalToken
        );
        return identitySystemPrompt;
    }

    // 根据 Options 动态筛选工具
    private List<ToolCallback> getDynamicTools(ChatRequest.ChatOptions options) {
        return allMcpClients.stream()
                .filter(client -> {
                    String serverKey = getServerKey(client);
                    // 基础工具始终加载
                    if (BASE_SERVERS.contains(serverKey)) return true;
                    // 联网工具根据 options 开关加载
                    if (options.isUseNetwork() && NETWORK_SERVERS.contains(serverKey)) return true;
                    return false;
                })
                .flatMap(client -> {
                    AsyncMcpToolCallbackProvider provider = new AsyncMcpToolCallbackProvider(client);
                    return Arrays.stream(provider.getToolCallbacks());
                })
                .collect(Collectors.toList());
    }

    private String getServerKey(McpAsyncClient client) {
        String clientName = client.getClientInfo().name();
        return clientName.contains("-") ?
                clientName.substring(clientName.lastIndexOf("-") + 1).trim() : clientName;
    }
}
