package blog.yuanyuan.yuanlive.live.model;

import blog.yuanyuan.yuanlive.live.constant.MsgType;
import lombok.Data;

@Data
public class IMPacket {
    private MsgType type;     // 消息类型
    private String msgId;     // 消息ID (前端生成的UUID)
    private String roomId;    // 房间ID
    private Long userId;      // 用户ID
    private Object data;      // 内容
    private Long timestamp;   // 时间戳
}
