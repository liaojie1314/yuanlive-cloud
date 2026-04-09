package blog.yuanyuan.yuanlive.live.constant;

public enum MsgType {
    JOIN_ROOM,  // 加入房间
    JOIN_RESP,
    LEAVE_ROOM,
    PING,   // 心跳
    PONG,   // 心跳响应
    CHAT,   // 群聊天消息（包括普通消息和礼物消息）
    CHAT_NOTIFY,
    JOIN_NOTIFY,
    SINGLE_CHAT,   // 私聊消息
    ACK,     // 消息确认
    LIVE_START, // 开始直播
    LIVE_END,   // 结束直播
    SYSTEM_MSG,  // 系统消息（等级提升、贡献值等）
    EVENT,      // 事件消息（进入房间、点赞、送礼等）
    LIKE,
}
