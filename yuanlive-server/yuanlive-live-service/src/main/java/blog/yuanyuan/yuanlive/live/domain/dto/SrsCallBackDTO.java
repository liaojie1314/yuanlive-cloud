package blog.yuanyuan.yuanlive.live.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Srs回调参数")
public class SrsCallBackDTO {
    // 回调事件类型：on_publish, on_unpublish, on_play, on_stop 等
    private String action;

    // SRS 为每个客户端连接生成的唯一标识
    private String client_id;

    // 推流端或播放端的 IP 地址
    private String ip;

    // 虚拟主机名
    private String vhost;

    // 应用名，例如推流地址 rtmp://server/live/1001 中的 "live"
    private String app;

    // 流名，在你的项目中通常对应 userId 或 roomId
    private String stream;

    // URL 问号后面的参数字符串，例如 "?token=abc&key=123"
    private String param;

    // SRS 服务器自身的标识，多节点集群时很有用
    private String server_id;

    private String file;
}
