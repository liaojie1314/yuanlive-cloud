package blog.yuanyuan.yuanlive.entity.user.entity;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class SysUserRole {
    @Schema(description="用户ID")
    private Long userId;
    @Schema(description="角色ID")
    private Long roleId;
}
