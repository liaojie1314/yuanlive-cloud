package blog.yuanyuan.yuanlive.live.server;

import blog.yuanyuan.yuanlive.live.properties.LiveRoomProperties;
import blog.yuanyuan.yuanlive.live.properties.LiveWeightsProperties;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class SessionManager {
    // 1. 全局在线用户字典: UserId -> Channel
    private static final Map<Long, Channel> USER_MAP = new ConcurrentHashMap<>();

    // 2. 房间广播组: RoomId -> ChannelGroup
    private static final Map<String, ChannelGroup> ROOM_MAP = new ConcurrentHashMap<>();

    public static final AttributeKey<Long> KEY_USER_ID = AttributeKey.valueOf("userId");
    public static final AttributeKey<String> KEY_USER_NAME = AttributeKey.valueOf("name");
    public static final AttributeKey<String> KEY_ROOM_ID = AttributeKey.valueOf("roomId");
    public static final AttributeKey<String> KEY_DEVICE_ID = AttributeKey.valueOf("deviceId");
    public static final AttributeKey<String> KEY_TOKEN = AttributeKey.valueOf("token");
    public static final AttributeKey<String> KEY_TRACE_ID = AttributeKey.valueOf("traceId");

    @Resource(name = "leaveRoomScript")
    private DefaultRedisScript<Long> leaveRoomScript;
    @Resource
    private LiveRoomProperties liveRoomProperties;
    @Resource
    private LiveWeightsProperties liveWeightsProperties;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    /**
     * 1. 用户上线 (连接建立时调用)
     * 仅保存连接引用，不加入房间
     */
    public void register(Long userId, Channel channel) {
        USER_MAP.put(userId, channel);
        channel.attr(KEY_USER_ID).set(userId);
        log.info("用户[{}] 建立连接，当前在线人数: {}", userId, USER_MAP.size());
    }

    /**
     * 2. 用户加入房间 (收到 LOGIN/JOIN 消息时调用)
     */
    public void joinRoom(String roomId, Channel channel) {
        // 将 roomId 绑定到 Channel，方便退出时识别
        channel.attr(KEY_ROOM_ID).set(roomId);

        ChannelGroup group = ROOM_MAP.computeIfAbsent(roomId, k ->
                new DefaultChannelGroup(GlobalEventExecutor.INSTANCE)
        );
        group.add(channel);
        log.info("连接[{}] 加入房间[{}]", channel.id(), roomId);
    }

    /**
     * 3. 用户下线/断开
     */
    public void remove(Channel channel) {
        String traceId = channel.attr(KEY_TRACE_ID).get();
        MDC.put("traceId", traceId);
        try {
            Long userId = channel.attr(KEY_USER_ID).get();
            if (userId != null) {
                USER_MAP.remove(userId);
                log.info("用户[{}] 断开连接", userId);
                // 执行离场 Lua 脚本
                String roomId = channel.attr(KEY_ROOM_ID).get();
                String currentKey = liveRoomProperties.getCurrentPrefix() + roomId;
                String rankingKey = liveRoomProperties.getMainRank();
                double viewWeight = liveWeightsProperties.getView();Long remaining = stringRedisTemplate.execute(
                        leaveRoomScript, // 之前定义的 DefaultRedisScript
                        Arrays.asList(currentKey, rankingKey),
                        userId.toString(), roomId, String.valueOf(viewWeight)
                );
            }
            // Netty 的 ChannelGroup 会自动移除断开的 Channel，不需要手动操作 ROOM_MAP
        } finally {
            MDC.remove("traceId");
        }
    }

    // 获取房间组（用于广播）
    public ChannelGroup getRoomChannels(String roomId) {
        return ROOM_MAP.get(roomId);
    }

    public Channel getUserChannel(Long userId) {
        return USER_MAP.get(userId);
    }
}
