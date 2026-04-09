package blog.yuanyuan.yuanlive.live.message.notification;

import blog.yuanyuan.yuanlive.live.constant.MsgType;
import blog.yuanyuan.yuanlive.live.message.Message;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 事件消息（进入房间、点赞、送礼等）
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class EventMessage extends Message {
    private EventData data;

    public EventMessage() {
        super(MsgType.EVENT);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EventData {
        private String type;          // 事件类型：enter_room, like, gift
        private String roomId;
        private String user;          // 用户名
        private Long userid;          // 用户ID
        private Boolean isVip;        // 是否VIP
        private Integer level;        // 用户等级
        // 礼物相关字段（type=gift时）
        private String giftName;      // 礼物名称
        private Integer giftCount;    // 礼物数量
        private String giftIcon;      // 礼物图标
    }

    @Override
    public MsgType getCmd() {
        return MsgType.EVENT;
    }
}
