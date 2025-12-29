package blog.yuanyuan.yuanlive.entity.user.entity;


import java.util.Date;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class SysMenu {
    @Schema(description="")
    private Long menuId;
    @Schema(description="菜单/权限名称")
    private String menuName;
    @Schema(description="父菜单ID")
    private Long parentId;
    @Schema(description="显示顺序")
    private Integer orderNum;
    @Schema(description="路由地址")
    private String path;
    @Schema(description="前端组件路径")
    private String component;
    @Schema(description="类型:M-目录,C-菜单,F-按钮")
    private String menuType;
    @Schema(description="权限标识(如 user:list)")
    private String perms;
    @Schema(description="图标")
    private String icon;
    @Schema(description="")
    private Date createTime;
    @Schema(description="")
    private Date updateTime;
}
