package blog.yuanyuan.yuanlive.feign.user;

import blog.yuanyuan.yuanlive.common.result.Result;
import blog.yuanyuan.yuanlive.entity.user.entity.SysUser;
import blog.yuanyuan.yuanlive.entity.user.vo.UserFollowVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(value = "user-service")
public interface UserFeignClient {

    @GetMapping("/user/checkToken")
    public Result<SysUser> checkToken(@RequestParam("token") String token);

    @GetMapping("/user/getInfo/{userId}")
    public Result<SysUser> getInfo(@PathVariable("userId") Long userId);

    @GetMapping("/follow/followers/{followUserId}")
    public Result<List<UserFollowVO>> getFollowers(@PathVariable("followUserId") Long followUserId);
}
