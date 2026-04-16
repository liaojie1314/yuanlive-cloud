package blog.yuanyuan.yuanlive.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Map;

@Component
@Slf4j
public class NettyRoutingFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        URI requestUrl = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);

        // 1. 判断是否为 WebSocket 请求 (更稳健的方式)
        // 只要 Header 里有 Upgrade: websocket，它就是 WebSocket，不管 scheme 是 http 还是 ws
        String upgradeHeader = exchange.getRequest().getHeaders().getUpgrade();
        boolean isWebSocket = "websocket".equalsIgnoreCase(upgradeHeader);

        if (!isWebSocket) {
            // 如果 scheme 是 ws/wss，也认
            String scheme = requestUrl != null ? requestUrl.getScheme() : "";
            if (!"ws".equals(scheme) && !"wss".equals(scheme)) {
                return chain.filter(exchange);
            }
        }

        // 2. 到了这里，说明肯定是 WebSocket 请求了
        if (requestUrl == null) {
            return chain.filter(exchange);
        }

        Response<ServiceInstance> response = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_LOADBALANCER_RESPONSE_ATTR);
        if (response != null && response.hasServer()) {
            ServiceInstance instance = response.getServer();
            Map<String, String> metadata = instance.getMetadata();
            String nettyPortStr = metadata.get("netty-port");

            if (nettyPortStr != null) {
                try {
                    int nettyPort = Integer.parseInt(nettyPortStr);

                    // 3. 替换端口，同时强制把协议改为 ws (防止它是 http)
                    URI newUrl = UriComponentsBuilder.fromUri(requestUrl)
                            .scheme("ws") // 强制修正协议
                            .port(nettyPort) // 强制修正端口
                            .build()
                            .toUri();
                    exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR, newUrl);
                } catch (NumberFormatException e) {
                    log.error("netty-port 格式错误", e);
                }
            }
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        // 必须在 LoadBalancerClientFilter (10150) 之后执行
        // 这样我们才能拿到已经被 LB 选中的真实 IP
        return 10151;
    }
}
