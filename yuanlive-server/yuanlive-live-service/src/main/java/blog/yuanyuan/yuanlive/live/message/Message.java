package blog.yuanyuan.yuanlive.live.message;

import blog.yuanyuan.yuanlive.live.constant.MsgType;
import blog.yuanyuan.yuanlive.live.message.notification.GroupChatNotification;
import blog.yuanyuan.yuanlive.live.message.notification.LiveEndMessage;
import blog.yuanyuan.yuanlive.live.message.notification.LiveStartMessage;
import blog.yuanyuan.yuanlive.live.message.notification.SystemNotification;
import blog.yuanyuan.yuanlive.live.message.request.*;
import blog.yuanyuan.yuanlive.live.message.response.AckMessage;
import blog.yuanyuan.yuanlive.live.message.response.JoinResponse;
import blog.yuanyuan.yuanlive.live.message.response.PongMessage;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 消息抽象父类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = JoinRequest.class, name = "JOIN"),
        @JsonSubTypes.Type(value = JoinResponse.class, name = "JOIN_RESP"),
        @JsonSubTypes.Type(value = LeaveRequest.class, name = "LEAVE"),
        @JsonSubTypes.Type(value = PingMessage.class, name = "PING"),
        @JsonSubTypes.Type(value = PongMessage.class, name = "PONG"),
        @JsonSubTypes.Type(value = GroupChatRequest.class, name = "CHAT"),
        @JsonSubTypes.Type(value = GroupChatNotification.class, name = "CHAT_NOTIFY"),
        @JsonSubTypes.Type(value = SingleChatRequest.class, name = "SINGLE_CHAT"),
        @JsonSubTypes.Type(value = AckMessage.class, name = "ACK"),
        @JsonSubTypes.Type(value = LiveStartMessage.class, name = "LIVE_START"),
        @JsonSubTypes.Type(value = LiveEndMessage.class, name = "LIVE_END"),
        @JsonSubTypes.Type(value = SystemNotification.class, name = "SYSTEM_NOTIFY")
})
public abstract class Message {
    @Getter
    private MsgType type;     // 消息类型
    private String msgId;     // 消息ID (前端生成的UUID)
    private String roomId;    // 房间ID
    private Long userId;      // 用户ID
    private Long timestamp;   // 时间戳

    public Message(MsgType type) {
        this.type = type;
        this.timestamp = System.currentTimeMillis();
    }

}