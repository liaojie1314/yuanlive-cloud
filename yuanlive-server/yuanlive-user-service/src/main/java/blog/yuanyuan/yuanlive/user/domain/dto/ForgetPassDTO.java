package blog.yuanyuan.yuanlive.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "忘记密码DTO")
public class ForgetPassDTO {
    @Schema(description = "邮箱")
    @NotBlank(message = "邮箱不能为空")
    private String email;
    @Schema(description = "验证码")
    @NotBlank(message = "验证码不能为空")
    private String code;
    @Schema(description = "新密码")
    @NotBlank(message = "新密码不能为空")
    private String password;
    @Schema(description = "确认新密码")
    @NotBlank(message = "确认新密码不能为空")
    private String confirmPassword;
}
