package blog.yuanyuan.yuanlive.live.message.response;

import blog.yuanyuan.yuanlive.live.constant.MsgType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class JoinResponse extends BaseResponse {

    public JoinResponse() {
        super(MsgType.JOIN_RESP);
    }

    private JoinData data;

    @Data
    public static class JoinData {
        private String roomId;       // 房间ID
        private String roomTitle;    // 房间标题
        private String anchorName;   // 主播名字
        private Integer onlineCount; // 房间实时人数[cite: 14]
    }

    @Override
    public MsgType getCmd() {
        return MsgType.JOIN_RESP;
    }
}
