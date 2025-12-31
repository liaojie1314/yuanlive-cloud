package blog.yuanyuan.yuanlive.common.config;

import blog.yuanyuan.yuanlive.common.interceptor.TraceIdInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 TraceId 拦截器
        registry.addInterceptor(new TraceIdInterceptor())
                .addPathPatterns("/**"); // 拦截所有请求
    }
}
