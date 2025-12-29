package blog.yuanyuan.yuanlive.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import jakarta.validation.constraints.Pattern;

@Data
@Schema(description = "登录参数DTO")
public class LoginDTO {
    @Schema(description = "账号(用户名或邮箱)")
    @NotBlank(message = "账号不能为空")
    private String account;
    @Schema(description = "密码")
    private String password;
    @Schema(description = "验证码")
    private String code;
    @Schema(description = "设备类型：mobile, desktop, web")
    @Pattern(regexp = "^(mobile|desktop|web)$", message = "设备类型只能为：mobile, desktop, web")
    private String device;
}