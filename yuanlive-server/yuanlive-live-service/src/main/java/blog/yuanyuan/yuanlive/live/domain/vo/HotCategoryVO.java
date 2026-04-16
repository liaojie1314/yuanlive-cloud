package blog.yuanyuan.yuanlive.live.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "热搜分类")
public class HotCategoryVO {
    @Schema(description = "热搜分类id")
    private Integer id;
    @Schema(description = "热搜分类名")
    private String label;
    @Schema(description = "热搜分类值")
    private String value;
    @Schema(description = "热搜分类父级值")
    private List<String> parentValue;
}
