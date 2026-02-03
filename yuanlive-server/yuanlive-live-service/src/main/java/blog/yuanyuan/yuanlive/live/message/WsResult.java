package blog.yuanyuan.yuanlive.live.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WsResult {
    private String type;
    private Object data;

    public static WsResult of(Message message) {
        return new WsResult(message.getType().name(), message);
    }
}
