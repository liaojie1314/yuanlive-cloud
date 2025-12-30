package blog.yuanyuan.yuanlive.user.properties;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "redis-key.qr-code")
@Data
@Schema(description = "二维码相关配置")
public class QrCodeProperties {
    @Schema(description = "二维码Redis键前缀")
    private String prefix;
    
    @Schema(description = "二维码有效期")
    private Long ttl;
    
    @Schema(description = "时间单位")
    private String timeunit;
}
