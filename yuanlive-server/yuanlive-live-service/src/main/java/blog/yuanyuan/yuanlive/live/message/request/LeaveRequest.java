package blog.yuanyuan.yuanlive.live.message.request;

import blog.yuanyuan.yuanlive.live.constant.MsgType;
import blog.yuanyuan.yuanlive.live.message.Message;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * 离开房间消息
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class LeaveRequest extends Message {
    public LeaveRequest() {
        super(MsgType.LEAVE_ROOM);
    }

    private LeaveData data;

    @Data
    public static class LeaveData {
        private String roomId;
        private String device;
    }

    @Override
    public MsgType getCmd() {
        return MsgType.LEAVE_ROOM;
    }
}
