package blog.yuanyuan.yuanlive.user.listener;

import blog.yuanyuan.yuanlive.user.service.UserStatsService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class UserStatsMqListener {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private UserStatsService userStatsService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "${live.mq.stats.queue.video}", durable = "true"),
            exchange = @Exchange(value = "${live.mq.stats.exchange}", type = ExchangeTypes.TOPIC),
            key = "${live.mq.stats.routing-key.video}"
    ))
    public void handleVideoStats(Map<String, Object> message) {
        log.info("MQ接收 - 视频变动: {}", message);
        Long userId = Long.valueOf(message.get("userId").toString());
        String type = message.get("type").toString();
        if ("add".equals(type)) {
            userStatsService.update()
                    .setSql("video_count = video_count + 1")
                    .eq("user_id", userId)
                    .update();
            return;
        }
        if ("sub".equals(type)) {
            userStatsService.update()
                    .setSql("video_count = video_count - 1")
                    .eq("user_id", userId)
                    .update();
        }
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "${live.mq.stats.queue.like}", durable = "true"),
            exchange = @Exchange(value = "${live.mq.stats.exchange}", type = ExchangeTypes.TOPIC),
            key = "${live.mq.stats.routing-key.like}"
    ))
    public void handleLikeStats(Map<String, Object> message) {
        log.info("MQ接收 - 点赞变动: {}", message);
        Long ownerId = Long.valueOf(message.get("ownerId").toString());
        Integer change = (Integer) message.get("change");

        // A. 更新 Redis 实时显示值
        stringRedisTemplate.opsForHash().increment("user:stats:" + ownerId, "totalLikes", change);

        // B. 更新 Redis 待入库增量池
        stringRedisTemplate.opsForValue().increment("user:stats:buffer:likes:" + ownerId, change);

        // C. 标记脏数据名单
        stringRedisTemplate.opsForSet().add("user:stats:dirty_set", ownerId.toString());
    }
}
