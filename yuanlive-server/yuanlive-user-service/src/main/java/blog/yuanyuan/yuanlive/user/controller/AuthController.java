package blog.yuanyuan.yuanlive.user.controller;

import blog.yuanyuan.yuanlive.user.domain.dto.LoginDTO;
import blog.yuanyuan.yuanlive.user.domain.dto.RefreshDTO;
import blog.yuanyuan.yuanlive.user.domain.dto.RegisterDTO;
import blog.yuanyuan.yuanlive.user.domain.vo.LoginVO;
import blog.yuanyuan.yuanlive.user.domain.vo.QrCodeCheckVO;
import blog.yuanyuan.yuanlive.user.domain.vo.QrCodeVO;
import blog.yuanyuan.yuanlive.user.domain.vo.RefreshVO;
import blog.yuanyuan.yuanlive.user.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Email;
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
    @Operation(summary = "获取验证码")
    public Result<String> getCode(@RequestParam("email") @Email(message = "格式错误") String email) {
        String code = authService.getCode(email);
        if (!code.isEmpty()) {
            return Result.success(code);
        }
        return Result.failed("请稍后重试");
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
