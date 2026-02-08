package blog.yuanyuan.yuanlive.live.mapper;

import blog.yuanyuan.yuanlive.entity.live.dto.FollowUnseenQueryDTO;
import blog.yuanyuan.yuanlive.entity.live.entity.VideoResource;
import blog.yuanyuan.yuanlive.entity.live.vo.UnseenVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author frodepu
* @description 针对表【video_resource(直播或视频记录表)】的数据库操作Mapper
* @createDate 2026-02-07 15:48:37
* @Entity blog.yuanyuan.yuanlive.live.entity.VideoResource
*/
public interface VideoResourceMapper extends BaseMapper<VideoResource> {

    List<UnseenVO> getUnseenCount(@Param("dtos") List<FollowUnseenQueryDTO> dtos);
}




