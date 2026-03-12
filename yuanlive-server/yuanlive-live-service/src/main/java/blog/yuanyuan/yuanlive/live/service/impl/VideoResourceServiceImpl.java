package blog.yuanyuan.yuanlive.live.service.impl;

import blog.yuanyuan.yuanlive.common.enums.BehaviorType;
import blog.yuanyuan.yuanlive.common.result.Result;
import blog.yuanyuan.yuanlive.common.result.ResultPage;
import blog.yuanyuan.yuanlive.common.util.InterestUtil;
import blog.yuanyuan.yuanlive.entity.live.dto.FollowUnseenQueryDTO;
import blog.yuanyuan.yuanlive.entity.live.entity.UserVideoInteract;
import blog.yuanyuan.yuanlive.entity.live.vo.UnseenVO;
import blog.yuanyuan.yuanlive.feign.user.UserFeignClient;
import blog.yuanyuan.yuanlive.live.domain.dto.VideoPageQueryDTO;
import blog.yuanyuan.yuanlive.entity.live.vo.VideoVO;
import blog.yuanyuan.yuanlive.live.mapper.UserVideoInteractMapper;
import blog.yuanyuan.yuanlive.live.service.UserVideoInteractService;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import blog.yuanyuan.yuanlive.entity.live.entity.VideoResource;
import blog.yuanyuan.yuanlive.live.service.VideoResourceService;
import blog.yuanyuan.yuanlive.live.mapper.VideoResourceMapper;
import jakarta.annotation.Resource;
import me.ahoo.cosid.provider.IdGeneratorProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    private UserVideoInteractMapper interactMapper;
    @Resource
    private UserVideoInteractService interactService;
    @Resource
    private UserFeignClient userFeignClient;
    @Resource
    private InterestUtil interestUtil;
    @Resource
    private IdGeneratorProvider idGeneratorProvider;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Value("${redis-key.video-interact.prefix}")
    private String videoInteractPrefix;
    @Value("${redis-key.video-interact.ttl}")
    private Duration videoInteractTtl;

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

    @Override
    public Result<String> operateVideo(Long id, Long uid, BehaviorType type) {
        if (!type.isStateful()) {
            return Result.failed("不支持的状态行为");
        }
        Optional<VideoResource> resource = lambdaQuery().eq(VideoResource::getId, id).oneOpt();
        if (resource.isPresent()) {
            String interactKey = videoInteractPrefix + uid + ":" + id;
            if (!stringRedisTemplate.hasKey(interactKey)) {
                // 没有找到key可能是过期了，去mysql中获取
                Optional<UserVideoInteract> optional = interactService.lambdaQuery()
                        .eq(UserVideoInteract::getUserId, uid)
                        .eq(UserVideoInteract::getVideoId, id).oneOpt();
                if (optional.isPresent()) {
                    UserVideoInteract interact = optional.get();
                    boolean index0 = interact.getHasLiked() == 1;
                    boolean index1 = interact.getHasShared() == 1;
                    boolean index2 = interact.getHasRecommended() == 1;
                    boolean index3 = interact.getHasUnliked() == 1;
                    stringRedisTemplate.executePipelined(new SessionCallback<Object>() {
                        @Override
                        public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {
                            operations.opsForValue().setBit((K) interactKey, 0, index0);
                            operations.opsForValue().setBit((K) interactKey, 1, index1);
                            operations.opsForValue().setBit((K) interactKey, 2, index2);
                            operations.opsForValue().setBit((K) interactKey, 3, index3);
                            return null;
                        }
                    });
                }
            }
            Boolean isSuccess = stringRedisTemplate.opsForValue()
                    .setBit(interactKey, type.getBitOffset(), true);
            stringRedisTemplate.expire(interactKey, videoInteractTtl);
            if (Boolean.TRUE.equals(isSuccess)) {
                return Result.success("已执行过该行为, 无需重复操作");
            }
            VideoResource videoResource = resource.get();
            interestUtil.recordUserInterest(uid, videoResource.getCategoryId(), type);
            interestUtil.recordSearch(videoResource.getTitle(), type);
            // 记录用户操作
            long interactId = idGeneratorProvider.getRequired("interact").generate();
            interactMapper.upsertAction(interactId, uid, id, type.getSqlColumn());
            return Result.success("操作成功");
        }
        return Result.failed("操作失败");
    }
}




