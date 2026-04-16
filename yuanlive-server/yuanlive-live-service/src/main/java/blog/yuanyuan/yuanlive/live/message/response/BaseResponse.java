package blog.yuanyuan.yuanlive.live.message.response;

import blog.yuanyuan.yuanlive.live.constant.MsgType;
import blog.yuanyuan.yuanlive.live.message.Message;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BaseResponse extends Message {
    private Integer code; // 状态码
    private String message;
    private Boolean success;

    public BaseResponse(MsgType msgType) {
        super(msgType);
    }
}
