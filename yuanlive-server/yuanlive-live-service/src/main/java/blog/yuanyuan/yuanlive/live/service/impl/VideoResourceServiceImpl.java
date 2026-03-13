package blog.yuanyuan.yuanlive.live.service.impl;

import blog.yuanyuan.yuanlive.common.enums.BehaviorType;
import blog.yuanyuan.yuanlive.common.exception.ApiException;
import blog.yuanyuan.yuanlive.common.result.Result;
import blog.yuanyuan.yuanlive.common.result.ResultPage;
import blog.yuanyuan.yuanlive.common.util.CacheUtil;
import blog.yuanyuan.yuanlive.common.util.InterestUtil;
import blog.yuanyuan.yuanlive.entity.live.dto.FollowUnseenQueryDTO;
import blog.yuanyuan.yuanlive.entity.live.entity.UserVideoInteract;
import blog.yuanyuan.yuanlive.entity.live.vo.UnseenVO;
import blog.yuanyuan.yuanlive.feign.user.UserFeignClient;
import blog.yuanyuan.yuanlive.live.domain.dto.VideoPageQueryDTO;
import blog.yuanyuan.yuanlive.entity.live.vo.VideoVO;
import blog.yuanyuan.yuanlive.live.domain.vo.LikeVO;
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
import java.util.concurrent.TimeUnit;

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
    @Resource
    private CacheUtil cacheUtil;

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
                rebuildCache(id, uid, interactKey);
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

    @Override
    public Result<LikeVO> cancelLike(Long id, Long uid, BehaviorType type) {
        if (type != BehaviorType.LIKE) {
            return Result.failed("不支持的状态行为");
        }

        Optional<VideoResource> resource = lambdaQuery().eq(VideoResource::getId, id).oneOpt();
        if (resource.isEmpty()) {
            return Result.failed("视频不存在");
        }

        String interactKey = videoInteractPrefix + uid + ":" + id;
        String countKey = "yuanlive:video:likeCount:" + id;

        // 2. 缓存重建：如果位图 Key 不存在，从 DB 恢复
        if (Boolean.FALSE.equals(stringRedisTemplate.hasKey(interactKey))) {
            rebuildCache(id, uid, interactKey);
        }

        // 3. 执行位图操作：原子性地将对应位设为 0，并获取旧值
        // isWasDone 为 true 表示之前确实点过赞
        Boolean isWasDone = stringRedisTemplate.opsForValue().setBit(interactKey, type.getBitOffset(), false);

        // 刷新位图过期时间
        stringRedisTemplate.expire(interactKey, videoInteractTtl);

        // 4. 判断是否需要执行后续撤回逻辑（幂等判断）
        if (Boolean.TRUE.equals(isWasDone)) {
            // --- 确有其赞，执行撤回逻辑 ---

            // A. 兴趣权重与搜索记录扣减
            VideoResource videoResource = resource.get();
            interestUtil.reduceUserInterest(uid, videoResource.getCategoryId(), type);
            interestUtil.reduceSearch(videoResource.getTitle(), type);

            // B. MySQL 持久化：将 has_liked 设为 0
            interactMapper.undoAction(uid, id, type.getSqlColumn());

            // C. 计数器处理：如果 Redis 中存在计数缓存，执行递减
            String countStr = stringRedisTemplate.opsForValue().get(countKey);
            if (countStr != null) {
                stringRedisTemplate.opsForValue().decrement(countKey);
            }
        }

        // 5. 获取并返回最新的点赞数（手动实现缓存旁路逻辑）
        Long count;
        String countStr = stringRedisTemplate.opsForValue().get(countKey);

        if (countStr != null) {
            count = Long.parseLong(countStr);
        } else {
            // 缓存失效，查库回填
            // 由于上面已经执行了 undoAction，此时库里的数据是最新的
            count = interactService.lambdaQuery()
                    .eq(UserVideoInteract::getVideoId, id)
                    .eq(UserVideoInteract::getHasLiked, 1)
                    .count();
            stringRedisTemplate.opsForValue().set(countKey, String.valueOf(count), 1, TimeUnit.DAYS);
        }

        return Result.success(LikeVO.builder()
                .isLiked(false) // 撤回成功，当前状态肯定为 false
                .count(count)
                .build());
    }

    @Override
    public Result<LikeVO> likeVideo(Long id, long uid, BehaviorType behaviorType) {
        Result<String> result = operateVideo(id, uid, behaviorType);
        if (!result.isSuccess()) {
            throw new ApiException(result.getMsg());
        }
        String countKey = "yuanlive:video:likeCount:" + id;
        String countStr = stringRedisTemplate.opsForValue().get(countKey);
        Long count;
        boolean isNewAction = "操作成功".equals(result.getData());
        if (countStr != null) {
            count = Long.parseLong(countStr);
            if (isNewAction) {
                count = stringRedisTemplate.opsForValue().increment(countKey);
            }
        } else {
            count = interactService.lambdaQuery()
                    .eq(UserVideoInteract::getVideoId, id)
                    .eq(UserVideoInteract::getHasLiked, 1)
                    .count();
            stringRedisTemplate.opsForValue().set(countKey, String.valueOf(count), 1, TimeUnit.DAYS);
        }
        LikeVO likeVO = LikeVO.builder()
                .isLiked(true)
                .count(count).build();
        return Result.success(likeVO);
    }

    private void rebuildCache(Long id, Long uid, String interactKey) {
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
}




