package blog.yuanyuan.yuanlive.common.config;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.stp.StpUtil;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign Token传递配置
 * 用于在Feign远程调用时自动传递当前用户的Token
 */
@Configuration
@Slf4j
public class FeignTokenConfig {

    @Bean
    public RequestInterceptor tokenRequestInterceptor() {
        return template -> {
            try {
                // 检查当前线程是否有登录用户
                if (StpUtil.isLogin()) {
                    // 获取当前用户的Token
                    String token = StpUtil.getTokenValue();
                    if (token != null) {
                        // 将Token添加到请求头中
                        String tokenPrefix = SaManager.getConfig().getTokenPrefix();
                        tokenPrefix = tokenPrefix == null ? "" : tokenPrefix + " ";
                        log.info("Feign请求添加Token前缀: {}, Token name: {}",
                                tokenPrefix,
                                StpUtil.getTokenName());
                        template.header(StpUtil.getTokenName(),
                                tokenPrefix + token);
                        log.info("Feign请求已添加Token: {}", token);
                    }
                } else {
                    log.debug("当前无登录用户，Feign请求不添加Token");
                }
            } catch (Exception e) {
                log.warn("Feign Token传递异常: {}", e.getMessage());
            }
        };
    }
}