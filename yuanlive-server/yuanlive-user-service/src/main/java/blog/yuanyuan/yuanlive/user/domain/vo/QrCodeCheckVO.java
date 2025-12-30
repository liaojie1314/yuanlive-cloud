package blog.yuanyuan.yuanlive.user.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@Schema(description = "二维码状态检查结果")
public class QrCodeCheckVO {
    @Schema(description = "用户ID，仅在已确认状态下返回")
    private String uid;

    @Schema(description = "状态码：0-等待扫码，1-已扫码，2-已确认，3-已取消，4-已过期")
    private int status;

    @Schema(description = "登录令牌，仅在已确认状态下返回")
    private String accessToken;
    @Schema(description = "刷新令牌，仅在已确认状态下返回")
    private String refreshToken;

}
