package blog.yuanyuan.yuanlive.common.aspect;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Aspect
@Component
@Slf4j
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class WebLogAspect {
    @Pointcut("execution(public * blog.yuanyuan.yuanlive..controller..*.*(..))")
    public void webLog() {}

    @Around("webLog()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        // 1. 获取当前请求对象
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;

        // 2. 生成请求唯一 ID (Trace ID)，放入 MDC，方便日志追踪

        String url = "";
        String method = "";
        String ip = "";
        String params = "";

        if (request != null) {
            url = request.getRequestURL().toString();
            method = request.getMethod();
            ip = request.getRemoteAddr(); // 实际生产中建议封装一个 IPUtils 获取真实 IP
        }

        // 3. 过滤参数 (防止上传文件等无法序列化的参数报错)
        Object[] args = joinPoint.getArgs();
        List<Object> logArgs = Arrays.stream(args)
                .filter(arg -> (!(arg instanceof HttpServletRequest) && !(arg instanceof ServletResponse) && !(arg instanceof MultipartFile)))
                .collect(Collectors.toList());
        try {
            params = JSONUtil.toJsonStr(logArgs);
        } catch (Exception e) {
            params = "无法序列化参数";
        }

        // 4. 打印【请求日志】
        log.info("URL          : {}", url);
        log.info("HTTP Method  : {}", method);
        log.info("Class Method : {}.{}", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName());
        log.info("IP           : {}", ip);
        log.info("Request Args : {}", params);

        Object result = null;
        try {
            // 5. 执行目标方法
            result = joinPoint.proceed();
            // 【正常返回】打印响应日志
            String resultStr = "";
            try {
                resultStr = JSONUtil.toJsonStr(result);
                if (resultStr.length() > 1000) {
                    resultStr = resultStr.substring(0, 1000) + "...";
                }
            } catch (Exception e) {
                resultStr = "无法序列化返回值";
            }
            log.info("Response     : {}", resultStr);

            return result;
        } finally {
            long timeCost = System.currentTimeMillis() - startTime;
            log.info("Time Cost    : {} ms", timeCost);
        }
    }
}
