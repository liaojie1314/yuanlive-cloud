package blog.yuanyuan.yuanlive.gateway.filter;

import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@Slf4j
public class TraceIdFilter implements GlobalFilter, Ordered {
    private static final String TRACE_ID_HEADER = "traceId";
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1. 生成 TraceId (如果前端没传的话)
        String traceId = IdUtil.fastSimpleUUID();

        // 2. 放入 MDC (为了让网关自己的日志也能打印出 ID)
        // 注意：WebFlux 中 MDC 支持有限，但这行能保证当前线程的日志有 ID
        MDC.put(TRACE_ID_HEADER, traceId);
        log.info("========================================== Start ==========================================");

        // 3. 放入 Request Header (传递给下游微服务)
        ServerHttpRequest newRequest = exchange.getRequest().mutate()
                .header(TRACE_ID_HEADER, traceId)
                .build();

        return chain.filter(exchange.mutate().request(newRequest).build())
                .doFinally(signalType -> {
                    // 4. 请求结束，清理 MDC，防止内存泄漏或线程污染
                    log.info("=========================================== End ===========================================");
                    MDC.remove(TRACE_ID_HEADER);
                });
    }

    @Override
    public int getOrder() {
        // 优先级设为最高，保证最先执行
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
