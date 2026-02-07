package blog.yuanyuan.yuanlive.entity.live.entity;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class LiveCategory {
    @Schema(description="")
    private Integer id;
    @Schema(description="父分类ID (0表示一级分类)")
    private Integer parentId;
    @Schema(description="分类名称")
    private String name;
    @Schema(description="分类图标")
    private String iconUrl;
    @Schema(description="排序权重 (越大越靠前)")
    private Integer sortWeight;
    @Schema(description = "分类名对应的英文名")
    private String value;
}
