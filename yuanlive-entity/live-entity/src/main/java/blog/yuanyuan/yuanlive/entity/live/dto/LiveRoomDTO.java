package blog.yuanyuan.yuanlive.entity.live.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 直播间DTO
 */
@Data
@Schema(description = "直播间DTO")
public class LiveRoomDTO {

    @Schema(description = "直播间标题")
    private String title;

    @Schema(description = "直播间封面图URL")
    private String coverImg;

    @Schema(description = "分类ID")
    @NotNull(message = "分类ID不能为空")
    private Integer categoryId;

    @Schema(description = "直播间公告")
    private String notification;
}
