package blog.yuanyuan.yuanlive.live.service.impl;

import blog.yuanyuan.yuanlive.common.result.Result;
import blog.yuanyuan.yuanlive.entity.live.dto.FollowUnseenQueryDTO;
import blog.yuanyuan.yuanlive.entity.live.vo.UnseenVO;
import blog.yuanyuan.yuanlive.feign.user.UserFeignClient;
import blog.yuanyuan.yuanlive.live.domain.vo.VideoVO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import blog.yuanyuan.yuanlive.entity.live.entity.VideoResource;
import blog.yuanyuan.yuanlive.live.service.VideoResourceService;
import blog.yuanyuan.yuanlive.live.mapper.VideoResourceMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
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
    @Resource
    private UserFeignClient userFeignClient;

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

    @Override
    public List<VideoVO> getVideoByUid(Long uid) {
        List<VideoResource> videos = lambdaQuery().eq(VideoResource::getUserId, uid)
                .list();
        videos.sort(Comparator.comparing(VideoResource::getId).reversed());
        Result<Long> result = userFeignClient.getLastReadVideoId(uid);
        Long lastReadVideoId = result.getData();
        return videos.stream().map(video -> {
            VideoVO videoVO = new VideoVO();
            videoVO.setId(video.getId());
            videoVO.setVideoUrl(video.getVideoUrl());
            videoVO.setCoverUrl(video.getCoverUrl());
            videoVO.setLikeCount(video.getLikeCount());
            videoVO.setCommentCount(video.getCommentCount());
            videoVO.setShareCount(video.getShareCount());
            videoVO.setCollectCount(video.getCollectCount());
            videoVO.setWatched(video.getId() <= lastReadVideoId);
            videoVO.setType(video.getType());
            return videoVO;
        }).toList();
    }
}




