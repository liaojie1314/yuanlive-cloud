package blog.yuanyuan.yuanlive.live.message.notification;

import blog.yuanyuan.yuanlive.live.constant.MsgType;
import blog.yuanyuan.yuanlive.live.message.Message;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * 结束直播消息
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class LiveEndMessage extends Message {

    public LiveEndMessage() {
        super(MsgType.LIVE_END);
    }

    private String reason;
    private Long duration; //直播时长 秒

    @Override
    public MsgType getType() {
        return MsgType.LIVE_END;
    }
}