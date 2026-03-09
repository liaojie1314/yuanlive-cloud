package blog.yuanyuan.yuanlive.user.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Data
@Configuration
@ConfigurationProperties(prefix = "redis-key.search")
public class SearchProperties {
    private History history = new History();
    private Hot hot = new Hot();

    @Data
    public static class History {
        private String prefix;
        private Duration ttl;
        private int maxCount;
    }

    @Data
    public static class Hot {
        private String rankKey;
        private String hourRankPrefix;
        private String aggKey;
        private Duration ttl;
    }
}
