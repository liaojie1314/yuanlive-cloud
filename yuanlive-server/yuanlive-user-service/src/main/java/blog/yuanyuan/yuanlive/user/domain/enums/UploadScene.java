package blog.yuanyuan.yuanlive.user.domain.enums;

import blog.yuanyuan.yuanlive.common.exception.ApiException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UploadScene {
    AVATAR("avatar", "头像", 2 * 1024 * 1024L, false),
    LIVE_COVER("liveCover", "直播封面", 2 * 1024 * 1024L, false),
    VIDEO_COVER("videoCover", "视频封面", 2 * 1024 * 1024L, false),
    COMMENT("comment", "评论图片", 2 * 1024 * 1024L, false),
    VIDEO("video", "视频", 16 * 1024 * 1024L, true);

    private final String code;
    private final String desc;
    private final Long chunkSize;
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
