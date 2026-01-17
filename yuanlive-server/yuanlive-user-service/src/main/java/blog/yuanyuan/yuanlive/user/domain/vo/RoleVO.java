package blog.yuanyuan.yuanlive.user.domain.vo;

import blog.yuanyuan.yuanlive.entity.user.entity.SysMenu;
import blog.yuanyuan.yuanlive.entity.user.entity.SysRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 角色VO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "角色VO")
public class RoleVO extends SysRole {
    @Schema(description = "菜单组")
    private List<SysMenu> menus;
}
