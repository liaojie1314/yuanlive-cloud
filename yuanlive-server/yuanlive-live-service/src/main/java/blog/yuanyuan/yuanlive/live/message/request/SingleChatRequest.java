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

    public SingleChatRequest() {
        super(MsgType.SINGLE_CHAT);
    }

    private SingleChatData data;

    @Data
    public static class SingleChatData {
        private Long toUserId;
        private String content;
    }

    @Override
    public MsgType getCmd() {
        return MsgType.SINGLE_CHAT;
    }
}
