package blog.yuanyuan.yuanlive.user.service.impl;

import blog.yuanyuan.yuanlive.common.exception.ApiException;
import blog.yuanyuan.yuanlive.entity.user.entity.*;
import blog.yuanyuan.yuanlive.user.domain.dto.CodeDTO;
import blog.yuanyuan.yuanlive.user.domain.dto.ForgetPassDTO;
import blog.yuanyuan.yuanlive.user.domain.dto.LoginDTO;
import blog.yuanyuan.yuanlive.user.domain.dto.RegisterDTO;
import blog.yuanyuan.yuanlive.user.domain.enums.QrCodeStatus;
import blog.yuanyuan.yuanlive.user.domain.vo.*;
import blog.yuanyuan.yuanlive.user.properties.*;
import blog.yuanyuan.yuanlive.user.service.AuthService;
import blog.yuanyuan.yuanlive.user.service.SysRoleService;
import blog.yuanyuan.yuanlive.user.service.SysUserService;
import blog.yuanyuan.yuanlive.user.service.UserStatsService;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import me.ahoo.cosid.provider.IdGeneratorProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import blog.yuanyuan.yuanlive.common.result.Result;
import blog.yuanyuan.yuanlive.common.result.ResultCode;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {
    @Resource
    private RegisterProperties registerProperties;
    @Resource
    private RefreshTokenProperties refreshTokenProperties;
    @Resource
    private ForgetProperties forgetProperties;
    @Resource
    private QrCodeProperties qrCodeProperties;
    @Resource
    private User2RefreshTokenProperties user2RefreshTokenProperties;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private MailService mailService;
    @Resource
    private PasswordEncoder passwordEncoder;
    @Resource
    private IdGeneratorProvider idGeneratorProvider;
    @Resource
    private SysUserService userService;
    @Resource
    private SysRoleService roleService;
    @Resource
    private UserStatsService userStatsService;
    @Resource
    private LoginLimitProperties loginLimitProperties;
    @Resource(name = "loginLimitScript")
    private DefaultRedisScript<Long> loginLimitScript;

    @Override
    public void getCode(CodeDTO codeDTO) {
        String key;
        Long ttl;
        String timeunit;
        if (codeDTO.getOperationType().equals("REGISTER")) {
            key = registerProperties.getPrefix() + codeDTO.getEmail();
            ttl = registerProperties.getTtl();
            timeunit = registerProperties.getTimeunit();
        } else {
            if (!userService.lambdaQuery().eq(SysUser::getEmail, codeDTO.getEmail()).exists()) {
                throw new RuntimeException("该邮箱不存在");
            }
            key = forgetProperties.getPrefix() + codeDTO.getEmail();
            ttl = forgetProperties.getTtl();
            timeunit = forgetProperties.getTimeunit();
        }
        String code = generateVerificationCode();
        if (Boolean.TRUE.equals(stringRedisTemplate.opsForValue()
                .setIfAbsent(key, code, ttl, TimeUnit.valueOf(timeunit)))) {
            String type = codeDTO.getOperationType().equals("REGISTER") ? "注册" : "找回密码";
            mailService.sendMail(codeDTO.getEmail(), code, type);
            return;
        }
        throw new RuntimeException("验证码已发送，请稍后重试");
    }

    @Override
    public boolean register(RegisterDTO registerDTO) {
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            throw new RuntimeException("密码不一致");
        }
        String email = registerDTO.getEmail();
        String code = stringRedisTemplate.opsForValue().get(registerProperties.getPrefix() + email);
        if (!registerDTO.getCode().equals(code)) {
            return false;
        }
        String password = registerDTO.getPassword();
        password = passwordEncoder.encode(password);
        registerDTO.setPassword(password);
        long uid = idGeneratorProvider.getRequired("safe-js").generate();
        if (registerDTO.getUsername().isEmpty()) {
            String name = "user-" + uid;
            registerDTO.setUsername(name);
        }
        SysUser user = BeanUtil.copyProperties(registerDTO, SysUser.class);
        user.setUid(uid);
        user.setGender(registerDTO.getGender());
        userService.save(user);
        UserStats stats = new UserStats();
        stats.setUserId(uid);
        userStatsService.save(stats);
        stringRedisTemplate.delete(registerProperties.getPrefix() + email);
        return true;
    }

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        SysUser user;
        if (Validator.isEmail(loginDTO.getAccount())) {
            user = userService.lambdaQuery().eq(SysUser::getEmail, loginDTO.getAccount()).one();
        } else {
            user = userService.lambdaQuery().eq(SysUser::getUsername, loginDTO.getAccount()).one();
        }
        if (user == null) {
            throw new ApiException("账号");
        }
        // 先判断账号是否存在，在执行lua脚本，防止针对账号的攻击
        LoginLimit(user);
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new ApiException("密码错误");
        }
        // 登陆成功，清理failCount
        String failKey = loginLimitProperties.getFailCount().getPrefix() + user.getUid();
        stringRedisTemplate.delete(failKey);

        if (isRepeatLogin(user.getUid(), loginDTO.getDevice(), loginDTO.getDeviceID())) {
            // 如果重复登陆
            String accessToken = StpUtil.getTokenValueByLoginId(user.getUid(), loginDTO.getDevice());
            String refreshToken = stringRedisTemplate.opsForValue()
                    .get(user2RefreshTokenProperties.getPrefix() + user.getUid() + ":" + loginDTO.getDevice());
            log.info("用户{}在设备{}重复登录", user.getUid(), loginDTO.getDevice());
            Long expireTimeStamp = getExpireTimeStamp(StpUtil.getTokenTimeout(accessToken));
            return new LoginVO(accessToken, refreshToken, user.getRole().getCode(), user.getUid().toString(), expireTimeStamp);
        }
        clearOldRefreshToken(user.getUid(), loginDTO.getDevice());
        StpUtil.login(user.getUid(), loginDTO.getDevice());
        // 设置用户角色与权限 同时也可在session中存放用户名等信息
        setRoleAndPerms(user);
        StpUtil.getTokenSession().set("deviceID", loginDTO.getDeviceID());
        String accessToken = StpUtil.getTokenInfo().getTokenValue();
        String refreshToken = IdUtil.simpleUUID();
        String key = refreshTokenProperties.getPrefix() + refreshToken;
        stringRedisTemplate.opsForValue()
                .set(key, user.getUid() + ":" + loginDTO.getDevice(), refreshTokenProperties.getTtl(), TimeUnit.valueOf(refreshTokenProperties.getTimeunit()));
//        StpUtil.getTokenSession().set("REFRESH_TOKEN", refreshToken);
        // 保存用户登录设备与refreshToken的映射关系
        String mappingKey = user2RefreshTokenProperties.getPrefix() + user.getUid() + ":" + loginDTO.getDevice();
        stringRedisTemplate.opsForValue()
                .set(mappingKey, refreshToken, user2RefreshTokenProperties.getTtl(), TimeUnit.valueOf(user2RefreshTokenProperties.getTimeunit()));
        Long expireTimeStamp = getExpireTimeStamp(StpUtil.getTokenTimeout());
        return new LoginVO(accessToken, refreshToken, user.getRole().getCode(), user.getUid().toString(), expireTimeStamp);
    }

    private void LoginLimit(SysUser user) {
        // 构造脚本参数
        String failKey = loginLimitProperties.getFailCount().getPrefix() + user.getUid();
        Long failTtl = loginLimitProperties.getFailCount().getTtl();
        String failTimeunit = loginLimitProperties.getFailCount().getTimeunit();
        long failSeconds = TimeUnit.valueOf(failTimeunit).toSeconds(failTtl);

        String lockKey = loginLimitProperties.getLock().getPrefix() + user.getUid();
        Long lockTtl = loginLimitProperties.getLock().getTtl();
        String lockTimeunit = loginLimitProperties.getLock().getTimeunit();
        long lockSeconds = TimeUnit.valueOf(lockTimeunit).toSeconds(lockTtl);

        Long lockTime = stringRedisTemplate
                .execute(loginLimitScript,
                        Arrays.asList(failKey, lockKey),
                        String.valueOf(loginLimitProperties.getThreshold()),
                        String.valueOf(failSeconds),
                        String.valueOf(lockSeconds));
        if (lockTime > 0) {
            throw new ApiException("登录失败次数过多，请在 " + lockTime + " 秒后重试");
        }
    }

    private void setRoleAndPerms(SysUser user) {
            UserVO userVO = userService.getUserById(user.getUid());
            List<String> roles = new ArrayList<>(userVO.getRoles().stream().map(SysRole::getRoleKey).toList());
            roles.add(user.getRole().name());
            String join = String.join(",", roles);
            StpUtil.getSession().set("role", join);
            List<Long> roleIds = userVO.getRoles().stream().map(SysRole::getRoleId).toList();
            List<SysMenu> menus = roleService.getRolesMenus(roleIds);
            List<String> perms = menus.stream()
                    .map(menu -> StrUtil.isNotBlank(menu.getPerms()) ? menu.getPerms() : null)
                    .filter(Objects::nonNull).toList();
            String permsJoin = String.join(",", perms);
            StpUtil.getSession().set("perms", permsJoin);
            StpUtil.getSession().set("username", user.getUsername());
            StpUtil.getSession().set("avatar", user.getAvatar() != null ? user.getAvatar() : "");
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
        Long expireTimeStamp = getExpireTimeStamp(StpUtil.getTokenTimeout());

        // 4. 刷新 Refresh Token 的有效期 (续命)
        stringRedisTemplate.expire(redisKey, refreshTokenProperties.getTtl(), TimeUnit.valueOf(refreshTokenProperties.getTimeunit()));
        setRoleAndPerms(userService.getById(userId));
        // 5. 返回新的 Access Token
        // Refresh Token 可以保持不变传回去，也可以生成新的（通常保持不变即可）
        return new RefreshVO(newTokenInfo, refreshToken, expireTimeStamp);
    }

    @Override
    public QrCodeVO initQrCode(String deviceID) {
        String uuid = IdUtil.simpleUUID();
        String key = qrCodeProperties.getPrefix() + uuid;
        stringRedisTemplate.opsForValue()
                .set(key, QrCodeStatus.WAITING.getStatus() + ":" + deviceID, qrCodeProperties.getTtl(), TimeUnit.valueOf(qrCodeProperties.getTimeunit()));
        return new QrCodeVO(uuid, "yuanlive://auth/login?uuid=" + uuid);
    }

    @Override
    public QrCodeCheckVO checkQrCodeStatus(String uuid) {
        String key = qrCodeProperties.getPrefix() + uuid;
        String value = stringRedisTemplate.opsForValue().get(key);
        if (value == null) {
            throw new ApiException(ResultCode.QRCODE_EXPIRE);
        }
        // 如果 Value 是 JSON 格式（说明已确认，里面有 Token），则解析它
        if (JSONUtil.isTypeJSON(value)) {
            JSONObject json = JSONUtil.parseObj(value);
            stringRedisTemplate.delete(key);
            Long expireTimeStamp = getExpireTimeStamp(StpUtil.getTokenTimeout(json.getStr("accessToken")));
            return QrCodeCheckVO.builder()
                    .status(QrCodeStatus.getByStatus(json.getInt("status")).name())
                    .accessToken(json.getStr("accessToken"))
                    .refreshToken(json.getStr("refreshToken"))
                    .uid(json.getStr("uid"))
                    .expire(expireTimeStamp).build();
        } else {
            // 还没成功，直接返回状态码 (0 或 1)
            return QrCodeCheckVO.builder()
                    .status(QrCodeStatus.getByStatus(Integer.parseInt(value.split(":")[0])).name())
                    .build();
        }
    }

    @Override
    public Result scanQrCode(String uuid) {
        if (!StpUtil.getLoginDeviceType().equals("mobile")) {
            return Result.failed("只有app端才可扫码");
        }
        String key = qrCodeProperties.getPrefix() + uuid;
        String value = stringRedisTemplate.opsForValue().get(key);
        // 1. 基础校验：二维码不存在或已过期
        if (value == null) {
            throw new ApiException(ResultCode.QRCODE_EXPIRE);
        }
        // 2. 解析当前状态
        int status;
        try {
            // 如果存的是 JSON (已确认状态)，说明肯定已经结束流程了
            if (JSONUtil.isTypeJSON(value)) {
                JSONObject json = JSONUtil.parseObj(value);
                status = json.getInt("status");
            } else {
                status = Integer.parseInt(value.split(":")[0]);
            }
        } catch (Exception e) {
            return Result.failed("二维码状态异常");
        }
        switch (status) {
            case 2:
                return Result.failed("该二维码已完成登录，请勿重复扫码");
            case 1:
                // 如果已经是“已扫码”状态，提示用户去之前的页面确认，或者是被别人扫了
                return Result.failed("二维码已被扫描，请在手机上确认登录");
            case 0:
                // 正常流程，继续往下走
                break;
            default:
                return Result.failed("无效的二维码状态");
        }
        // 4. 更新状态为 1 (SCANNED)
        stringRedisTemplate.opsForValue().set(key, QrCodeStatus.SCANNED.getStatus() + ":" + value.split(":")[1]);
        return Result.success("扫码成功");
    }

    @Override
    public Result<String> confirmLogin(String uuid) {
        if (!StpUtil.getLoginDeviceType().equals("mobile")) {
            return Result.failed("只有app端才可扫码");
        }
        String key = qrCodeProperties.getPrefix() + uuid;
        if (!stringRedisTemplate.hasKey(key)) {
            throw new ApiException(ResultCode.QRCODE_EXPIRE);
        }
        // 不能重复确认
        if (JSONUtil.isTypeJSON(stringRedisTemplate.opsForValue().get(key))) {
            return Result.failed("二维码已确认");
        }
        // 1. 获取当前操作的手机用户 ID
        long userId = StpUtil.getLoginIdAsLong();
        // 清除desktop端旧 Refresh Token
        clearOldRefreshToken(userId, "desktop");
        String str = stringRedisTemplate.opsForValue().get(key);
        String deviceID = str.split(":")[1];
        // 2. 关键：为 desktop 端生成一个独立的 Token
        String pcToken = StpUtil.createLoginSession(userId, SaLoginParameter.create().setDeviceType("desktop"));
        // 3. 构造要存入 Redis 的数据 (状态 + Token)
        String refreshToken = IdUtil.simpleUUID();
        String refreshKey = refreshTokenProperties.getPrefix() + refreshToken;
        stringRedisTemplate.opsForValue()
                .set(refreshKey, userId + ":" + "desktop", refreshTokenProperties.getTtl(), TimeUnit.valueOf(refreshTokenProperties.getTimeunit()));
//        StpUtil.getTokenSessionByToken(pcToken).set("REFRESH_TOKEN", refreshToken);
        // 保存用户登录设备与refreshToken的映射关系
        String mappingKey = user2RefreshTokenProperties.getPrefix() + userId + ":" + "desktop";
        stringRedisTemplate.opsForValue()
                .set(mappingKey, refreshToken, user2RefreshTokenProperties.getTtl(), TimeUnit.valueOf(user2RefreshTokenProperties.getTimeunit()));
        StpUtil.getTokenSessionByToken(pcToken).set("deviceID", deviceID);
        Map<String, Object> data = new HashMap<>();
        data.put("status", QrCodeStatus.CONFIRMED.getStatus());
        data.put("accessToken", pcToken);
        data.put("refreshToken", refreshToken);
        data.put("uid", userId);
        // 4. 写入 Redis，web 端轮询时就能拿到了
        stringRedisTemplate.opsForValue()
                .set(key, JSONUtil.toJsonStr(data), qrCodeProperties.getTtl(), TimeUnit.valueOf(qrCodeProperties.getTimeunit()));
        return Result.success("确认登录成功");
    }

    @Override
    public Result<String> logout() {
        // 清除refreshToken
        clearOldRefreshToken(StpUtil.getLoginIdAsLong(), StpUtil.getLoginDeviceType());
        StpUtil.logout();
        return Result.success("注销成功");
    }

    @Override
    public Result<String> forgetPassword(ForgetPassDTO forgetPassDTO) {
        SysUser user = userService.lambdaQuery()
                .eq(SysUser::getEmail, forgetPassDTO.getEmail())
                .one();
        if (!forgetPassDTO.getPassword().equals(forgetPassDTO.getConfirmPassword())) {
            return Result.failed("密码不一致");
        }
        String key = forgetProperties.getPrefix() + forgetPassDTO.getEmail();
        if (!stringRedisTemplate.hasKey(key)) {
            return Result.failed("验证码已过期");
        }
        if (!Objects.equals(stringRedisTemplate.opsForValue().get(key), forgetPassDTO.getCode())) {
            return Result.failed("验证码错误");
        }
        boolean updated = userService.lambdaUpdate()
                .eq(SysUser::getEmail, forgetPassDTO.getEmail())
                .set(SysUser::getPassword, passwordEncoder.encode(forgetPassDTO.getPassword()))
                .update();
        if (updated) {
            stringRedisTemplate.delete(key);
            // 清除所有端旧 Refresh Token
            clearOldRefreshToken(user.getUid(), "web");
            clearOldRefreshToken(user.getUid(), "mobile");
            clearOldRefreshToken(user.getUid(), "desktop");
            StpUtil.logout(user.getUid());
            return Result.success("修改成功");
        } else {
            return Result.failed("不存在该账号");
        }
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
        String mappingKey = user2RefreshTokenProperties.getPrefix() + uid + ":" + device;
        if (stringRedisTemplate.hasKey(mappingKey)) {
            String refreshToken = stringRedisTemplate.opsForValue().get(mappingKey);
            if (refreshToken != null) {
                String refreshKey = refreshTokenProperties.getPrefix() + refreshToken;
                stringRedisTemplate.delete(refreshKey);
            }
            stringRedisTemplate.delete(mappingKey);
        }
    }

    private boolean isRepeatLogin(Long uid, String device, String deviceID) {
        String token = StpUtil.getTokenValueByLoginId(uid, device);
        if (token == null) {
            return false;
        }
        SaSession tokenSession = StpUtil.getTokenSessionByToken(token);
        if (tokenSession == null) {
            return false;
        }
        return tokenSession.getString("deviceID").equals(deviceID);
    }

    private Long getExpireTimeStamp(long timeout) {
        Long expireTime;
        if (timeout == -1) {
            expireTime = -1L;
        } else if (timeout == -2) {
            throw new RuntimeException("Token生成异常或已过期");
        } else {
            expireTime = (System.currentTimeMillis() + timeout * 1000) / 1000;
        }
        return expireTime;
    }
}
