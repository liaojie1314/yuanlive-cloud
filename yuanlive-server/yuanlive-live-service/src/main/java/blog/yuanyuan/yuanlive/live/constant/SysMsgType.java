package blog.yuanyuan.yuanlive.live.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SysMsgType {
    ANNOUNCEMENT("announcement", "直播间公告"),
    USER_JOIN("user_join", "用户进入通知"),
    WARNING("warning", "违规警告");

    private final String code;
    private final String desc;
}
