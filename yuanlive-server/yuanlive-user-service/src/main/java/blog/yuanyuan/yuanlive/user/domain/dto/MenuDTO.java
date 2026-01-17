package blog.yuanyuan.yuanlive.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.apache.ibatis.annotations.Update;

/**
 * 菜单DTO
 */
@Data
@Schema(description = "菜单DTO")
public class MenuDTO {
    @Schema(description = "菜单ID(修改时必传)")
    @NotNull(message = "菜单ID不能为空", groups = Update.class)
    private Long menuId;

    @Schema(description = "菜单/权限名称")
    @NotBlank(message = "菜单名称不能为空")
    private String name;

    @Schema(description = "父菜单ID")
    @NotNull(message = "父菜单ID不能为空")
    private Long parentId;

    @Schema(description = "显示顺序")
    private Integer sort;

    @Schema(description = "路由地址")
    private String path;

    @Schema(description = "前端组件路径")
    private String component;

    @Schema(description = "类型:M-目录,C-菜单,F-按钮")
    @Pattern(message = "菜单类型必须是 M(目录)、C(菜单) 或 F(按钮)", regexp = "^[MCF]$")
    private String menuType;

    @Schema(description = "权限标识(如 user:list)")
    private String perms;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "菜单名称/标题")
    @NotBlank(message = "菜单标题不能为空")
    private String title;

    @Schema(description = "是否显示启用(0 -> 隐藏，1 -> 显示)")
    private Integer isVisible;
}
