package blog.yuanyuan.yuanlive.user.controller;

import blog.yuanyuan.yuanlive.user.domain.dto.*;
import blog.yuanyuan.yuanlive.user.domain.vo.LoginVO;
import blog.yuanyuan.yuanlive.user.domain.vo.QrCodeCheckVO;
import blog.yuanyuan.yuanlive.user.domain.vo.QrCodeVO;
import blog.yuanyuan.yuanlive.user.domain.vo.RefreshVO;
import blog.yuanyuan.yuanlive.user.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import blog.yuanyuan.yuanlive.common.result.Result;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Resource
    private AuthService authService;


    @PostMapping("/getCode")
    @Operation(summary = "获取验证码")
    public Result<String> getCode(@RequestBody @Validated CodeDTO codeDTO) {
        authService.getCode(codeDTO);
        return Result.success(null, "验证码已发送");
    }

    @PostMapping("/register")
    @Operation(summary = "注册")
    public Result<String> register(@RequestBody @Validated RegisterDTO registerDTO) {
        if (authService.register(registerDTO)) {
            return Result.success(null, "注册成功");
        }
        return Result.failed("验证码错误");
    }

    @PostMapping("/login")
    @Operation(summary = "账号名密码登录")
    public Result<LoginVO> login(@RequestBody @Validated LoginDTO loginDTO) {
        return Result.success(authService.login(loginDTO));
    }

    @PostMapping("/forgetPassword")
    @Operation(summary = "忘记密码")
    public Result<String> forgetPassword(@RequestBody @Validated ForgetPassDTO forgetPassDTO) {
        return authService.forgetPassword(forgetPassDTO);
    }

    @PostMapping("/logout")
    @Operation(summary = "登出")
    public Result logout() {
        return authService.logout();
    }

    @PostMapping("/refreshToken")
    @Operation(summary = "刷新令牌")
    public Result<RefreshVO> refreshToken(@RequestBody RefreshDTO refreshDTO) {
        return Result.success(authService.refreshToken(refreshDTO.getRefreshToken()));
    }

    @GetMapping("/qrcode/init")
    @Operation(summary = "获取二维码")
    public Result<QrCodeVO> initQrCode() {
        return Result.success(authService.initQrCode());
    }

    @GetMapping("/qrcode/check")
    @Operation(summary = "检查二维码状态")
    public Result<QrCodeCheckVO> checkQrCodeStatus(@RequestParam("uuid") String uuid) {
        return Result.success(authService.checkQrCodeStatus(uuid));
    }

    @PostMapping("/qrcode/scan")
    @Operation(summary = "扫码登录")
    public Result scanQrCode(@RequestParam("uuid") String uuid) {
        return authService.scanQrCode(uuid);
    }

    @PostMapping("/qrcode/confirm")
    @Operation(summary = "确认扫码登录")
    public Result<String> confirmLogin(@RequestParam("uuid") String uuid) {
        return authService.confirmLogin(uuid);
    }
}
