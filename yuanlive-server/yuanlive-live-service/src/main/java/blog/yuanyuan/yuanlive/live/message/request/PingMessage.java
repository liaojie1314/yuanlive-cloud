package blog.yuanyuan.yuanlive.live.message.request;

import blog.yuanyuan.yuanlive.live.constant.MsgType;
import blog.yuanyuan.yuanlive.live.message.Message;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * 心跳消息
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class PingMessage extends Message {

    public PingMessage() {
        super(MsgType.PING);
    }

    @Override
    public MsgType getType() {
        return MsgType.PING;
    }
}