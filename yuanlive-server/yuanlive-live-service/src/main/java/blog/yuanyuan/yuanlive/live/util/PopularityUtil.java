package blog.yuanyuan.yuanlive.live.util;

import blog.yuanyuan.yuanlive.live.domain.vo.LiveRoomRankVO;
import blog.yuanyuan.yuanlive.live.properties.LiveRoomProperties;
import cn.hutool.core.collection.CollUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.*;

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

    public List<LiveRoomRankVO> getPopularRoomVOS(List<String> roomIdList) {
        int n = roomIdList.size();
        String mainRankKey = liveRoomProperties.getMainRank();
        // 2. 使用 SessionCallback 执行管道，无需手动处理序列化
        List<Object> rawResults = stringRedisTemplate.executePipelined(new SessionCallback<Object>() {
            @Override
            public <K, V> Object execute(@NotNull RedisOperations<K, V> operations) {
                // 第一部分：批量获取 Hash (Session)
                for (String roomId : roomIdList) {
                    String sessionKey = liveRoomProperties.getSessionPrefix() + roomId;
                    operations.opsForHash().entries((K) sessionKey);
                }
                // 第二部分：批量获取 Score (人气)
                for (String roomId : roomIdList) {
                    operations.opsForZSet().score((K) mainRankKey, roomId);
                }
                return null; // 必须返回 null
            }
        });

        // 3. 解析结果 (此时 rawResults 里的对象直接就是 Map 和 Double)
        List<LiveRoomRankVO> roomVOList = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            // 获取详情 Map
            Map<String, String> sessionMap = (Map<String, String>) rawResults.get(i);
            if (CollUtil.isEmpty(sessionMap)) continue;

            // 获取人气分数
            Object scoreObj = rawResults.get(i + n);
            // 注意：zSet.score 可能返回 null 或 Double
            double popularity = (scoreObj instanceof Double score) ? score : 0.0;

            // 4. 组装 VO
            LiveRoomRankVO vo = LiveRoomRankVO.builder()
                    .id(Long.valueOf(roomIdList.get(i)))
                    .title(sessionMap.get("roomTitle"))
                    .anchorName(sessionMap.get("anchor"))
                    .coverImg(sessionMap.get("coverImg"))
                    .hotScore(popularity)
                    .build();

            roomVOList.add(vo);
        }
        // 按人气降序
        roomVOList.sort(Comparator.comparing(LiveRoomRankVO::getHotScore).reversed());
        return roomVOList;
    }

}
