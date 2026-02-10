package blog.yuanyuan.yuanlive.live.util;

import blog.yuanyuan.yuanlive.live.properties.LiveRoomProperties;
import blog.yuanyuan.yuanlive.live.properties.LiveWeightsProperties;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Component
public class PopularityUtil {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private LiveRoomProperties liveRoomProperties;
    @Resource(name = "endLiveScript")
    private DefaultRedisScript<Long> endLiveScript;
    @Resource(name = "updatePopularityScript")
    private DefaultRedisScript<Long> updatePopularityScript;

    public void updatePopularity(String roomId, double increment) {
        String sessionKey = liveRoomProperties.getSessionPrefix() + roomId;
        stringRedisTemplate.execute(
                updatePopularityScript,
                Arrays.asList(liveRoomProperties.getMainRank(),
                        liveRoomProperties.getCategoryRank(),
                        sessionKey),
                roomId,
                String.valueOf(increment)
        );
    }

    public void endLive(String roomId) {
        String sessionKey = liveRoomProperties.getSessionPrefix() + roomId;
        stringRedisTemplate.execute(
                endLiveScript,
                Arrays.asList(liveRoomProperties.getMainRank(),
                        liveRoomProperties.getCategoryRank(),
                        sessionKey),
                roomId
        );
    }
}
