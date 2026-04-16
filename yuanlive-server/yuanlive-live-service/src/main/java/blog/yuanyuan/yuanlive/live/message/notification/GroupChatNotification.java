package blog.yuanyuan.yuanlive.live.message.notification;

import blog.yuanyuan.yuanlive.live.constant.MsgType;
import blog.yuanyuan.yuanlive.live.message.Message;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class GroupChatNotification extends Message {
    private ChatData data;

    public GroupChatNotification() {
        super(MsgType.CHAT_NOTIFY);
    }

    @Data
    public static class ChatData {
        private String type;          // 消息类型：text(普通), gift(礼物)
        private String roomId;
        private String user;          // 用户名
        private Long userid;          // 用户ID
        private String content;       // 普通消息内容
        private Boolean isVip;        // 是否VIP
        private Integer level;        // 用户等级
        // 礼物相关字段
        private String giftIcon;      // 礼物图标
        private Integer giftCount;    // 礼物数量
    }

    @Override
    public MsgType getCmd() {
        return MsgType.CHAT;
    }
}
