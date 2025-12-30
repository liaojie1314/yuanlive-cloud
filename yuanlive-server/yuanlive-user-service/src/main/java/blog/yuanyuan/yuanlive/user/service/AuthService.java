package blog.yuanyuan.yuanlive.user.service;

import blog.yuanyuan.yuanlive.user.domain.dto.LoginDTO;
import blog.yuanyuan.yuanlive.user.domain.dto.RegisterDTO;
import blog.yuanyuan.yuanlive.user.domain.vo.LoginVO;
import blog.yuanyuan.yuanlive.user.domain.vo.QrCodeCheckVO;
import blog.yuanyuan.yuanlive.user.domain.vo.QrCodeVO;
import blog.yuanyuan.yuanlive.user.domain.vo.RefreshVO;
import jakarta.validation.constraints.Email;
import result.Result;

import java.util.Map;

public interface AuthService {
    String getCode(@Email(message = "格式错误") String email);

    boolean register(RegisterDTO registerDTO);

    LoginVO login(LoginDTO loginDTO);

    RefreshVO refreshToken(String refreshToken);

    QrCodeVO initQrCode();

    QrCodeCheckVO checkQrCodeStatus(String uuid);

    Result scanQrCode(String uuid);

    Result<String> confirmLogin(String uuid);
}
