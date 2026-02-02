package blog.yuanyuan.yuanlive.live.message.notification;

import blog.yuanyuan.yuanlive.live.constant.MsgType;
import blog.yuanyuan.yuanlive.live.message.Message;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * 开始直播消息
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class LiveStartMessage extends Message {

    public LiveStartMessage() {
        super(MsgType.LIVE_START);
    }
    private String title;
    private String coverImage;
    private String category;
    private String anchorName;

    @Override
    public MsgType getType() {
        return MsgType.LIVE_START;
    }
}