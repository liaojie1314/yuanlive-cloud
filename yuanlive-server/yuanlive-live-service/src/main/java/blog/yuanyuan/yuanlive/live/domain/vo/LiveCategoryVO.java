package blog.yuanyuan.yuanlive.live.domain.vo;

import blog.yuanyuan.yuanlive.entity.live.entity.LiveCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 直播分类VO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "直播分类VO")
public class LiveCategoryVO extends LiveCategory {
    @Schema(description = "子分类")
    private List<LiveCategoryVO> children;
}
