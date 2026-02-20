package blog.yuanyuan.yuanlive.ai.config;

import io.micrometer.observation.ObservationRegistry;
import jakarta.annotation.Resource;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.model.ApiKey;
import org.springframework.ai.model.tool.DefaultToolCallingManager;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.resolution.StaticToolCallbackResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

//@Configuration
public class ChatModelConfig {
    @Value("${spring.ai.openai.api-key}")
    private String apiKey;
    @Value("${spring.ai.openai.base-url}")
    private String baseUrl;
    @Value("${spring.ai.openai.chat.options.model}")
    private String model;
    private final List<ToolCallback> toolCallbacks;

    public ChatModelConfig(List<ToolCallback> toolCallbacks) {
        this.toolCallbacks = toolCallbacks;
    }

    @Bean
    public OpenAiChatModel nvidiaChatModel() {
        OpenAiApi openAiApi = getOpenAiApi();
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(model)
                .temperature(0.7)
                .build();
        StaticToolCallbackResolver resolver = new StaticToolCallbackResolver(toolCallbacks);
        DefaultToolCallingManager toolCallingManager = DefaultToolCallingManager.builder()
                .toolCallbackResolver(resolver).build();
        return new OpenAiChatModel(
                openAiApi,
                options,
                toolCallingManager,
                new RetryTemplate(),
                ObservationRegistry.NOOP);
    }

    @NotNull
    private OpenAiApi getOpenAiApi() {
        ApiKey key = new ApiKey() {
            @NotNull
            @Override
            public String getValue() {
                return apiKey;
            }
        };
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        String completionsPath = "/chat/completions";
        String embeddingsPath = "/embeddings";
        return new OpenAiApi(
                baseUrl,
                key,
                headers,
                completionsPath,
                embeddingsPath,
                RestClient.builder(),
                WebClient.builder(),
                new DefaultResponseErrorHandler());
    }

}
