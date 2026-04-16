package blog.yuanyuan.yuanlive.user.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "redis-key.login-limit")
public class LoginLimitProperties {
    private LimitConfig lock;
    private LimitConfig failCount;
    private Long threshold;
    @Data
    public static class LimitConfig {
        private String prefix;
        private Long ttl;
        private String timeunit;
    }
}
