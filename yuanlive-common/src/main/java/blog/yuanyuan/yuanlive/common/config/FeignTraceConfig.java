package blog.yuanyuan.yuanlive.common.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignTraceConfig {
    @Bean
    public RequestInterceptor traceIdRequestInterceptor() {
        return template -> {
            // 从 MDC 拿 ID
            String traceId = MDC.get("traceId");
            // 只要 ID 存在，就塞到 Feign 发出的 Header 里
            if (traceId != null) {
                template.header("traceId", traceId);
            }
        };
    }
}
