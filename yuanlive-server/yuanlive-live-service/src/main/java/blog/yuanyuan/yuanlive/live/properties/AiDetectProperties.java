package blog.yuanyuan.yuanlive.live.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConfigurationProperties(prefix = "live.ai")
@Data
public class AiDetectProperties {
    private long aiDetectThreshold;
    private Integer cacheThreshold;
    private Duration ttl;
}
