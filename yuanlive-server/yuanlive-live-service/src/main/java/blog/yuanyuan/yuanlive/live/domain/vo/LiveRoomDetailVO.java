package blog.yuanyuan.yuanlive.live.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 直播间详情VO
 */
@Data
@Schema(description = "直播间详情VO")
public class LiveRoomDetailVO extends LiveRoomVO {
    @Schema(description = "主播名称")
    private String anchorName;

    @Schema(description = "主播头像")
    private String anchorAvatar;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "推流地址")
    private String pushUrl;

    @Schema(description = "拉流地址")
    private String pullUrl;
}
