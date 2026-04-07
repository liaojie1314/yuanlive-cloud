package blog.yuanyuan.yuanlive.ai.filter;

import com.alibaba.cloud.ai.graph.agent.interceptor.ModelCallHandler;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelInterceptor;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelRequest;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class MessageFilterInterceptor extends ModelInterceptor {
    private final int maxMessages;

    public MessageFilterInterceptor(@Value("${spring.ai.max-messages}") int maxMessages) {
        this.maxMessages = maxMessages;
    }

    @Override
    public ModelResponse interceptModel(ModelRequest request, ModelCallHandler handler) {
        List<Message> messages = request.getMessages();

        Message systemMessage = messages.stream()
                .filter(m -> m instanceof SystemMessage)
                .findFirst()
                .orElse(null);
        messages.removeIf(m -> m instanceof SystemMessage);

        if (!messages.isEmpty()) {
            List<Message> filtered = new ArrayList<>();

            int roundCount = 0;
            String lastPairId = "";

            // 从后往前扫描
            for (int i = messages.size() - 1; i >= 0; i--) {
                Message m = messages.get(i);
                String currentUserId = (String) m.getMetadata().getOrDefault("userId", "default");

                if (!currentUserId.equals(lastPairId)) {
                    roundCount++;
                    lastPairId = currentUserId;
                }

                if (roundCount <= maxMessages) {
                    filtered.add(0, m);
                } else {
                    break;
                }
            }
            messages = filtered;
        }
        if (systemMessage != null) {
            messages.add(0, systemMessage);
        }

//        log.info("Filtered message size: {}", messages.size());
//        messages.forEach(m -> log.info("Message: {}", m));

        ModelRequest enhanced = ModelRequest.builder(request)
                .messages(messages)
                .build();
        return handler.call(enhanced);
    }

    @Override
    public String getName() {
        return "MessageFilterInterceptor";
    }
}
