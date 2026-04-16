package blog.yuanyuan.yuanlive.live.config;

import blog.yuanyuan.yuanlive.live.tools.LiveMcpTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ToolCallbackConfig {
    @Bean
    public ToolCallbackProvider toolCallbackProvider(LiveMcpTools liveMcpTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(liveMcpTools)
                .build();
    }
}
