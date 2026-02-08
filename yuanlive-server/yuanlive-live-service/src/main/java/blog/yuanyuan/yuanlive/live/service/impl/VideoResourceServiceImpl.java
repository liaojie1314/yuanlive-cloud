package blog.yuanyuan.yuanlive.live.service.impl;

import blog.yuanyuan.yuanlive.entity.live.dto.FollowUnseenQueryDTO;
import blog.yuanyuan.yuanlive.entity.live.vo.UnseenVO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import blog.yuanyuan.yuanlive.entity.live.entity.VideoResource;
import blog.yuanyuan.yuanlive.live.service.VideoResourceService;
import blog.yuanyuan.yuanlive.live.mapper.VideoResourceMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
* @author frodepu
* @description 针对表【video_resource(直播或视频记录表)】的数据库操作Service实现
* @createDate 2026-02-07 15:48:37
*/
@Service
public class VideoResourceServiceImpl extends ServiceImpl<VideoResourceMapper, VideoResource>
    implements VideoResourceService{
    @Resource
    private VideoResourceMapper videoResourceMapper;

    @Override
    public List<UnseenVO> getUnseenCount(List<Long> followingIds, List<Long> lastReadVideoIds) {
        List<FollowUnseenQueryDTO> dtos = new ArrayList<>();
        for (int i = 0; i < followingIds.size(); i++) {
            FollowUnseenQueryDTO queryDTO = new FollowUnseenQueryDTO();
            queryDTO.setFollowingId(followingIds.get(i));
            queryDTO.setLastReadVideoId(lastReadVideoIds.get(i));
            dtos.add(queryDTO);
        }
        List<UnseenVO> vos = videoResourceMapper.getUnseenCount(dtos);
        // 补齐uid为null的情况
        for (int i = 0; i < dtos.size(); i++) {
            if (vos.get(i).getUid() != null) {
                continue;
            }
            vos.get(i).setUid(dtos.get(i).getFollowingId());
        }
        return vos;
    }
}




