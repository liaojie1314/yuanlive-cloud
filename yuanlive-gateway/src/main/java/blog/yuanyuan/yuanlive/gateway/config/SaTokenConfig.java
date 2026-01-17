package blog.yuanyuan.yuanlive.gateway.config;

import blog.yuanyuan.yuanlive.common.result.ResultCode;
import blog.yuanyuan.yuanlive.entity.user.entity.UserRoleEnum;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import blog.yuanyuan.yuanlive.common.result.Result;

@Configuration
@Slf4j
public class SaTokenConfig {
    private static final String[] WHITE_LIST = {
            // 1. 图标与静态资源
            "/favicon.ico",
            "/webjars/**",
            // 2. Knife4j / Swagger 核心页面与资源
            "/doc.html",
            "/swagger-resources/**",
            // 3. 核心：OpenAPI 接口文档数据 (JSON)
            // 匹配网关自身的文档
            "/v3/api-docs/**",
            "/*/v3/api-docs/**",
            // 4. 认证服务放行
            "/user/auth/login",
            "/user/auth/register",
            "/user/auth/getCode",
            "/user/auth/refreshToken",
            "/user/auth/qrcode/init",
            "/user/auth/qrcode/check",
            "/user/auth/forgetPassword"
    };

    // 注册 Sa-Token全局过滤器
    @Bean
    public SaReactorFilter getSaReactorFilter() {
        return new SaReactorFilter()
                // 拦截地址
                .addInclude("/**")    /* 拦截全部path */
                // 开放地址
                .addExclude(WHITE_LIST)
                // 鉴权方法：每次访问进入
                .setAuth(obj -> {
                    // 登录校验 -- 拦截所有路由，并排除/user/doLogin 用于开放登录
                    SaRouter.match("/**",  r -> StpUtil.checkLogin());
                    SaRouter.match("/user/menu/**", r -> StpUtil.checkPermission("menu"));
                })
                // 异常处理方法：每次setAuth函数出现异常时进入
                .setError(e -> {
                    Result<Object> result = Result.failed(e.getMessage());
                    if (e instanceof NotLoginException exception) {
                        if (exception.getType().equals(NotLoginException.INVALID_TOKEN)) {
                            result.setCode(ResultCode.TOKEN_EXPIRED.getCode());
                            result.setMsg(ResultCode.TOKEN_EXPIRED.getMsg());
                        }
                    }
                    if (e instanceof NotRoleException) {
                        result.setCode(ResultCode.FORBIDDEN.getCode());
                        result.setMsg(ResultCode.FORBIDDEN.getMsg());
                    }
                    try {
                        return new ObjectMapper().writeValueAsString(result);
                    } catch (JsonProcessingException ex) {
                        throw new RuntimeException(ex);
                    }
                })
                ;
    }
}
