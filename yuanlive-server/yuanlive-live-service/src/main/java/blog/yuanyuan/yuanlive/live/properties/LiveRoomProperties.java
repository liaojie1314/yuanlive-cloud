package blog.yuanyuan.yuanlive.live.properties;

import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "redis-key.live-room")
@Data
public class LiveRoomProperties {
    private String sessionPrefix;
    private String currentPrefix;
    private String totalPrefix;
}
