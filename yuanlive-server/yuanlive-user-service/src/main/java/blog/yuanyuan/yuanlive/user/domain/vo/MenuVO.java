package blog.yuanyuan.yuanlive.user.domain.vo;

import blog.yuanyuan.yuanlive.entity.user.entity.SysMenu;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

/**
 * 菜单VO
 */
@Data
@Schema(description = "菜单VO")
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MenuVO extends SysMenu {
    @Schema(description = "子菜单")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<MenuVO> children;
}
