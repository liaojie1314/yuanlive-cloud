package blog.yuanyuan.yuanlive.user.service;

import blog.yuanyuan.yuanlive.user.domain.dto.LoginDTO;
import blog.yuanyuan.yuanlive.user.domain.dto.RegisterDTO;
import blog.yuanyuan.yuanlive.user.domain.vo.LoginVO;
import jakarta.validation.constraints.Email;

public interface AuthService {
    String getCode(@Email(message = "格式错误") String email);

    boolean register(RegisterDTO registerDTO);

    LoginVO login(LoginDTO loginDTO);

    LoginVO refreshToken(String refreshToken);
}
