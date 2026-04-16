package blog.yuanyuan.yuanlive.entity.live.dto;

import lombok.Data;

import java.util.List;

@Data
public class FollowUnseenQueryDTO {
    private Long followingId; // 关注的用户ID列表
    private Long lastReadVideoId; // 用户最后阅读的视频ID列表
}
