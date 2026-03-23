package blog.yuanyuan.yuanlive.user.domain.enums;

import blog.yuanyuan.yuanlive.common.exception.ApiException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UploadScene {
    AVATAR("avatar", "头像", 512 * 1024L, 2 * 1024 * 1024L, false),
    LIVE_COVER("liveCover", "直播封面", 512 * 1024L, 2 * 1024 * 1024L, false),
    VIDEO_COVER("videoCover", "视频封面", 512 * 1024L, 2 * 1024 * 1024L, false),
    COMMENT("comment", "评论图片", 512 * 1024L, 2 * 1024 * 1024L, false),
    VIDEO("video", "视频", 5 * 1024 * 1024L, 10 * 1024 * 1024L, true);

    private final String code;
    private final String desc;
    private final long minChunkSize; // 建议分片大小
    private final long maxChunkSize;
    private final boolean useMinioCompose; // 是否支持 MinIO 服务端合并

    public static UploadScene of(String code) {
        for (UploadScene scene : values()) {
            if (scene.code.equalsIgnoreCase(code)) {
                return scene;
            }
        }
        throw new ApiException("非法的上传场景: " + code);
    }
}
