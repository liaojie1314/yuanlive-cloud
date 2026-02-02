package blog.yuanyuan.yuanlive.live.message.request;

import blog.yuanyuan.yuanlive.live.constant.MsgType;
import blog.yuanyuan.yuanlive.live.message.Message;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * 聊天消息
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class GroupChatRequest extends Message {

    public GroupChatRequest() {
        super(MsgType.CHAT);
    }
    private String content;

    @Override
    public MsgType getType() {
        return MsgType.CHAT;
    }
}