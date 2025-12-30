package blog.yuanyuan.yuanlive.user.controller;

import blog.yuanyuan.yuanlive.user.domain.dto.LoginDTO;
import blog.yuanyuan.yuanlive.user.domain.dto.RefreshDTO;
import blog.yuanyuan.yuanlive.user.domain.dto.RegisterDTO;
import blog.yuanyuan.yuanlive.user.domain.vo.LoginVO;
import blog.yuanyuan.yuanlive.user.domain.vo.RefreshVO;
import blog.yuanyuan.yuanlive.user.properties.RegisterProperties;
import blog.yuanyuan.yuanlive.user.service.AuthService;
import cn.dev33.satoken.annotation.SaCheckRole;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Email;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import result.Result;

@RestController
@RequestMapping("/auth")
@Validated
public class AuthController {
    @Resource
    private AuthService authService;


    @PostMapping("/getCode")
    public Result<String> getCode(@RequestParam("email") @Email(message = "格式错误") String email) {
        String code = authService.getCode(email);
        if (!code.isEmpty()) {
            return Result.success(code);
        }
        return Result.failed("请稍后重试");
    }

    @PostMapping("/register")
    public Result<String> register(@RequestBody @Validated RegisterDTO registerDTO) {
        if (authService.register(registerDTO)) {
            return Result.success(null, "注册成功");
        }
        return Result.failed("验证码错误");
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody @Validated LoginDTO loginDTO) {
        return Result.success(authService.login(loginDTO));
    }

    @PostMapping("/refreshToken")
    public Result<RefreshVO> refreshToken(@RequestBody RefreshDTO refreshDTO) {
        return Result.success(authService.refreshToken(refreshDTO.getRefreshToken()));
    }

    @GetMapping("/test")
//    @SaCheckRole("test")
    public Result<String> test() {
        return Result.success("测试成功");
    }

}
