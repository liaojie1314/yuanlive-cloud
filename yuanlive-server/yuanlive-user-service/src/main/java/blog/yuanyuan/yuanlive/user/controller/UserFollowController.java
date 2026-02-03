package blog.yuanyuan.yuanlive.user.controller;

import blog.yuanyuan.yuanlive.common.result.Result;
import blog.yuanyuan.yuanlive.user.domain.dto.UserFollowDTO;
import blog.yuanyuan.yuanlive.entity.user.vo.UserFollowVO;
import blog.yuanyuan.yuanlive.user.service.UserFollowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "用户关注主播接口")
@RequestMapping("/follow")
public class UserFollowController {

    @Autowired
    private UserFollowService userFollowService;

    @PostMapping("/follow")
    @Operation(summary = "关注指定用户")
    public Result<Boolean> followUser(@RequestBody UserFollowDTO userFollowDTO) {
        userFollowDTO.setUserId(StpUtil.getLoginIdAsLong());
        Boolean result = userFollowService.followUser(userFollowDTO);
        return Result.success(result);
    }

    @DeleteMapping("/unfollow/{followUserId}")
    @Operation(summary = "取消关注指定用户")
    public Result<Boolean> unfollowUser(@PathVariable("followUserId") Long followUserId) {
        Long userId = StpUtil.getLoginIdAsLong();
        Boolean result = userFollowService.unfollowUser(userId, followUserId);
        return Result.success(result);
    }

    @GetMapping("/followers/{followUserId}")
    @Operation(summary = "获取某人的粉丝列表")
    public Result<List<UserFollowVO>> getFollowers(@PathVariable("followUserId") Long followUserId) {
        List<UserFollowVO> followers = userFollowService.getFollowers(followUserId);
        return Result.success(followers);
    }

    @GetMapping("/following/{userId}")
    @Operation(summary = "获取某人的关注列表")
    public Result<List<UserFollowVO>> getFollowing(@PathVariable("userId") Long userId) {
        List<UserFollowVO> following = userFollowService.getFollowing(userId);
        return Result.success(following);
    }

    @GetMapping("/following/live/{userId}")
    @Operation(summary = "获取某人正在直播的关注列表")
    public Result<List<UserFollowVO>> getFollowingLive(@PathVariable("userId") Long userId) {
        List<UserFollowVO> followingLive = userFollowService.getFollowingLive(userId);
        return Result.success(followingLive);
    }

    @GetMapping("/check/{followUserId}")
    @Operation(summary = "检查当前用户是否关注了指定用户")
    public Result<Boolean> checkFollowing(@PathVariable("followUserId") Long followUserId) {
        Long userId = StpUtil.getLoginIdAsLong();
        Boolean isFollowing = userFollowService.checkFollowing(userId, followUserId);
        return Result.success(isFollowing);
    }
}
