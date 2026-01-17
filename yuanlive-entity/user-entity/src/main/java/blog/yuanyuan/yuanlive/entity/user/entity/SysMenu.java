package blog.yuanyuan.yuanlive.entity.user.entity;


import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class SysMenu {
    @Schema(description="")
    @TableId(type = IdType.AUTO)
    private Long menuId;
    @Schema(description="菜单/权限名称")
    private String name;
    @Schema(description="父菜单ID")
    private Long parentId;
    @Schema(description="显示顺序")
    private Integer sort;
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
    @Schema(description="菜单名称/标题")
    private String title;
    @Schema(description="是否显示启用(0 -> 隐藏，1 -> 显示)")
    private Integer isVisible;
}
