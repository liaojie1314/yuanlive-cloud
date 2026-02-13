package blog.yuanyuan.yuanlive.user.domain.dto;

import blog.yuanyuan.yuanlive.entity.user.entity.UserRoleEnum;
import blog.yuanyuan.yuanlive.entity.user.enums.GenderEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Data;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Data
public class UserDTO {
    @Schema(description="用户ID")
    @NotNull(message = "用户ID不能为空", groups = Update.class)
    private Long uid;
    @Schema(description="用户名")
    private String username;
    @Schema(description="密码(BCrypt加密)")
    @Null(message = "更新用户信息时不能修改密码，请前往专门的修改密码接口", groups = Update.class)
    @NotEmpty(message = "密码不能为空", groups = Insert.class)
    private String password;
    @Schema(description="头像")
    private String avatar;
    @Schema(description="手机号")
    private String phone;
    @Schema(description="邮箱")
    private String email;
    @Schema(description="性别")
    private GenderEnum gender;
    @Schema(description="用户类型")
    private UserRoleEnum role;
    private Integer status;

    private List<Long> roleIds;
}
