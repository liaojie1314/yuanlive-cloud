package blog.yuanyuan.yuanlive.live.message.notification;

import blog.yuanyuan.yuanlive.live.constant.MsgType;
import blog.yuanyuan.yuanlive.live.message.Message;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class GroupChatNotification extends Message {
    private String content;
    private String username; // 后端补全：发送者昵称
    private String avatar;   // 后端补全：发送者头像
    private Integer level;   // 用户等级

    public GroupChatNotification() {
        super(MsgType.CHAT_NOTIFY);
    }

    @Override
    public MsgType getType() {
        return MsgType.CHAT_NOTIFY;
    }
}
