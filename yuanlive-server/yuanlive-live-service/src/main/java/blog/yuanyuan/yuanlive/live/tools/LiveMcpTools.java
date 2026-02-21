package blog.yuanyuan.yuanlive.live.tools;

import blog.yuanyuan.yuanlive.live.message.notification.SystemNotification;
import blog.yuanyuan.yuanlive.live.properties.LiveRoomProperties;
import blog.yuanyuan.yuanlive.live.server.SessionManager;
import cn.hutool.json.JSONUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LiveMcpTools {
    @Resource
    private SessionManager sessionManager;
    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private LiveRoomProperties liveRoomProperties;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Value("${live.mq.chat.exchange}")
    private String exchange;

    @Tool(description = "在直播间公屏发布系统公告，警告全体观众")
    public String broadcast(@ToolParam(description = "直播间房间ID") String roomId) {
        if (!checkRoom(roomId)) {
            return "直播间不存在";
        }
        String notice = "【系统公告】文明发言，共建和谐直播间。恶意违规者将被永久封禁。";
        SystemNotification notification = SystemNotification.builder()
                .content(notice)
                .roomId(roomId)
                .build();
        rabbitTemplate.convertAndSend(exchange, "", JSONUtil.toJsonStr(notification));
        return "全体广播已发送至房间: " + roomId;
    }

    @Tool(description = "向指定直播间的主播发送私密警告消息")
    public String warnAnchor(@ToolParam(description = "直播间房间id") String roomId) {
        log.info("warnAnchor roomId: {}", roomId);
        if (!checkRoom(roomId)) {
            return "直播间不存在";
        }
        // 根据房间ID获取主播uid
        String sessionKey = liveRoomProperties.getSessionPrefix() + roomId;
        String anchorId = (String) stringRedisTemplate.opsForHash().get(sessionKey, "anchorId");
        if (anchorId == null) {
            return "直播间不存在";
        }
        long uid = Long.parseLong(anchorId);
        String warningMsg = "【系统警告】检测到您的直播间弹幕存在违规倾向，请加强管理，否则将面临封禁。";
        SystemNotification notification = SystemNotification.builder()
                .content(warningMsg)
                .toUserId(uid)
                .build();
        rabbitTemplate.convertAndSend(exchange, "", JSONUtil.toJsonStr(notification));
        return "警告已发送至主播: " + uid;
    }

    // 判断直播间是否存在
    private boolean checkRoom(String roomId) {
        return stringRedisTemplate.hasKey(liveRoomProperties.getSessionPrefix() + roomId);
    }
}
