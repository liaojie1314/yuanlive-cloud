package blog.yuanyuan.yuanlive.live.message.request;

import blog.yuanyuan.yuanlive.live.constant.MsgType;
import blog.yuanyuan.yuanlive.live.message.Message;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * 聊天消息（包括普通消息和礼物消息）
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class GroupChatRequest extends Message {

    public GroupChatRequest() {
        super(MsgType.CHAT);
    }
    
    private ChatData data;

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