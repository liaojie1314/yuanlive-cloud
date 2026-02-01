package blog.yuanyuan.yuanlive.live.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 直播间查询DTO
 */
@Data
@Schema(description = "直播间查询DTO")
public class LiveRoomQueryDTO {
    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页条数", example = "10")
    private Integer pageSize = 10;

    @Schema(description = "主播ID")
    private Long anchorId;

    @Schema(description = "直播状态 0:未开播 1:直播中")
    private Integer roomStatus;

    @Schema(description = "分类ID")
    private Integer categoryId;

    @Schema(description = "直播间标题(模糊查询)")
    private String title;
}
