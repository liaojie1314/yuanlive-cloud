package blog.yuanyuan.yuanlive.ai.checkpointSaver;

import com.alibaba.cloud.ai.graph.checkpoint.BaseCheckpointSaver;
import com.alibaba.cloud.ai.graph.checkpoint.savers.redis.RedisSaver;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class CheckpointSaver {
    @Bean
    public BaseCheckpointSaver redisSaver(RedissonClient redisson) {
        return CustomRedisSaver.builder()
                .redisson(redisson).build();
    }
}
