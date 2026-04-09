package blog.yuanyuan.yuanlive.live.message.notification;

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
public class SystemNotification extends Message {
    private SystemData data;

    public SystemNotification() {
        super(MsgType.SYSTEM_MSG);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SystemData {
        private String type;        // level_up, contribution
        private String user;        // 用户名
        private Long userid;        // 用户ID
        private String content;     // 消息内容
        private Boolean isVip;      // 是否VIP
        private Integer level;      // 等级
    }

    @Override
    public MsgType getCmd() {
        return MsgType.SYSTEM_MSG;
    }
}
