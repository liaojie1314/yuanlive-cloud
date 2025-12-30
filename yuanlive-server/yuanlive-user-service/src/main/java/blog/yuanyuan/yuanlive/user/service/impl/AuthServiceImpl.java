package blog.yuanyuan.yuanlive.user.service.impl;

import blog.yuanyuan.yuanlive.entity.user.entity.SysUser;
import blog.yuanyuan.yuanlive.user.domain.dto.LoginDTO;
import blog.yuanyuan.yuanlive.user.domain.dto.RegisterDTO;
import blog.yuanyuan.yuanlive.user.domain.vo.LoginVO;
import blog.yuanyuan.yuanlive.user.domain.vo.RefreshVO;
import blog.yuanyuan.yuanlive.user.properties.RefreshTokenProperties;
import blog.yuanyuan.yuanlive.user.properties.RegisterProperties;
import blog.yuanyuan.yuanlive.user.service.AuthService;
import blog.yuanyuan.yuanlive.user.service.SysUserService;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import me.ahoo.cosid.provider.IdGeneratorProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import result.Result;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {
    @Resource
    private RegisterProperties registerProperties;
    @Resource
    private RefreshTokenProperties refreshTokenProperties;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private SysUserService sysUserService;
    @Resource
    private PasswordEncoder passwordEncoder;
    @Resource
    private IdGeneratorProvider idGeneratorProvider;

    @Override
    public String getCode(String email) {
        String key = registerProperties.getPrefix() + email;
        String code = generateVerificationCode();
        if (Boolean.TRUE.equals(stringRedisTemplate.opsForValue()
                .setIfAbsent(key, code, registerProperties.getTtl(), TimeUnit.valueOf(registerProperties.getTimeunit())))) {
            return code;
        }
        return "";
    }

    @Override
    public boolean register(RegisterDTO registerDTO) {
        String email = registerDTO.getEmail();
        String code = stringRedisTemplate.opsForValue().get(registerProperties.getPrefix() + email);
        if (!registerDTO.getCode().equals(code)) {
            return false;
        }
        String password = registerDTO.getPassword();
        password = passwordEncoder.encode(password);
        registerDTO.setPassword(password);
        if (registerDTO.getUsername().isEmpty()) {
            String name = "user" + idGeneratorProvider.getRequired("user_id").generate();
            registerDTO.setUsername(name);
        }
        SysUser user = BeanUtil.copyProperties(registerDTO, SysUser.class);
        sysUserService.save(user);
        stringRedisTemplate.delete(registerProperties.getPrefix() + email);
        return true;
    }

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        SysUser user;
        if (Validator.isEmail(loginDTO.getAccount())) {
            user = sysUserService.lambdaQuery().eq(SysUser::getEmail, loginDTO.getAccount()).one();
        } else {
            user = sysUserService.lambdaQuery().eq(SysUser::getUsername, loginDTO.getAccount()).one();
        }
        if (user == null || !passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new RuntimeException("账号或密码错误");
        }
        clearOldRefreshToken(user.getUid(), loginDTO.getDevice());
        StpUtil.login(user.getUid(), loginDTO.getDevice());
        StpUtil.getSession().set("role", user.getRole().name());
        String accessToken = StpUtil.getTokenInfo().getTokenValue();
        String refreshToken = IdUtil.simpleUUID();
        String key = refreshTokenProperties.getPrefix() + refreshToken;
        stringRedisTemplate.opsForValue()
                .set(key, user.getUid() + ":" + loginDTO.getDevice(), refreshTokenProperties.getTtl(), TimeUnit.valueOf(refreshTokenProperties.getTimeunit()));
        StpUtil.getTokenSession().set("REFRESH_TOKEN", refreshToken);
        return new LoginVO(accessToken, refreshToken, user.getRole().getCode());
    }

    @Override
    public RefreshVO refreshToken(String refreshToken) {
        String redisKey = refreshTokenProperties.getPrefix() + refreshToken;
        String value = stringRedisTemplate.opsForValue().get(redisKey);

        if (StrUtil.isBlank(value)) {
            throw new RuntimeException("登录已失效，请重新登录");
        }
        // 2. 解析 Value (格式: userId:device)
        String[] split = value.split(":");
        Long userId = Long.parseLong(split[0]);
        String device = split[1];

        // 3. Sa-Token 重新登录，获取新的 Access Token
        // 注意：这里必须使用相同的 device，否则会把其他设备的登录挤掉（或者产生错误的设备会话）
        StpUtil.login(userId, device);
        String newTokenInfo = StpUtil.getTokenInfo().getTokenValue();

        // 4. 刷新 Refresh Token 的有效期 (续命)
        stringRedisTemplate.expire(redisKey, refreshTokenProperties.getTtl(), TimeUnit.valueOf(refreshTokenProperties.getTimeunit()));

        // 5. 返回新的 Access Token
        // Refresh Token 可以保持不变传回去，也可以生成新的（通常保持不变即可）
        return new RefreshVO(newTokenInfo, refreshToken);
    }

    /**
     * 生成6位随机验证码（000000-999999）
     * @return 6位数字验证码的字符串，包含前导零
     */
    private String generateVerificationCode() {
        // 生成0到999999之间的随机数，并格式化为6位数字符串，不足6位前面补0
        return String.format("%06d", new Random().nextInt(1000000));
    }

    private void clearOldRefreshToken(Long uid, String device) {
        String oldAccessToken = StpUtil.getTokenValueByLoginId(uid, device);
        if (oldAccessToken != null) {
            SaSession oldSession = StpUtil.getTokenSessionByToken(oldAccessToken);

            if (oldSession != null) {
                String oldRefreshToken = oldSession.getString("REFRESH_TOKEN");
                if (oldRefreshToken != null) {
                    String rtKey = refreshTokenProperties.getPrefix() + oldRefreshToken;
                    stringRedisTemplate.delete(rtKey);
                }
            }
        }
    }
}
