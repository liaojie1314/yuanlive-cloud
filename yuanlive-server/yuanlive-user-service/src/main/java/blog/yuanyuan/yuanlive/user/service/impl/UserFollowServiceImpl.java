package blog.yuanyuan.yuanlive.user.service.impl;

import blog.yuanyuan.yuanlive.common.exception.ApiException;
import blog.yuanyuan.yuanlive.common.result.Result;
import blog.yuanyuan.yuanlive.entity.live.vo.UnseenVO;
import blog.yuanyuan.yuanlive.entity.user.entity.UserStats;
import blog.yuanyuan.yuanlive.feign.live.LiveFeignClient;
import blog.yuanyuan.yuanlive.user.domain.vo.UserFollowUnseenVO;
import blog.yuanyuan.yuanlive.user.service.UserStatsService;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import blog.yuanyuan.yuanlive.entity.user.entity.SysUser;
import blog.yuanyuan.yuanlive.entity.user.entity.UserFollow;
import blog.yuanyuan.yuanlive.user.domain.dto.UserFollowDTO;
import blog.yuanyuan.yuanlive.entity.user.vo.UserFollowLivingVO;
import blog.yuanyuan.yuanlive.user.service.UserFollowService;
import blog.yuanyuan.yuanlive.user.service.SysUserService;
import blog.yuanyuan.yuanlive.user.mapper.UserFollowMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import me.ahoo.cosid.provider.IdGeneratorProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


@Service
@Slf4j
public class UserFollowServiceImpl extends ServiceImpl<UserFollowMapper, UserFollow>
    implements UserFollowService{

    @Resource
    private SysUserService sysUserService;
    @Resource
    private UserStatsService userStatsService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private IdGeneratorProvider idGeneratorProvider;
    @Resource
    private LiveFeignClient liveFeignClient;

    @Value("${redis-key.anchor-map.key}")
    private String anchorMap;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean followUser(UserFollowDTO userFollowDTO) {
        Long userId = userFollowDTO.getUserId();
        Long followUserId = userFollowDTO.getFollowUserId();

        // 1. 基础校验
        if (Objects.equals(userId, followUserId)) throw new ApiException("不能关注自己");

        // 2. 检查记录状态
        UserFollow existingFollow = lambdaQuery()
                .eq(UserFollow::getUserId, userId)
                .eq(UserFollow::getFollowUserId, followUserId)
                .one();

        boolean shouldIncr = false;

        if (existingFollow == null) {
            // 情况 A: 从未关注 -> 新增
            UserFollow newFollow = UserFollow.builder()
                    .id(idGeneratorProvider.getRequired("user-follow").generate())
                    .userId(userId)
                    .followUserId(followUserId)
                    .status(1)
                    .createTime(new Date())
                    .build();
            this.save(newFollow);
            shouldIncr = true;
        } else if (existingFollow.getStatus() == 0) {
            // 情况 B: 曾关注但已取消 -> 恢复
            existingFollow.setStatus(1);
            existingFollow.setUpdateTime(new Date());
            this.updateById(existingFollow);
            shouldIncr = true;
        }

        // 3. 同步更新 MySQL 统计表 (利用注册时已有的数据)
        if (shouldIncr) {
            // 增加自己的关注数
            userStatsService.update()
                    .setSql("following_count = following_count + 1")
                    .eq("user_id", userId)
                    .update();

            // 增加目标的粉丝数
            userStatsService.update()
                    .setSql("follower_count = follower_count + 1")
                    .eq("user_id", followUserId)
                    .update();
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean unfollowUser(Long userId, Long followUserId) {
        // 1. 查找当前是否处于“已关注”状态（status = 1）
        // 只有状态为 1 的记录才需要执行取关逻辑，避免重复扣减计数
        UserFollow existingFollow = lambdaQuery()
                .eq(UserFollow::getUserId, userId)
                .eq(UserFollow::getFollowUserId, followUserId)
                .eq(UserFollow::getStatus, 1)
                .one();

        if (existingFollow != null) {
            // 2. 更新关系表状态为取消关注 (0)
            existingFollow.setStatus(0);
            existingFollow.setUpdateTime(new Date());
            boolean isUpdated = this.updateById(existingFollow);

            if (isUpdated) {
                // 3. 原子自减：减少自己的关注数
                // gt("following_count", 0) 是为了兜底，确保不会因为异常导致数据变成负数
                userStatsService.lambdaUpdate()
                        .setSql("following_count = following_count - 1")
                        .eq(UserStats::getUserId, userId)
                        .gt(UserStats::getFollowingCount, 0)
                        .update();

                // 4. 原子自减：减少目标的粉丝数
                userStatsService.lambdaUpdate()
                        .setSql("follower_count = follower_count - 1")
                        .eq(UserStats::getUserId, followUserId)
                        .gt(UserStats::getFollowerCount, 0)
                        .update();
            }
            return isUpdated;
        }

        // 如果本来就没关注，直接返回 true，符合幂等性原则
        return true;
    }

    @Override
    public List<UserFollowLivingVO> getFollowers(Long followUserId) {
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollow::getFollowUserId, followUserId)
                .eq(UserFollow::getStatus, 1); // 只查询已关注的
        List<UserFollow> userFollows = this.list(wrapper);
        List<Long> followersIds = userFollows.stream().map(UserFollow::getUserId).toList();
        if (CollUtil.isEmpty(followersIds)) {
            return Collections.emptyList();
        }
        List<SysUser> followers = sysUserService.lambdaQuery()
                .select(SysUser::getUid, SysUser::getAvatar, SysUser::getUsername)
                .in(SysUser::getUid, followersIds).list();

        return userFollows.stream().map(follow -> {
            UserFollowLivingVO vo = UserFollowLivingVO.builder()
                    .id(follow.getId())
                    .userId(follow.getUserId())
                    .followUserId(follow.getFollowUserId())
                    .status(follow.getStatus())
                    .createTime(follow.getCreateTime())
                    .build();
            // 查询用户信息
            SysUser user = followers.stream()
                    .filter(u -> u.getUid().equals(follow.getUserId()))
                    .findFirst().orElse(null);
            if (user != null) {
                vo.setUsername(user.getUsername());
                vo.setAvatar(user.getAvatar());
            }
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<UserFollowUnseenVO> getFollowing(Long userId) {
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollow::getUserId, userId)
                .eq(UserFollow::getStatus, 1); // 只查询已关注的
        List<UserFollow> userFollows = this.list(wrapper);
        List<Long> followingIds = new ArrayList<>();
        List<Long> lastReadVideoIds = new ArrayList<>();
        for (UserFollow follow : userFollows) {
            if (follow.getUserId() != null) {
                followingIds.add(follow.getFollowUserId());
                lastReadVideoIds.add(follow.getLastReadVideoId());
            }
        }
        if (CollUtil.isEmpty(followingIds)) {
            return Collections.emptyList();
        }
        List<SysUser> following = sysUserService.lambdaQuery()
                .select(SysUser::getUid, SysUser::getAvatar, SysUser::getUsername)
                .in(SysUser::getUid, followingIds).list();
        Result<List<UnseenVO>> result = liveFeignClient.getUnseenCount(followingIds, lastReadVideoIds);
        List<UnseenVO> unseenVOS = result.getData();

        return userFollows.stream().map(follow -> {
            // 查询被关注用户的信息
            SysUser user = following.stream()
                    .filter(u -> u.getUid().equals(follow.getFollowUserId()))
                    .findFirst().orElse(null);
            UnseenVO unseenVO = unseenVOS.stream()
                    .filter(vo -> vo.getUid().equals(follow.getFollowUserId()))
                    .findFirst().orElse(new UnseenVO(follow.getUserId(), 0));
            assert user != null;
            return UserFollowUnseenVO.builder()
                    .username(user.getUsername())
                    .unseenCount(unseenVO.getCount())
                    .avatar(user.getAvatar())
                    .build();

        }).collect(Collectors.toList());
    }

    @Override
    public Boolean checkFollowing(Long userId, Long followUserId) {
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollow::getUserId, userId)
                .eq(UserFollow::getFollowUserId, followUserId)
                .eq(UserFollow::getStatus, 1); // 只查询已关注的
        return this.count(wrapper) > 0;
    }

    @Override
    public List<UserFollowLivingVO> getFollowingLive(Long userId) {
        List<UserFollow> followList = lambdaQuery()
                .eq(UserFollow::getUserId, userId)
                .eq(UserFollow::getStatus, 1).list();
        if (CollUtil.isEmpty(followList)) {
            return Collections.emptyList();
        }
        List<Object> ids = followList.stream()
                .map(user -> String.valueOf(user.getFollowUserId()))
                .collect(Collectors.toList());
        List<Object> values = stringRedisTemplate
                .opsForHash().multiGet(anchorMap, ids);
        
        // 提取正在直播的主播ID列表
        List<Long> liveAnchorIds = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            if (values.get(i) != null) {
                liveAnchorIds.add(followList.get(i).getFollowUserId());
            }
        }
        
        if (CollUtil.isEmpty(liveAnchorIds)) {
            return Collections.emptyList();
        }
        
        // 批量查询主播信息
        List<SysUser> liveAnchors = sysUserService.lambdaQuery()
                .select(SysUser::getUid, SysUser::getAvatar, SysUser::getUsername)
                .in(SysUser::getUid, liveAnchorIds).list();
        
        // 将主播信息转为Map便于快速查找
        Map<Long, SysUser> anchorMapById = liveAnchors.stream()
                .collect(Collectors.toMap(SysUser::getUid, user -> user));
        
        // 构建结果列表
        List<UserFollowLivingVO> result = new ArrayList<>();
        for (int i = 0; i < followList.size(); i++) {
            if (values.get(i) != null) { // 此用户正在直播
                Long anchorId = followList.get(i).getFollowUserId();
                SysUser anchor = anchorMapById.get(anchorId);
                if (anchor != null) {
                    UserFollowLivingVO vo = UserFollowLivingVO.builder()
                            .id(followList.get(i).getId())
                            .userId(followList.get(i).getUserId())
                            .followUserId(anchorId)
                            .createTime(followList.get(i).getCreateTime())
                            .username(anchor.getUsername())
                            .avatar(anchor.getAvatar())
                            .status(1) // 表示正在直播
                            .roomId((String) values.get(i))
                            .build();
                    result.add(vo);
                }
            }
        }
        
        return result;
    }


}




