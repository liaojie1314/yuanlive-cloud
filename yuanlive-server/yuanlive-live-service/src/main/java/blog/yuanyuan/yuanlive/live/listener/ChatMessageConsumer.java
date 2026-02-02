package blog.yuanyuan.yuanlive.live.listener;

import blog.yuanyuan.yuanlive.live.message.Message;
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

@Component
@Slf4j
public class ChatMessageConsumer {
    @Resource
    SessionManager sessionManager;
    @Resource
    ObjectMapper objectMapper;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(
                    value = "", // 留空，让 RabbitMQ 生成随机名字 (e.g. amq.gen-JzTY...)
                    autoDelete = "true", // 关机即删
                    exclusive = "true"   // 排他队列
            ),
            exchange = @Exchange(value = "${yuanlive.chat.mq.exchange}",
                    type = ExchangeTypes.FANOUT)
    ))
    public void onMessage(String messageStr) {
        log.info("收到 MQ 消息: {}", messageStr);
        try {
            Message message = objectMapper.readValue(messageStr, Message.class);
            if (StrUtil.isNotBlank(message.getRoomId())) {
                ChannelGroup group = sessionManager.getRoomChannels(message.getRoomId());
                if (group != null && !group.isEmpty()) {
                    group.writeAndFlush(new TextWebSocketFrame(messageStr));
                    log.info("房间[{}]广播成功: {}", message.getRoomId(), message.getType());
                }
            } else if (message instanceof SingleChatRequest singleMsg) {
                Channel channel = sessionManager.getUserChannel(singleMsg.getToUserId());
                if (channel != null && channel.isActive()) {
                    channel.writeAndFlush(new TextWebSocketFrame(messageStr));
                    log.info("私聊[{}]发送成功: {}", singleMsg.getToUserId(), message.getType());
                }
            }
        } catch (Exception e) {
            log.error("MQ消费异常", e);
        }
    }
}
