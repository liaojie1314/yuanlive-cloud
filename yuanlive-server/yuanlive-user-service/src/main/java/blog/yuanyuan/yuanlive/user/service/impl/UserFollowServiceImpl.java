package blog.yuanyuan.yuanlive.user.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import blog.yuanyuan.yuanlive.entity.user.entity.SysUser;
import blog.yuanyuan.yuanlive.entity.user.entity.UserFollow;
import blog.yuanyuan.yuanlive.user.domain.dto.UserFollowDTO;
import blog.yuanyuan.yuanlive.user.domain.vo.UserFollowVO;
import blog.yuanyuan.yuanlive.user.service.UserFollowService;
import blog.yuanyuan.yuanlive.user.service.SysUserService;
import blog.yuanyuan.yuanlive.user.mapper.UserFollowMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class UserFollowServiceImpl extends ServiceImpl<UserFollowMapper, UserFollow>
    implements UserFollowService{

    @Resource
    private SysUserService sysUserService;

    @Override
    @Transactional
    public Boolean followUser(UserFollowDTO userFollowDTO) {
        Long userId = userFollowDTO.getUserId();
        Long followUserId = userFollowDTO.getFollowUserId();

        // 检查是否已经关注
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollow::getUserId, userId)
                .eq(UserFollow::getFollowUserId, followUserId);
        UserFollow existingFollow = this.list(wrapper).get(0);

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
        UserFollow existingFollow = this.list(wrapper).get(0);
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
        List<SysUser> followers = sysUserService.lambdaQuery()
                .select(SysUser::getAvatar, SysUser::getUsername)
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

        List<Long> followingIds = userFollows.stream().map(UserFollow::getUserId).toList();
        List<SysUser> following = sysUserService.lambdaQuery()
                .select(SysUser::getAvatar, SysUser::getUsername)
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
}




