package blog.yuanyuan.yuanlive.live.message.request;

import blog.yuanyuan.yuanlive.live.constant.MsgType;
import blog.yuanyuan.yuanlive.live.message.Message;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class LikeRequest extends Message {

    public LikeRequest() {
        super(MsgType.LIKE);
    }

    private LikeData data;

    @Data
    public static class LikeData {
        private String roomId;
        private Integer count;
    }

    @Override
    public MsgType getCmd() {
        return MsgType.LIKE;
    }
}
