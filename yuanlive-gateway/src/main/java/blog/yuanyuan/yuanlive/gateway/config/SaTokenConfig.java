package blog.yuanyuan.yuanlive.gateway.config;

import blog.yuanyuan.yuanlive.common.result.ResultCode;
import blog.yuanyuan.yuanlive.entity.user.entity.UserRoleEnum;
import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import blog.yuanyuan.yuanlive.common.result.Result;
import org.springframework.web.bind.annotation.RequestMethod;

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
            "/user/auth/forgetPassword",
            // 5. 放行WebSocket
            "/ws/**",
            // 6. 其他需要放行的地址
            "/live/category/getInfo/*",
            "/live/category/list",
            "/live/category/tree",
            "/live/category/firstLevel",
            "/live/room/start",
            "/live/room/end",
            "/live/room/dvr",
            "/user/follow/followers/*",
            "/user/follow/following/*",
            "/live/category/listByCategory",
            "/live/room/popularRooms"

    };

    // 注册 Sa-Token全局过滤器
    @Bean
    public SaReactorFilter getSaReactorFilter() {
        return new SaReactorFilter()
                // 拦截地址
                .addInclude("/**")    /* 拦截全部path */
                // 开放地址
                .addExclude(WHITE_LIST)
                // 前置函数：在每次认证函数之前执行
                .setBeforeAuth(obj -> {
                    // ---------- 设置跨域响应头 ----------
                    SaHolder.getResponse()
                            // 允许指定域访问跨域资源
                            .setHeader("Access-Control-Allow-Origin", "*")
                            // 允许所有请求方式
                            .setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PUT")
                            // 有效时间
                            .setHeader("Access-Control-Max-Age", "3600")
                            // 允许的header参数
                            .setHeader("Access-Control-Allow-Headers", "*");
                    // 如果是 OPTIONS 请求，则结束请求
                    if (SaHolder.getRequest().getMethod().equals(RequestMethod.OPTIONS.name())) {
                        SaRouter.back();
                    }
                })
                // 鉴权方法：每次访问进入
                .setAuth(obj -> {
                    // 登录校验 -- 拦截所有路由，并排除/user/doLogin 用于开放登录
                    SaRouter.match("/**",  r -> StpUtil.checkLogin());
                    SaRouter.match("/user/menu/**", r -> StpUtil.checkRole(UserRoleEnum.ADMIN.name()));
                    SaRouter.match("/user/admin/**", r -> StpUtil.checkRole(UserRoleEnum.ADMIN.name()));
                    SaRouter.match("/user/role/**", r -> StpUtil.checkRole(UserRoleEnum.ADMIN.name()));
                    SaRouter.match("/live/category/**", r -> StpUtil.checkRole(UserRoleEnum.ADMIN.name()));
                })
                // 异常处理方法：每次setAuth函数出现异常时进入
                .setError(e -> {
                    // 设置响应头
                    SaHolder.getResponse().setHeader("Content-Type", "application/json;charset=UTF-8");

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
                    log.warn("Response     : {}", JSONUtil.toJsonStr(result));
                    try {
                        return new ObjectMapper().writeValueAsString(result);
                    } catch (JsonProcessingException ex) {
                        throw new RuntimeException(ex);
                    }
                });
    }
}
