package blog.yuanyuan.yuanlive.user.domain.vo;

import blog.yuanyuan.yuanlive.entity.user.entity.SysMenu;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 菜单VO
 */
@Data
@Schema(description = "菜单VO")
public class MenuVO extends SysMenu {
    @Schema(description = "子菜单")
    private List<MenuVO> children;
}
