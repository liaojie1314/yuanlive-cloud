package blog.yuanyuan.yuanlive.live.server;

import blog.yuanyuan.yuanlive.entity.live.entity.LiveRoom;
import blog.yuanyuan.yuanlive.live.constant.MsgType;
import blog.yuanyuan.yuanlive.live.domain.vo.LiveRoomDetailVO;
import blog.yuanyuan.yuanlive.live.message.*;
import blog.yuanyuan.yuanlive.live.message.notification.GroupChatNotification;
import blog.yuanyuan.yuanlive.live.message.request.GroupChatRequest;
import blog.yuanyuan.yuanlive.live.message.request.JoinRequest;
import blog.yuanyuan.yuanlive.live.message.request.LeaveRequest;
import blog.yuanyuan.yuanlive.live.message.request.PingMessage;
import blog.yuanyuan.yuanlive.live.message.response.AckMessage;
import blog.yuanyuan.yuanlive.live.message.response.JoinResponse;
import blog.yuanyuan.yuanlive.live.message.response.PongMessage;
import blog.yuanyuan.yuanlive.live.properties.LiveRoomProperties;
import blog.yuanyuan.yuanlive.live.properties.LiveWeightsProperties;
import blog.yuanyuan.yuanlive.live.service.LiveRoomService;
import blog.yuanyuan.yuanlive.live.util.PopularityUtil;
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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Objects;

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
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private LiveRoomService liveRoomService;
    @Resource
    private LiveWeightsProperties liveWeightsProperties;
    @Resource
    private LiveRoomProperties liveRoomProperties;
    @Resource(name = "joinRoomScript")
    private DefaultRedisScript<Long> joinRoomScript;
    @Resource(name = "updatePopularityScript")
    private DefaultRedisScript<Long> updatePopularityScript;

    @Resource
    private PopularityUtil popularityUtil;

    @Value("${live.mq.chat.exchange}")
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
        switch (message.getType()) {
            case PING -> {
                log.info("收到心跳包");
                handlePing(ctx, (PingMessage) message);
            }
            case JOIN -> handleJoinRoom(ctx, (JoinRequest) message);
            case CHAT -> handleChat(ctx, (GroupChatRequest) message);
            case LEAVE -> handleLeaveRoom(ctx, (LeaveRequest) message);
            default -> handleGenericMessage(ctx, message);
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
        Long userId = ctx.channel().attr(SessionManager.KEY_USER_ID).get();// 判断直播间是否存在
        boolean exists = checkRoom(roomId);
        if (!exists) {
            log.warn("直播间[{}] 不存在", roomId);
            JoinResponse response = JoinResponse.builder()
                    .msgId(join.getMsgId())
                    .code(404)
                    .message("直播间未开播")
                    .success(false)
                    .build();
            sendMsg(ctx, response);
            return;
        }
        if (StrUtil.isNotBlank(roomId) && userId != null) {
            // 如果用户已经加入过该房间，不允许重复加入
            String currentRoomId = ctx.channel().attr(SessionManager.KEY_ROOM_ID).get();
            if (roomId.equals(currentRoomId)) {
                JoinResponse response = JoinResponse.builder()
                        .msgId(join.getMsgId())
                        .code(400)
                        .message("已加入过该房间")
                        .success(false)
                        .build();
                sendMsg(ctx, response);
                return;
            }
            // 如果用户已经加入过其他房间，先执行退出逻辑
            if (currentRoomId != null) {
                leaveRoom(ctx);
            }

            // 1. 本地连接管理
            sessionManager.joinRoom(roomId, ctx.channel());
            // 2. 准备 Redis Keys
            String currentKey = liveRoomProperties.getCurrentPrefix() + roomId;
            String totalKey = liveRoomProperties.getTotalPrefix() + roomId;
            String sessionKey = liveRoomProperties.getSessionPrefix() + roomId;

            // 3. 执行原子脚本：一气呵成完成 SADD, PFADD, SCARD, Peak Check
            Long currentCount = stringRedisTemplate.execute(
                    joinRoomScript,
                    Arrays.asList(currentKey, sessionKey, totalKey),
                    userId.toString()
            );

            // 4. 增加人气权重
            popularityUtil.updatePopularity(roomId, liveWeightsProperties.getView());
            String anchorName = (String) stringRedisTemplate.opsForHash()
                    .get(sessionKey, "anchor");
            String roomTitle = (String) stringRedisTemplate.opsForHash()
                    .get(sessionKey, "roomTitle");
            JoinResponse response = JoinResponse.builder()
                    .msgId(join.getMsgId())
                    .code(200)
                    .roomTitle(roomTitle)
                    .roomId(roomId)
                    .anchorName(anchorName)
                    .onlineCount(currentCount.intValue())
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
                .code(200)
                .build();
        String roomId = ctx.channel().attr(SessionManager.KEY_ROOM_ID).get();
        boolean exists = checkRoom(roomId);
        if (roomId == null || !exists) {
            ack.setSuccess(false);
            ack.setMessage("直播间未开播\n或用户未加入房间");
            sendMsg(ctx, ack);
            return;
        }
        ack.setSuccess(true);
        sendMsg(ctx, ack);

        // 构造广播通知 (Notification)，补全发送者信息
        Long userId = ctx.channel().attr(SessionManager.KEY_USER_ID).get();
        String username = ctx.channel().attr(SessionManager.KEY_USER_NAME).get();


        popularityUtil.updatePopularity(roomId, liveWeightsProperties.getChat());
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

    // 3. 处理离开房间
    private void handleLeaveRoom(ChannelHandlerContext ctx, LeaveRequest message) {
        String roomId = ctx.channel().attr(SessionManager.KEY_ROOM_ID).get();
        Long userId = ctx.channel().attr(SessionManager.KEY_USER_ID).get();
        AckMessage ack = AckMessage.builder()
                .msgId(message.getMsgId())
                .build();
        if (StrUtil.isBlank(roomId) || userId == null) {
            log.warn("用户未加入任何房间，无需处理退出请求");
            ack.setSuccess(false);
            ack.setMessage("用户未加入任何房间，无需处理退出请求");
            ack.setCode(400);
            sendMsg(ctx, ack);
            return;
        }
        log.info("用户[{}] 退出房间[{}]", userId, roomId);
        // 执行离场
        leaveRoom(ctx);
        ack.setSuccess(true);
        sendMsg(ctx, ack);
    }

    private void leaveRoom(ChannelHandlerContext ctx) {
        Long userId = ctx.channel().attr(SessionManager.KEY_USER_ID).get();
        String roomId = ctx.channel().attr(SessionManager.KEY_ROOM_ID).get();
        log.info("用户[{}] 退出房间[{}]", userId, roomId);
        ctx.channel().attr(SessionManager.KEY_ROOM_ID).set(null);
        String currentKey = liveRoomProperties.getCurrentPrefix() + roomId;
        stringRedisTemplate.opsForSet().remove(currentKey, userId.toString());
        popularityUtil.updatePopularity(roomId, -liveWeightsProperties.getView());
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
        WsResult result = WsResult.of(message);
        ctx.channel().writeAndFlush(new TextWebSocketFrame(JSONUtil.toJsonStr(result)));
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

    // 判断直播间是否存在
    private boolean checkRoom(String roomId) {
        return stringRedisTemplate.hasKey(liveRoomProperties.getSessionPrefix() + roomId);
    }
}
