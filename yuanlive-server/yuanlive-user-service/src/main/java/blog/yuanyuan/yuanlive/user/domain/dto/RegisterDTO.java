package blog.yuanyuan.yuanlive.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "注册信息DTO")
public class RegisterDTO {
    @Schema(description = "用户名")
    private String username;
    @Schema(description = "邮箱")
    @Email(message = "格式错误")
    private String email;
    @Schema(description = "验证码")
    @NotBlank(message = "验证码不能为空")
    private String code;
    @Schema(description = "密码")
    @NotBlank(message = "密码不能为空")
    private String password;
    @Schema(description = "确认密码")
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;
}
