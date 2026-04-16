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
        super(MsgType.JOIN_ROOM);
    }

    private JoinData data;

    @Data
    public static class JoinData {
        private String roomId;
        private String device;
    }

    @Override
    public MsgType getCmd() {
        return MsgType.JOIN_ROOM;
    }
}