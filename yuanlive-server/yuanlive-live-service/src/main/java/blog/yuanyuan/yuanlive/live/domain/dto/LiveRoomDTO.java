package blog.yuanyuan.yuanlive.live.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.apache.ibatis.annotations.Update;

/**
 * 直播间DTO
 */
@Data
@Schema(description = "直播间DTO")
public class LiveRoomDTO {
    @Schema(description = "直播间ID(修改时必传)")
    @NotNull(message = "直播间ID不能为空", groups = Update.class)
    private Long id;

    @Schema(description = "直播间标题")
    @NotBlank(message = "直播间标题不能为空")
    private String title;

    @Schema(description = "直播间封面图URL")
    private String coverImg;

    @Schema(description = "分类ID")
    @NotNull(message = "分类ID不能为空")
    private Integer categoryId;
}
