package blog.yuanyuan.yuanlive.live.message.notification;

import blog.yuanyuan.yuanlive.live.constant.MsgType;
import blog.yuanyuan.yuanlive.live.message.Message;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class SystemNotification extends Message {
    private String content;
    private Long toUserId;

    @Override
    public MsgType getType() {
        return MsgType.SYSTEM_NOTIFY;
    }
}
