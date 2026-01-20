package blog.yuanyuan.yuanlive.live.server;

import blog.yuanyuan.yuanlive.common.result.Result;
import blog.yuanyuan.yuanlive.feign.user.UserFeignClient;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import io.netty.channel.*;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
@Slf4j
@ChannelHandler.Sharable
public class AuthHandshakeHandler extends ChannelDuplexHandler {
    private final UserFeignClient userFeignClient;

    public AuthHandshakeHandler(UserFeignClient userFeignClient) {
        this.userFeignClient = userFeignClient;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 只拦截 HTTP 握手请求 (FullHttpRequest)
        if (msg instanceof FullHttpRequest request) {
            String uri = request.uri();

            // 1. 只有访问 /ws 路径才处理，其他的不管（防止误拦截 favicon.ico 等）
            if (!uri.contains("/ws")) {
                super.channelRead(ctx, msg);
                return;
            }

            // --- A. 获取 URL 中的 deviceID ---
            // 使用 Hutool 解析 URL 参数 (或者自己用 Netty 的 QueryStringDecoder)
            // uri 格式: /ws?deviceID=123456
            Map<String, String> params = HttpUtil.decodeParamMap(uri, StandardCharsets.UTF_8);
            String deviceId = params.get("deviceID");

            if (StrUtil.isBlank(deviceId)) {
                log.warn("握手失败: deviceID 为空");
                ctx.close();
                return;
            }

            // --- B. 获取 Header 中的 Token ---
            // 约定 Token 放在 Sec-WebSocket-Protocol 中
            String token = request.headers().get("Sec-WebSocket-Protocol");
            if (StrUtil.isNotBlank(token)) {
                ctx.channel().attr(SessionManager.KEY_TOKEN).set(token);
            }

            Result<Long> result = userFeignClient.checkToken(token);
            Long userId = result.getData();
            if (userId == null) {
                log.warn("握手失败: {}", result.getMsg());
                ctx.close();
                return;
            }

            // --- C. 绑定属性到 Channel ---
            // 绑定基本信息 (只绑 User，不绑 Room)
            ctx.channel().attr(SessionManager.KEY_DEVICE_ID).set(deviceId);
            ctx.channel().attr(SessionManager.KEY_USER_ID).set(userId);

            // --- D. 关键处理：Token 协议头 ---
            request.headers().remove("Sec-WebSocket-Protocol");

            log.info("握手认证成功 | Device: {} | User: {}", deviceId, userId);
            request.setUri("/ws");
        }

        // 继续向下传递，交给 WebSocketServerProtocolHandler 完成标准握手
        super.channelRead(ctx, msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof HttpResponse response) {
            // 如果是 WebSocket 握手成功响应 (101 Switching Protocols)
            if (response.status().equals(HttpResponseStatus.SWITCHING_PROTOCOLS)) {
                // 拿回刚才存的 Token
                String token = ctx.channel().attr(SessionManager.KEY_TOKEN).get();
                if (StrUtil.isNotBlank(token)) {
                    // 【欺骗 Apifox】把 Header 塞回去
                    response.headers().set("Sec-WebSocket-Protocol", token);
                    log.info("已在握手响应中补全 Sec-WebSocket-Protocol: {}", token);
                }
//                ctx.pipeline().remove(this);
            }
        }
        super.write(ctx, msg, promise);
    }
}
