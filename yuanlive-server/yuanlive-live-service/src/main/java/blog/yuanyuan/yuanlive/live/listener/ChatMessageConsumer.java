package blog.yuanyuan.yuanlive.live.listener;

import blog.yuanyuan.yuanlive.live.model.IMPacket;
import blog.yuanyuan.yuanlive.live.server.SessionManager;
import cn.hutool.json.JSONUtil;
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

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(
                    value = "", // 留空，让 RabbitMQ 生成随机名字 (e.g. amq.gen-JzTY...)
                    autoDelete = "true", // 关机即删
                    exclusive = "true"   // 排他队列
            ),
            exchange = @Exchange(value = "${yuanlive.chat.mq.exchange}",
                    type = ExchangeTypes.FANOUT)
    ))
    public void onMessage(String message) {
        try {
            IMPacket packet = JSONUtil.toBean(message, IMPacket.class);
            String roomId = packet.getRoomId();

            // 查找本机是否有该房间的连接
            ChannelGroup group = sessionManager.getRoomChannels(roomId);

            if (group != null && !group.isEmpty()) {
                // 直接向组内所有客户端广播
                group.writeAndFlush(new TextWebSocketFrame(message));
                log.debug("MQ消息已推送到房间: {}, 人数: {}", roomId, group.size());
            }
        } catch (Exception e) {
            log.error("MQ消费异常", e);
        }
    }
}
