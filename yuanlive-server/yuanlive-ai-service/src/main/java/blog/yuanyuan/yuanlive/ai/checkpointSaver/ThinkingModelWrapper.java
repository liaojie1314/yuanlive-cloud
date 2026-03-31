package blog.yuanyuan.yuanlive.ai.checkpointSaver;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

public class ThinkingModelWrapper implements ChatModel {
    private final ChatModel delegate;

    public ThinkingModelWrapper(ChatModel delegate) {
        this.delegate = delegate;
    }

    @Override
    public ChatResponse call(Prompt prompt) {
        return delegate.call(prompt);
    }

    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        StringBuilder fullReasoning = new StringBuilder();
        return delegate.stream(prompt).map(response -> {
            Object r = response.getResult().getOutput().getMetadata().get("reasoningContent");
            if (r == null) {
                r = response.getResult().getOutput().getMetadata().get("reasoning_content");
            }
            if (r != null && !r.toString().isEmpty()) {
                String delta = r.toString();
                fullReasoning.append(delta);
                response.getResult().getOutput().getMetadata().put("reasoningDelta", delta);
            }
            if (!fullReasoning.isEmpty()) {
                response.getResult().getOutput().getMetadata().put("reasoningContent", fullReasoning.toString());
            }

            return response;
        });
    }
}
