package blog.yuanyuan.yuanlive.user.service;

import blog.yuanyuan.yuanlive.user.domain.dto.CodeDTO;
import blog.yuanyuan.yuanlive.user.domain.dto.ForgetPassDTO;
import blog.yuanyuan.yuanlive.user.domain.dto.LoginDTO;
import blog.yuanyuan.yuanlive.user.domain.dto.RegisterDTO;
import blog.yuanyuan.yuanlive.user.domain.vo.LoginVO;
import blog.yuanyuan.yuanlive.user.domain.vo.QrCodeCheckVO;
import blog.yuanyuan.yuanlive.user.domain.vo.QrCodeVO;
import blog.yuanyuan.yuanlive.user.domain.vo.RefreshVO;
import blog.yuanyuan.yuanlive.common.result.Result;

public interface AuthService {
    void getCode(CodeDTO codeDTO);

    boolean register(RegisterDTO registerDTO);

    LoginVO login(LoginDTO loginDTO);

    RefreshVO refreshToken(String refreshToken);

    QrCodeVO initQrCode(String deviceID);

    QrCodeCheckVO checkQrCodeStatus(String uuid);

    Result scanQrCode(String uuid);

    Result<String> confirmLogin(String uuid);

    Result<String> logout();

    Result<String> forgetPassword(ForgetPassDTO forgetPassDTO);
}
