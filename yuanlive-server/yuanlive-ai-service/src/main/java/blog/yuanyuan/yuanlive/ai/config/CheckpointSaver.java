package blog.yuanyuan.yuanlive.ai.config;

import com.alibaba.cloud.ai.graph.checkpoint.BaseCheckpointSaver;
import com.alibaba.cloud.ai.graph.checkpoint.savers.redis.RedisSaver;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class CheckpointSaver {
    @Bean
    public BaseCheckpointSaver redisSaver(RedissonClient redisson) {
        return RedisSaver.builder()
                .redisson(redisson).build();
    }
}
