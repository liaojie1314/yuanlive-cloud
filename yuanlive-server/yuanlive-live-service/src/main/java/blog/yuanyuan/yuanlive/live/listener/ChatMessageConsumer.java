package blog.yuanyuan.yuanlive.live.listener;

import blog.yuanyuan.yuanlive.common.result.Result;
import blog.yuanyuan.yuanlive.entity.user.vo.UserFollowLivingVO;
import blog.yuanyuan.yuanlive.feign.user.UserFeignClient;
import blog.yuanyuan.yuanlive.live.message.Message;
import blog.yuanyuan.yuanlive.live.message.WsResult;
import blog.yuanyuan.yuanlive.live.message.notification.LiveStartMessage;
import blog.yuanyuan.yuanlive.live.message.notification.SystemNotification;
import blog.yuanyuan.yuanlive.live.message.request.SingleChatRequest;
import blog.yuanyuan.yuanlive.live.server.SessionManager;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class ChatMessageConsumer {
    @Resource
    SessionManager sessionManager;
    @Resource
    ObjectMapper objectMapper;
    @Resource
    UserFeignClient userFeignClient;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(
                    value = "", // 留空，让 RabbitMQ 生成随机名字 (e.g. amq.gen-JzTY...)
                    autoDelete = "true", // 关机即删
                    exclusive = "true"   // 排他队列
            ),
            exchange = @Exchange(value = "${live.mq.chat.exchange}",
                    type = ExchangeTypes.FANOUT)
    ))
    public void onMessage(String messageStr) {
        log.info("收到 MQ 消息: {}", messageStr);
        try {
            Message message = objectMapper.readValue(messageStr, Message.class);
            log.info("message class: {}", message.getClass());
            if (message instanceof LiveStartMessage liveStart) {
                // 为关注者发送开播消息
                // 获取关注者
                Result<List<UserFollowLivingVO>> result = userFeignClient.getFollowers(liveStart.getUserId());
                if (result.getData() != null) {
                    List<UserFollowLivingVO> followers = result.getData();
                    for (UserFollowLivingVO follow : followers) {
                        Channel channel = sessionManager.getUserChannel(follow.getUserId());
                        if (channel != null && channel.isActive()) {
                            WsResult live = WsResult.of(liveStart);
                            channel.writeAndFlush(new TextWebSocketFrame(JSONUtil.toJsonStr(live)));
                            log.info("关注者[{}]发送成功: {}", follow.getUserId(), liveStart.getType());
                        }
                    }
                }
            }
            // 广播消息
            if (StrUtil.isNotBlank(message.getRoomId())) {
                ChannelGroup group = sessionManager.getRoomChannels(message.getRoomId());
                if (group != null && !group.isEmpty()) {
                    WsResult broadcast = WsResult.of(message);
                    group.writeAndFlush(new TextWebSocketFrame(JSONUtil.toJsonStr(broadcast)));
                    log.info("房间[{}]广播成功: {}", message.getRoomId(), message.getType());
                }
            } else if (message instanceof SystemNotification notification
                    && notification.getToUserId() != null) {
                Channel channel = sessionManager.getUserChannel(notification.getToUserId());
                if (channel != null && channel.isActive()) {
                    WsResult single = WsResult.of(notification);
                    channel.writeAndFlush(new TextWebSocketFrame(JSONUtil.toJsonStr(single)));
                    log.info("警告[{}]发送成功: {}", notification.getToUserId(), message.getType());
                }
            }
            // 私聊消息
            else if (message instanceof SingleChatRequest singleMsg) {
                Channel channel = sessionManager.getUserChannel(singleMsg.getToUserId());
                if (channel != null && channel.isActive()) {
                    WsResult single = WsResult.of(singleMsg);
                    channel.writeAndFlush(new TextWebSocketFrame(JSONUtil.toJsonStr(single)));
                    log.info("私聊[{}]发送成功: {}", singleMsg.getToUserId(), message.getType());
                }
            }
        } catch (Exception e) {
            log.error("MQ消费异常", e);
        }
    }
}
