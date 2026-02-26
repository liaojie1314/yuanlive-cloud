package blog.yuanyuan.yuanlive.live.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "视频分页查询参数")
public class VideoPageQueryDTO {

    @Schema(description = "用户ID")
    private Long uid;

    @Schema(description = "页码，从1开始")
    private Integer pageNum = 1;

    @Schema(description = "每页大小")
    private Integer pageSize = 10;
}