package blog.yuanyuan.yuanlive.ai.strategy;

import com.alibaba.cloud.ai.graph.KeyStrategy;
import com.alibaba.cloud.ai.graph.KeyStrategyFactory;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;

@Slf4j
public class RiskStrategies {
    public static final String  RISK_SCORE = "risk_score";
    public static final String  ROOM_ID = "room_id";
    public static final String  ADMIN_APPROVED = "admin_approved";
    public static final String  CHAT_HISTORY = "chat_history";

    public static KeyStrategyFactory create() {
        return () -> {
            HashMap<String, KeyStrategy> map = new HashMap<>();
            map.put(RISK_SCORE, KeyStrategy.REPLACE);
            map.put(ROOM_ID, KeyStrategy.REPLACE);
            map.put(ADMIN_APPROVED, KeyStrategy.REPLACE);
            map.put(CHAT_HISTORY, KeyStrategy.APPEND);
            return map;
        };
    }

    // 清理 redis 中的短期工作流内容
    public static void cleanRedis(String threadName, StringRedisTemplate stringRedisTemplate) {
        String metaKey = "graph:thread:meta:" + threadName;
        String threadId = (String) stringRedisTemplate
                .opsForHash().get(metaKey, "\"thread_id\"");
        threadId = threadId.substring(1, threadId.length() - 1);
        log.info("threadId: {}", threadId);
        String contentKey = "graph:checkpoint:content:" + threadId;
        String reverseKey = "graph:thread:reverse:" + threadId;
        stringRedisTemplate.delete(List.of(contentKey, reverseKey, metaKey));
    }
}
