package blog.yuanyuan.yuanlive.ai.checkpointSaver;

import com.alibaba.cloud.ai.graph.checkpoint.BaseCheckpointSaver;
import com.mongodb.client.MongoClient;
import jakarta.annotation.Resource;
import org.redisson.api.RedissonClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class CheckpointSaver {

    @Bean
    public BaseCheckpointSaver redisSaver(RedissonClient redisson) {
        return CustomRedisSaver.builder()
                .redisson(redisson).build();
    }

    @Bean
    public BaseCheckpointSaver mongodbSaver(MongoClient mongoClient,
                                            @Qualifier("titleModel") ChatModel titleModel) {
        return AiChatMongoSaver.builder()
                .client(mongoClient)
                .titleModel(titleModel)
                .build();
    }
}
