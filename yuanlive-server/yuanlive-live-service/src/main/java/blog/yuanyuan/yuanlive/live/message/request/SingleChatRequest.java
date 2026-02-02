package blog.yuanyuan.yuanlive.live.message.request;

import blog.yuanyuan.yuanlive.live.constant.MsgType;
import blog.yuanyuan.yuanlive.live.message.Message;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class SingleChatRequest extends Message {
    private Long toUserId;
    private String content;

    public SingleChatRequest() {
        super(MsgType.SINGLE_CHAT);
    }

    @Override
    public MsgType getType() {
        return MsgType.SINGLE_CHAT;
    }
}
