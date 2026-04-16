package blog.yuanyuan.yuanlive.live.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ws.schild.jave.EncoderException;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.ScreenExtractor;
import ws.schild.jave.info.MultimediaInfo;
import ws.schild.jave.info.VideoInfo;

import java.io.File;

@Slf4j
@Component
public class VideoProcessUtil {

    public VideoProcessResult processVideo(File video) throws EncoderException {
        if (video == null || !video.exists()) {
            return VideoProcessResult.builder().success(false).build();
        }
        MultimediaObject object = new MultimediaObject(video);
        MultimediaInfo info = object.getInfo();
        long duration = info.getDuration();
        VideoInfo videoInfo = info.getVideo();
        Integer width = videoInfo.getSize().getWidth();
        Integer height = videoInfo.getSize().getHeight();
        // 准备封面文件
        String coverPath = video.getAbsolutePath()
                .substring(0, video.getAbsolutePath().lastIndexOf(".")) + ".jpg";
        File coverFile = new File(coverPath);
        long captureTime = duration > 3000 ? 2000 : 1000;
        boolean success = false;
        ScreenExtractor extractor = new ScreenExtractor();
        extractor.renderOneImage(object, -1, -1, captureTime, coverFile, 2);
        success = coverFile.exists();
        return VideoProcessResult.builder()
                .duration(duration / 1000)
                .width(width)
                .height(height)
                .coverFile(coverFile)
                .success(success)
                .build();
    }
}
