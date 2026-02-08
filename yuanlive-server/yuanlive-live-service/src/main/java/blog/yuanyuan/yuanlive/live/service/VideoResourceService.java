package blog.yuanyuan.yuanlive.live.service;

import blog.yuanyuan.yuanlive.entity.live.entity.VideoResource;
import blog.yuanyuan.yuanlive.entity.live.vo.UnseenVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author frodepu
* @description 针对表【video_resource(直播或视频记录表)】的数据库操作Service
* @createDate 2026-02-07 15:48:37
*/
public interface VideoResourceService extends IService<VideoResource> {

    List<UnseenVO> getUnseenCount(List<Long> followingIds, List<Long> lastReadVideoIds);
}
