package blog.yuanyuan.yuanlive.live.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 直播分类查询DTO
 */
@Data
@Schema(description = "直播分类查询DTO")
public class LiveCategoryQueryDTO {
    @Schema(description = "分类名称(模糊查询)")
    private String name;

    @Schema(description = "父分类IDs")
    private List<Integer> parentIds;

    @Schema(description = "当前页", example = "1")
    private Integer current = 1;

    @Schema(description = "每页数量", example = "10")
    private Integer size = 10;
}
