package blog.yuanyuan.yuanlive.live.server;

import blog.yuanyuan.yuanlive.live.constant.MsgType;
import blog.yuanyuan.yuanlive.live.message.*;
import blog.yuanyuan.yuanlive.live.message.notification.GroupChatNotification;
import blog.yuanyuan.yuanlive.live.message.request.GroupChatRequest;
import blog.yuanyuan.yuanlive.live.message.request.JoinRequest;
import blog.yuanyuan.yuanlive.live.message.request.PingMessage;
import blog.yuanyuan.yuanlive.live.message.response.AckMessage;
import blog.yuanyuan.yuanlive.live.message.response.JoinResponse;
import blog.yuanyuan.yuanlive.live.message.response.PongMessage;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    @Resource
    private ObjectMapper objectMapper;
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
        Message message = objectMapper.readValue(frame.text(), Message.class);
        log.info("收到消息: {}", message);
        if (message == null) return;

        // 根据具体消息类型进行处理
        if (message instanceof PingMessage ping) {
            log.info("收到心跳包");
            handlePing(ctx, ping);
        } else if (message instanceof JoinRequest join) {
            // 👉 重点：在这里处理加入房间
            handleJoinRoom(ctx, join);
        } else if (message instanceof GroupChatRequest chat) {
            // 发送聊天
            handleChat(ctx, chat);
        } else {
            // 处理其他通用消息类型
            handleGenericMessage(ctx, message);
        }
    }

    // 1. 处理心跳
    private void handlePing(ChannelHandlerContext ctx, PingMessage ping) {
        PongMessage pong = PongMessage.builder()
                .msgId(ping.getMsgId())
                .build();
        sendMsg(ctx, pong);
    }

    private void handleJoinRoom(ChannelHandlerContext ctx, JoinRequest join) {
        String roomId = join.getRoomId();
        if (StrUtil.isNotBlank(roomId)) {
            // 将当前连接加入到 Room Group
            sessionManager.joinRoom(roomId, ctx.channel());
            JoinResponse response = JoinResponse.builder()
                    .msgId(join.getMsgId())
                    .code(200)
                    .type(MsgType.JOIN_RESP)
                    .success(true)
                    .onlineCount(1)
                    .build();
            sendMsg(ctx, response);
            log.info("用户[{}] 加入房间[{}] 成功", ctx.channel().attr(SessionManager.KEY_USER_ID).get(), roomId);
        }
    }

    // 2. 处理聊天
    private void handleChat(ChannelHandlerContext ctx, GroupChatRequest chat) {
        AckMessage ack = AckMessage.builder()
                .msgId(chat.getMsgId())
                .build();
        sendMsg(ctx, ack);

        // 构造广播通知 (Notification)，补全发送者信息
        Long userId = ctx.channel().attr(SessionManager.KEY_USER_ID).get();
        String roomId = ctx.channel().attr(SessionManager.KEY_ROOM_ID).get();
        String username = ctx.channel().attr(SessionManager.KEY_USER_NAME).get();

        if (roomId != null) {
            GroupChatNotification notification = GroupChatNotification.builder()
                    .type(MsgType.CHAT_NOTIFY) // 使用专用的通知类型
                    .roomId(roomId)
                    .userId(userId)
                    .username(username)
                    .content(chat.getContent())
                    .timestamp(System.currentTimeMillis())
                    .build();
            rabbitTemplate.convertAndSend(exchangeName, "", JSONUtil.toJsonStr(notification));
        }
    }

    // 处理通用消息
    private void handleGenericMessage(ChannelHandlerContext ctx, Message message) {
        // 对于其他类型的消息，直接转发到消息队列
        String currentRoomId = ctx.channel().attr(SessionManager.KEY_ROOM_ID).get();
        if (currentRoomId != null) {
            message.setRoomId(currentRoomId);
            message.setUserId(ctx.channel().attr(SessionManager.KEY_USER_ID).get());
            String json = JSONUtil.toJsonStr(message);
            rabbitTemplate.convertAndSend(exchangeName, "", json);
        }
    }

    // 辅助发送方法
    private void sendMsg(ChannelHandlerContext ctx, Message message) {
        ctx.channel().writeAndFlush(new TextWebSocketFrame(JSONUtil.toJsonStr(message)));
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
