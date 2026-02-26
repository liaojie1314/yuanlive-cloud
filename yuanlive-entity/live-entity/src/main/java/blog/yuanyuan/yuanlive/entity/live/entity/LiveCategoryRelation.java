package blog.yuanyuan.yuanlive.entity.live.entity;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class LiveCategoryRelation {
    @Schema(description="主键ID")
    private Integer id;
    @Schema(description="子分类ID")
    private Integer categoryId;
    @Schema(description="父分类ID")
    private Integer parentId;
}
