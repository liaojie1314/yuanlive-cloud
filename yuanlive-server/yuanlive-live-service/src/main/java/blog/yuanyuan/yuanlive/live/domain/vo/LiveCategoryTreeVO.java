package blog.yuanyuan.yuanlive.live.domain.vo;

import blog.yuanyuan.yuanlive.entity.live.entity.LiveCategory;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 直播分类树形结构VO - 用于treeList接口
 * 只包含parentId，不包含parentIds
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "直播分类树形结构VO")
@JsonIgnoreProperties(value = {"sortWeight"})
public class LiveCategoryTreeVO extends LiveCategory {
    @Schema(description = "子分类")
    private List<LiveCategoryTreeVO> children;
    
    @Schema(description="父分类ID (0表示一级分类)")
    private Integer parentId;
}