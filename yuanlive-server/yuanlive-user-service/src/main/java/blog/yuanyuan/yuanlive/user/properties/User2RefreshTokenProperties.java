package blog.yuanyuan.yuanlive.user.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "redis-key.user2refresh-token")
@Data
public class User2RefreshTokenProperties {
    private String prefix;
    private Long ttl;
    private String timeunit;
}
