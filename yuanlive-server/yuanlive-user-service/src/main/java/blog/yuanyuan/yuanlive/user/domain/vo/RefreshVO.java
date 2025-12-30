package blog.yuanyuan.yuanlive.user.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RefreshVO {
    @Schema(description = "访问令牌，前端使用时需要'Authorization '进行拼接")
    private String accessToken;
    @Schema(description = "刷新令牌")
    private String refreshToken;
}
