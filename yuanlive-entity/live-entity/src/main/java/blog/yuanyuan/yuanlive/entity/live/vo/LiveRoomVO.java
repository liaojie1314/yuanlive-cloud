package blog.yuanyuan.yuanlive.entity.live.vo;

import blog.yuanyuan.yuanlive.entity.live.entity.LiveRoom;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 直播间VO
 */
@Data
@Schema(description = "直播间VO")
public class LiveRoomVO extends LiveRoom {
    @Schema(description = "主播名称")
    private String anchorName;

    @Schema(description = "分类名称")
    private String categoryName;

}
