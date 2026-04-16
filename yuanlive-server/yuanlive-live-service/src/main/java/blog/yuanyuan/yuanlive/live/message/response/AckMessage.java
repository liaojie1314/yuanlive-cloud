package blog.yuanyuan.yuanlive.live.message.response;

import blog.yuanyuan.yuanlive.live.constant.MsgType;
import blog.yuanyuan.yuanlive.live.message.Message;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * 消息确认
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class AckMessage extends BaseResponse {

    public AckMessage() {
        super(MsgType.ACK);
    }

    /**
     * 确认的消息ID
     */
    private String ackMsgId;

    /**
     * 确认状态
     */
    private Boolean success;

    @Override
    public MsgType getCmd() {
        return MsgType.ACK;
    }
}