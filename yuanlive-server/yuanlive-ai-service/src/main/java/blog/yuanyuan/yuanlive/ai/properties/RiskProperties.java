package blog.yuanyuan.yuanlive.ai.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "risk")
@Configuration
@Data
public class RiskProperties {
    private int low;
    private int medium;
    private int high;
}
