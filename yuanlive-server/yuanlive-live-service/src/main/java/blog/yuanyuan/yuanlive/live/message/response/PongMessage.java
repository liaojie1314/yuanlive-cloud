package blog.yuanyuan.yuanlive.live.message.response;

import blog.yuanyuan.yuanlive.live.constant.MsgType;
import blog.yuanyuan.yuanlive.live.message.Message;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * 心跳响应消息
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class PongMessage extends Message {

    public PongMessage() {
        super(MsgType.PONG);
    }

    @Override
    public MsgType getType() {
        return MsgType.PONG;
    }
}