package blog.yuanyuan.yuanlive.entity.live.vo;

import blog.yuanyuan.yuanlive.entity.live.entity.VideoResource;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "搜索结果信息")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchVO {
    @Schema(description = "视频信息")
    private VideoResource video;
    @Schema(description = "直播房间信息")
    private LiveRoomRankVO liveRoom;
    @Schema(description = "是否是房间信息")
    private boolean checkRoom;
}
