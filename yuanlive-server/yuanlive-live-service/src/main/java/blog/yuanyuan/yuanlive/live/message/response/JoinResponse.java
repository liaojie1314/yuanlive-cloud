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
    private Integer onlineCount; // 房间实时人数
    private String roomTitle;    // 房间标题
    private String anchorName;   // 主播名字

    public JoinResponse() {
        super(MsgType.JOIN_RESP);
    }

    @Override
    public MsgType getType() {
        return MsgType.JOIN_RESP;
    }
}
