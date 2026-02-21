package blog.yuanyuan.yuanlive.live.constant;

public enum MsgType {
    JOIN,  // 加入房间
    JOIN_RESP,
    LEAVE,
    PING,   // 心跳
    PONG,   // 心跳响应
    CHAT,   // 群聊天消息
    CHAT_NOTIFY,    // 广播群聊通知
    SINGLE_CHAT,   // 私聊消息
    ACK,     // 消息确认
    LIVE_START, // 开始直播
    LIVE_END,   // 结束直播
    SYSTEM_NOTIFY
}
