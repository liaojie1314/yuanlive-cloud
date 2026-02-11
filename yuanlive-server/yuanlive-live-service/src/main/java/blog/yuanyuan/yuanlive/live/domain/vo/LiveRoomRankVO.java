package blog.yuanyuan.yuanlive.live.domain.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Schema(description = "直播房间信息")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveRoomRankVO {
    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "房间ID")
    private Long id;
    @Schema(description = "房间名")
    private String title;
    @Schema(description = "主播名")
    private String anchorName;
    @Schema(description = "房间封面")
    private String coverImg;
    @Schema(description = "人气指数")
    private double hotScore;
}
