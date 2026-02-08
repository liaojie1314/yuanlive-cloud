package blog.yuanyuan.yuanlive.live.util;

import lombok.Builder;
import lombok.Data;

import java.io.File;

@Data
@Builder
public class VideoProcessResult {
    private Long duration;
    private int width;
    private int height;
    private File coverFile;
    private boolean success;
}
