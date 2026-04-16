package blog.yuanyuan.yuanlive.live.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "直播子类目返回内容")
public class LiveChildVO {
    @Schema(description = "id")
    private Integer id;
    @Schema(description = "标签")
    private String label;
    @Schema(description = "值")
    private String value;
}
