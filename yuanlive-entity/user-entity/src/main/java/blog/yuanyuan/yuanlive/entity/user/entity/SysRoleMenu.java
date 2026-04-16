package blog.yuanyuan.yuanlive.entity.user.entity;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class SysRoleMenu {
    @Schema(description="角色ID")
    private Long roleId;
    @Schema(description="菜单ID")
    private Long menuId;
}
