package blog.yuanyuan.yuanlive.live.server;

import blog.yuanyuan.yuanlive.live.constant.MsgType;
import blog.yuanyuan.yuanlive.live.model.IMPacket;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@ChannelHandler.Sharable
@Component
@Slf4j
public class NettyServerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    @Resource
    private SessionManager sessionManager;
    @Resource
    private RabbitTemplate rabbitTemplate;
    @Value("${yuanlive.chat.mq.exchange}")
    private String exchangeName;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        sessionManager.remove(ctx.channel());
        super.channelInactive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) throws Exception {
        // 1. 解析消息
        log.info("收到消息: {}", frame.text());
        IMPacket packet = JSONUtil.toBean(frame.text(), IMPacket.class);
        log.info("收到消息: {}", packet);
        if (packet == null) return;

        switch (packet.getType()) {
            case PING:
                // 心跳保持不变
                log.info("收到心跳包");
                handlePing(ctx);
                break;

            case JOIN:
                // 👉 重点：在这里处理加入房间
                handleJoinRoom(ctx, packet);
                break;

            case CHAT:
                // 发送聊天
                handleChat(ctx, packet);
                break;

            default:
                break;
        }
    }

    // 1. 处理心跳
    private void handlePing(ChannelHandlerContext ctx) {
        IMPacket pong = new IMPacket();
        pong.setType(MsgType.PONG);
        sendMsg(ctx, pong);
    }

    private void handleJoinRoom(ChannelHandlerContext ctx, IMPacket packet) {
        String roomId = packet.getRoomId();
        if (StrUtil.isNotBlank(roomId)) {
            // 将当前连接加入到 Room Group
            sessionManager.joinRoom(roomId, ctx.channel());

            // 可选：回复一个 LOGIN_SUCCESS 消息给前端
            // ctx.writeAndFlush(...)
        }
    }

    // 2. 处理聊天
    private void handleChat(ChannelHandlerContext ctx, IMPacket packet) {
        IMPacket ack = new IMPacket();
        ack.setType(MsgType.ACK);
        sendMsg(ctx, ack);
        String currentRoomId = ctx.channel().attr(SessionManager.KEY_ROOM_ID).get();
        if (currentRoomId == null) {
            return;
        }
        packet.setUserId(ctx.channel().attr(SessionManager.KEY_USER_ID).get());
        packet.setRoomId(currentRoomId);
        // 广播
        String json = JSONUtil.toJsonStr(packet);
        rabbitTemplate.convertAndSend(exchangeName, "", json);
    }

    // 辅助发送方法
    private void sendMsg(ChannelHandlerContext ctx, IMPacket packet) {
        ctx.channel().writeAndFlush(new TextWebSocketFrame(JSONUtil.toJsonStr(packet)));
    }

    // 处理心跳超时 (IdleStateHandler 触发)
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        String traceId = ctx.channel().attr(SessionManager.KEY_TRACE_ID).get();
        MDC.put("traceId", traceId);
        try {
            if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
                // 此时 AuthHandshakeHandler 已经执行完毕，Attribute 里有值了
                Long userId = ctx.channel().attr(SessionManager.KEY_USER_ID).get();
                String deviceId = ctx.channel().attr(SessionManager.KEY_DEVICE_ID).get();

                if (userId != null) {
                    // ✅ 在这里注册才是真正有效的
                    sessionManager.register(userId, ctx.channel());
                    log.info("✅ 用户[{}] 设备[{}] 握手成功，正式上线！", userId, deviceId);
                }
            } else if (evt instanceof IdleStateEvent event) {
                if (event.state() == IdleState.READER_IDLE) {
                    log.warn("心跳超时，关闭连接: {}", ctx.channel().id());
                    ctx.close();
                }
            } else {
                super.userEventTriggered(ctx, evt);
            }
        } finally {
            MDC.remove("traceId");
        }
    }
}
