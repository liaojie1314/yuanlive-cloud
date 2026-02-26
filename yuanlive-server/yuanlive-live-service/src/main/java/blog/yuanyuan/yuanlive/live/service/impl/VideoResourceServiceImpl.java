package blog.yuanyuan.yuanlive.live.service.impl;

import blog.yuanyuan.yuanlive.common.result.Result;
import blog.yuanyuan.yuanlive.common.result.ResultPage;
import blog.yuanyuan.yuanlive.entity.live.dto.FollowUnseenQueryDTO;
import blog.yuanyuan.yuanlive.entity.live.vo.UnseenVO;
import blog.yuanyuan.yuanlive.feign.user.UserFeignClient;
import blog.yuanyuan.yuanlive.live.domain.dto.VideoPageQueryDTO;
import blog.yuanyuan.yuanlive.live.domain.vo.VideoVO;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
    public ResultPage<VideoVO> getVideoByUidWithPaging(VideoPageQueryDTO queryDTO) {
        // 创建分页对象
        Page<VideoResource> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        // 查询指定用户的视频资源，按ID降序排列（最新的在前）
        Page<VideoResource> videoPage = lambdaQuery()
                .eq(VideoResource::getUserId, queryDTO.getUid())
                .orderByDesc(VideoResource::getId)
                .page(page);

        // 获取用户最后观看的视频ID
        Result<Long> result = userFeignClient.getLastReadVideoId(queryDTO.getUid());
        Long lastReadVideoId = result.getData();

        // 将VideoResource转换为VideoVO，并设置观看状态
        List<VideoVO> videoVOList = videoPage.getRecords().stream().map(video -> {
            VideoVO videoVO = new VideoVO();
            BeanUtil.copyProperties(video, videoVO);
            videoVO.setWatched(video.getId() <= lastReadVideoId);
            return videoVO;
        }).toList();

        // 构建分页结果
        ResultPage<VideoVO> resultPage = new ResultPage<>();
        resultPage.setList(videoVOList);
        resultPage.setTotal(videoPage.getTotal());
        resultPage.setPageSize(videoPage.getSize());
        resultPage.setCurrentPage(videoPage.getCurrent());

        return resultPage;
    }
}




