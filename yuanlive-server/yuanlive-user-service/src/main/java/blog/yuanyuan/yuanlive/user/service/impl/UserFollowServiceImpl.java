package blog.yuanyuan.yuanlive.user.service.impl;

import blog.yuanyuan.yuanlive.common.exception.ApiException;
import blog.yuanyuan.yuanlive.user.domain.vo.UserVO;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import blog.yuanyuan.yuanlive.entity.user.entity.SysUser;
import blog.yuanyuan.yuanlive.entity.user.entity.UserFollow;
import blog.yuanyuan.yuanlive.user.domain.dto.UserFollowDTO;
import blog.yuanyuan.yuanlive.entity.user.vo.UserFollowVO;
import blog.yuanyuan.yuanlive.user.service.UserFollowService;
import blog.yuanyuan.yuanlive.user.service.SysUserService;
import blog.yuanyuan.yuanlive.user.mapper.UserFollowMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
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
    private StringRedisTemplate stringRedisTemplate;

    @Value("${redis-key.anchor-map.key}")
    private String anchorMap;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean followUser(UserFollowDTO userFollowDTO) {
        Long userId = userFollowDTO.getUserId();
        Long followUserId = userFollowDTO.getFollowUserId();
        UserVO following = sysUserService.getUserById(followUserId);
        if (Objects.equals(userId, followUserId)) {
            throw new ApiException("不能关注自己");
        }
        if (following == null || following.getStatus() != 1) {
            throw new ApiException("用户不存在或已停用");
        }
        // 检查是否已经关注
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollow::getUserId, userId)
                .eq(UserFollow::getFollowUserId, followUserId);
        UserFollow existingFollow = CollUtil.getFirst(list(wrapper));

        if (existingFollow != null) {
            // 如果是取消状态，则更新为关注状态
            if (existingFollow.getStatus() == 0) {
                existingFollow.setStatus(1);
                existingFollow.setUpdateTime(new Date());
                return this.updateById(existingFollow);
            } else {
                // 已经关注了
                return true;
            }
        } else {
            // 创建新的关注记录
            UserFollow userFollow = UserFollow.builder()
                    .userId(userId)
                    .followUserId(followUserId)
                    .status(1)
                    .build();
            return this.save(userFollow);
        }
    }

    @Override
    @Transactional
    public Boolean unfollowUser(Long userId, Long followUserId) {
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollow::getUserId, userId)
                .eq(UserFollow::getFollowUserId, followUserId);
        UserFollow existingFollow = CollUtil.getFirst(list(wrapper));
        if (existingFollow != null && existingFollow.getStatus() == 1) {
            // 更新状态为取消关注
            existingFollow.setStatus(0);
            return this.updateById(existingFollow);
        }
        return true;
    }

    @Override
    public List<UserFollowVO> getFollowers(Long followUserId) {
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
            UserFollowVO vo = UserFollowVO.builder()
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
    public List<UserFollowVO> getFollowing(Long userId) {
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollow::getUserId, userId)
                .eq(UserFollow::getStatus, 1); // 只查询已关注的
        List<UserFollow> userFollows = this.list(wrapper);

        List<Long> followingIds = userFollows.stream().map(UserFollow::getFollowUserId).toList();
        if (CollUtil.isEmpty(followingIds)) {
            return Collections.emptyList();
        }
        List<SysUser> following = sysUserService.lambdaQuery()
                .select(SysUser::getUid, SysUser::getAvatar, SysUser::getUsername)
                .in(SysUser::getUid, followingIds).list();

        return userFollows.stream().map(follow -> {
            UserFollowVO vo = UserFollowVO.builder()
                    .id(follow.getId())
                    .userId(follow.getUserId())
                    .followUserId(follow.getFollowUserId())
                    .status(follow.getStatus())
                    .createTime(follow.getCreateTime())
                    .build();
            // 查询被关注用户的信息
            SysUser user = following.stream()
                    .filter(u -> u.getUid().equals(follow.getFollowUserId()))
                    .findFirst().orElse(null);
            if (user != null) {
                vo.setUsername(user.getUsername());
                vo.setAvatar(user.getAvatar());
            }
            return vo;
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
    public List<UserFollowVO> getFollowingLive(Long userId) {
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
        List<UserFollowVO> result = new ArrayList<>();
        for (int i = 0; i < followList.size(); i++) {
            if (values.get(i) != null) { // 此用户正在直播
                Long anchorId = followList.get(i).getFollowUserId();
                SysUser anchor = anchorMapById.get(anchorId);
                if (anchor != null) {
                    UserFollowVO vo = UserFollowVO.builder()
                            .id(followList.get(i).getId())
                            .userId(followList.get(i).getUserId())
                            .followUserId(anchorId)
                            .createTime(followList.get(i).getCreateTime())
                            .username(anchor.getUsername())
                            .avatar(anchor.getAvatar())
                            .status(1) // 表示正在直播
                            .build();
                    result.add(vo);
                }
            }
        }
        
        return result;
    }
}




