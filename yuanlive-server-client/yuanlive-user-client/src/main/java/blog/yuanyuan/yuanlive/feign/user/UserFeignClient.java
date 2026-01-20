package blog.yuanyuan.yuanlive.feign.user;

import blog.yuanyuan.yuanlive.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "user-service")
public interface UserFeignClient {

    @GetMapping("/user/checkToken")
    public Result<Long> checkToken(@RequestParam("token") String token);
}
