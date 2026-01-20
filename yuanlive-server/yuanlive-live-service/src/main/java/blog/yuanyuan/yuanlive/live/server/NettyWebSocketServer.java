package blog.yuanyuan.yuanlive.live.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NettyWebSocketServer {
    @Value("${yuanlive.netty.port}")
    private int port;
    @Value("${yuanlive.netty.path}")
    private String path;
    @Value("${yuanlive.chat.heartbeat}")
    private int heartbeat;
    @Resource
    private NettyServerHandler nettyServerHandler;
    @Resource
    private AuthHandshakeHandler authHandshakeHandler;

    private NioEventLoopGroup boss;
    private NioEventLoopGroup worker;

    @PostConstruct
    public void start() {
        Thread nettyThread = new Thread(() -> {
            boss = new NioEventLoopGroup(1);
            worker = new NioEventLoopGroup();
            try {
                ServerBootstrap server = new ServerBootstrap();
                server.group(boss, worker)
                        .channel(NioServerSocketChannel.class)
                        .option(ChannelOption.SO_BACKLOG, 1024)
                        .childOption(ChannelOption.SO_KEEPALIVE, true)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) {
                                ChannelPipeline pipeline = ch.pipeline();
                                // HTTP 协议解析
//                                pipeline.addFirst(new LoggingHandler(LogLevel.INFO));
                                pipeline.addLast(new HttpServerCodec());
                                pipeline.addLast(new ChunkedWriteHandler());
                                pipeline.addLast(new HttpObjectAggregator(65536));
                                pipeline.addLast(authHandshakeHandler);

                                // 心跳检测: 30秒无读操作则触发 userEventTriggered
                                pipeline.addLast(new IdleStateHandler(heartbeat, 0, 0));

                                // WebSocket 协议处理
                                pipeline.addLast(new WebSocketServerProtocolHandler(path));

                                // 业务逻辑
                                pipeline.addLast(nettyServerHandler);
                            }
                        });
                log.info("Netty WebSocket 启动成功，端口: {}", port);
                server.bind(port).sync().channel().closeFuture().sync();
            } catch (InterruptedException e) {
                log.error("Netty 启动中断", e);
                Thread.currentThread().interrupt();
            } finally {
                destroy();
            }
        }, "netty-websocket-server");
        nettyThread.setDaemon(true);
        nettyThread.start();
    }

    @PreDestroy
    public void destroy() {
        if (boss != null) boss.shutdownGracefully();
        if (worker != null) worker.shutdownGracefully();
    }
}
