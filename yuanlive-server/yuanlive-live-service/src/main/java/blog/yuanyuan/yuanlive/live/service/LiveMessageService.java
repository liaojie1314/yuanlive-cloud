package blog.yuanyuan.yuanlive.live.service;

import blog.yuanyuan.yuanlive.live.message.Message;
import blog.yuanyuan.yuanlive.live.message.request.*;
import io.netty.channel.ChannelHandlerContext;

public interface LiveMessageService {
    void handlePing(ChannelHandlerContext ctx, PingMessage ping);
    void handleJoinRoom(ChannelHandlerContext ctx, JoinRequest join);
    void handleChat(ChannelHandlerContext ctx, GroupChatRequest chat);
    void handleLeaveRoom(ChannelHandlerContext ctx, LeaveRequest leave);
    void handleGenericMessage(ChannelHandlerContext ctx, Message message);
    void handleDisconnect(ChannelHandlerContext ctx);
    void handleLike(ChannelHandlerContext ctx, LikeRequest like);
}
