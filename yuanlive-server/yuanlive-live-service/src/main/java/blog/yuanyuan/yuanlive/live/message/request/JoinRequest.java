package blog.yuanyuan.yuanlive.live.message.request;

import blog.yuanyuan.yuanlive.live.constant.MsgType;
import blog.yuanyuan.yuanlive.live.message.Message;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * 加入房间消息
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class JoinRequest extends Message {
    
    public JoinRequest() {
        super(MsgType.JOIN);
    }

    @Override
    public MsgType getType() {
        return MsgType.JOIN;
    }
}