package blog.yuanyuan.yuanlive.user.controller;

import blog.yuanyuan.yuanlive.common.result.Result;
import blog.yuanyuan.yuanlive.user.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/user")
@Tag(name = "user-controller", description = "用户接口")
public class UserController {
    @Resource
    private SysUserService userService;

    @Operation(summary = "检查用户token, 返回用户id")
    @GetMapping("/checkToken")
    public Result<Long> checkToken(@RequestParam("token") String token) {
        return Result.success(userService.checkToken(token));
    }
}
