package blog.yuanyuan.yuanlive.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

@Data
@Schema(description = "申请获取验证码DTO")
public class CodeDTO {
    @Schema(description = "邮箱")
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式错误")
    private String email;
    @Schema(description = "操作类型")
    @Pattern(regexp = "^(REGISTER|FORGET_PASSWORD)$", message = "操作类型错误")
    private String operationType;
}
