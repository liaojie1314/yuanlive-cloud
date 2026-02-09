package blog.yuanyuan.yuanlive.user.controller;

import blog.yuanyuan.yuanlive.common.result.Result;
import blog.yuanyuan.yuanlive.user.domain.dto.UserFollowDTO;
import blog.yuanyuan.yuanlive.entity.user.vo.UserFollowLivingVO;
import blog.yuanyuan.yuanlive.user.domain.vo.UserFollowUnseenVO;
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
    public Result<List<UserFollowLivingVO>> getFollowers(@PathVariable("followUserId") Long followUserId) {
        List<UserFollowLivingVO> followers = userFollowService.getFollowers(followUserId);
        return Result.success(followers);
    }

    @GetMapping("/following")
    @Operation(summary = "获取某人的关注列表")
    public Result<List<UserFollowUnseenVO>> getFollowing() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<UserFollowUnseenVO> following = userFollowService.getFollowing(userId);
        return Result.success(following);
    }

    @GetMapping("/following/live")
    @Operation(summary = "获取当前用户正在直播的关注列表")
    public Result<List<UserFollowLivingVO>> getFollowingLive() {
        List<UserFollowLivingVO> followingLive = userFollowService.getFollowingLive(StpUtil.getLoginIdAsLong());
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
