package blog.yuanyuan.yuanlive.user.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "redis-key.register")
@Configuration
@Data
public class RegisterProperties {
    private String prefix;
    private Long ttl;
    private String timeunit;
}
