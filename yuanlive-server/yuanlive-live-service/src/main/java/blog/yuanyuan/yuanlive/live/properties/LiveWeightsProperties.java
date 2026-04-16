package blog.yuanyuan.yuanlive.live.properties;

import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "live.weights")
@Data
public class LiveWeightsProperties {
    private double view;
    private double like;
    private double chat;
    private double gift;
}
