package blog.yuanyuan.yuanlive.ai.checkpointSaver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class ThinkingModelWrapper implements ChatModel {
    private final ChatModel delegate;

    public ThinkingModelWrapper(ChatModel delegate) {
        this.delegate = delegate;
    }

    @Override
    public ChatResponse call(Prompt prompt) {
        List<Message> cleanedInstructions = prompt.getInstructions().stream()
                .map(msg -> {
                    if (msg instanceof AssistantMessage assistantMsg &&
                            (msg.getMetadata().containsKey("reasoningContent") || msg.getMetadata().containsKey("reasoning_content"))) {

                        // 克隆元数据并移除旧的推理内容（仅用于本次请求）
                        Map<String, Object> cleanProperties = new HashMap<>(assistantMsg.getMetadata());
                        cleanProperties.remove("reasoningContent");
                        cleanProperties.remove("reasoning_content");
                        cleanProperties.remove("reasoningDelta");

                        // 使用 Builder 创建副本，保护原始对象的引用不被破坏
                        return AssistantMessage.builder()
                                .content(assistantMsg.getText())
                                .properties(cleanProperties)
                                .toolCalls(assistantMsg.getToolCalls())
                                .media(assistantMsg.getMedia())
                                .build();
                    }
                    return msg;
                }).collect(Collectors.toList());

        Prompt cleanedPrompt = new Prompt(cleanedInstructions, prompt.getOptions());
        ChatResponse response = delegate.call(cleanedPrompt);
        if (response != null && response.getResult() != null) {
            Map<String, Object> metadata = response.getResult().getOutput().getMetadata();
            Object r = metadata.get("reasoningContent");
            if (r == null) {
                r = metadata.get("reasoning_content");
            }
            if (r != null && !r.toString().isEmpty()) {
                metadata.put("reasoningContent", r.toString());
            }
        }

        return response;
    }

    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
//        prompt.getInstructions().forEach(msg -> {
//            log.info("【发送验证】角色: {}, 包含推理内容: {}, 内容: {}",
//                    msg.getMessageType(),
//                    msg.getMetadata().containsKey("reasoningContent"),
//                    msg.getMetadata().get("reasoningContent"));
//        });
        List<Message> cleanedInstructions = prompt.getInstructions().stream()
                .map(msg -> {
                    // 仅处理带有推理内容的 AI 消息
                    if (msg instanceof AssistantMessage assistantMsg &&
                            (msg.getMetadata().containsKey("reasoningContent") || msg.getMetadata().containsKey("reasoning_content"))) {

                        // 复制元数据并移除推理字段
                        Map<String, Object> cleanProperties = new HashMap<>(assistantMsg.getMetadata());
                        cleanProperties.remove("reasoningContent");
                        cleanProperties.remove("reasoning_content");
                        cleanProperties.remove("reasoningDelta");

                        return AssistantMessage.builder()
                                .content(assistantMsg.getText())
                                .properties(cleanProperties)
                                .toolCalls(assistantMsg.getToolCalls())
                                .media(assistantMsg.getMedia())
                                .build();
                    }
                    return msg;
                }).collect(Collectors.toList());

        // 使用清洗后的指令创建新的临时 Prompt
        Prompt cleanedPrompt = new Prompt(cleanedInstructions, prompt.getOptions());

        // 打印日志验证
//        cleanedPrompt.getInstructions().forEach(msg -> {
//            log.info("【发送验证】角色: {}, 包含推理内容: {}, 内容: {}",
//                    msg.getMessageType(),
//                    msg.getMetadata().containsKey("reasoningContent"),
//                    msg.getMetadata().get("reasoningContent"));
//        });

        StringBuilder fullReasoning = new StringBuilder();

        // 注意：这里调用 delegate 时传入的是 cleanedPrompt
        return delegate.stream(cleanedPrompt).map(response -> {
            // 后续处理模型返回的增量推理内容 (逻辑保持不变)
            Object r = response.getResult().getOutput().getMetadata().get("reasoningContent");
            if (r == null) {
                r = response.getResult().getOutput().getMetadata().get("reasoning_content");
            }

            if (r != null && !r.toString().isEmpty()) {
                String delta = r.toString();
                fullReasoning.append(delta);
                // 注入当前流式的增量
                response.getResult().getOutput().getMetadata().put("reasoningDelta", delta);
            }

            if (!fullReasoning.isEmpty()) {
                // 注入累计的完整推理内容
                response.getResult().getOutput().getMetadata().put("reasoningContent", fullReasoning.toString());
            }

            return response;
        });
    }
}
