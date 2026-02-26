package blog.yuanyuan.yuanlive.live.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 直播分类DTO
 */
@Data
@Schema(description = "直播分类DTO")
public class LiveCategoryDTO {
    @Schema(description = "分类ID(修改时必传)")
    @NotNull(message = "分类ID不能为空", groups = Update.class)
    private Integer id;

    @Schema(description = "父分类IDs")
    private List<Integer> parentIds;

    @Schema(description = "分类名称")
    @NotBlank(message = "分类名称不能为空")
    private String name;

    @Schema(description = "分类名对应的英文名")
    @NotBlank(message = "分类名对应的英文名不能为空")
    private String value;

    @Schema(description = "分类图标")
    private String iconUrl;

    @Schema(description = "排序权重 (越大越靠前)")
    private Integer sortWeight;
}
