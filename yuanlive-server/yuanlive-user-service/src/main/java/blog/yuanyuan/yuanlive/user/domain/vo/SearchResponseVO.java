package blog.yuanyuan.yuanlive.user.domain.vo;

import blog.yuanyuan.yuanlive.entity.live.entity.VideoResource;
import blog.yuanyuan.yuanlive.entity.live.vo.LiveRoomRankVO;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SearchResponseVO {
    private List<VideoResource> videoResources;
    private List<LiveRoomRankVO> liveRooms;
}
