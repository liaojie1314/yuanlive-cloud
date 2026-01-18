package blog.yuanyuan.yuanlive.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 角色DTO
 */
@Data
@Schema(description = "角色DTO")
public class RoleDTO {
    @Schema(description = "角色ID(修改时必传)")
    @NotNull(message = "角色ID不能为空", groups = Update.class)
    private Long roleId;

    @Schema(description = "角色名称")
    @NotBlank(message = "角色名称不能为空")
    private String roleName;

    @Schema(description = "角色字符串")
    @NotBlank(message = "角色字符串不能为空")
    private String roleKey;

    @Schema(description = "状态:1-正常,0-停用")
    private Integer status;

    @Schema(description = "菜单组")
    private List<Long> menuIds;
}
