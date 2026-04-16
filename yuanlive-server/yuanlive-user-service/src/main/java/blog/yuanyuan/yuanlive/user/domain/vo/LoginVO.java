package blog.yuanyuan.yuanlive.user.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginVO {
    @Schema(description = "访问令牌，前端使用时需要'Authorization '进行拼接")
    private String accessToken;
    @Schema(description = "刷新令牌")
    private String refreshToken;
    @Schema(description = "用户角色:0->普通用户, 1->主播, 2->管理员")
    private Integer role;
    @Schema(description = "用户id")
    private String uid;
    @Schema(description = "accessToken过期时间戳")
    private Long expire;
}
