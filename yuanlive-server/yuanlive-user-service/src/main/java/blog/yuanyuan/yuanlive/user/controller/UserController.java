package blog.yuanyuan.yuanlive.user.controller;

import blog.yuanyuan.yuanlive.common.result.Result;
import blog.yuanyuan.yuanlive.entity.user.entity.SysUser;
import blog.yuanyuan.yuanlive.user.domain.vo.UserVO;
import blog.yuanyuan.yuanlive.user.service.SysUserService;
import cn.hutool.core.bean.BeanUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/user")
@Tag(name = "用户接口")
public class UserController {
    @Resource
    private SysUserService userService;

    @Operation(summary = "检查用户token, 返回用户id")
    @GetMapping("/checkToken")
    public Result<SysUser> checkToken(@RequestParam("token") String token) {
        return Result.success(userService.checkToken(token));
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/getUserInfo")
    public Result<UserVO> getUserInfo() {
        return Result.success(userService.getUserInfo());
    }

    @GetMapping("/getInfo/{userId}")
    @Operation(summary = "根据ID获取用户详情")
    public Result<SysUser> getInfo(@PathVariable("userId") Long userId) {
        UserVO userVO = userService.getUserById(userId);
        SysUser user = BeanUtil.copyProperties(userVO, SysUser.class);
        return Result.success(user);
    }
}
