package blog.yuanyuan.yuanlive.user.service.impl;

import blog.yuanyuan.yuanlive.common.exception.ApiException;
import blog.yuanyuan.yuanlive.common.result.Result;
import blog.yuanyuan.yuanlive.common.util.MinioTemplate;
import blog.yuanyuan.yuanlive.user.domain.dto.FileCheckDTO;
import blog.yuanyuan.yuanlive.user.domain.dto.FileChunkDTO;
import blog.yuanyuan.yuanlive.user.domain.dto.FileMergeChunkDTO;
import blog.yuanyuan.yuanlive.user.domain.dto.FileUploadDTO;
import blog.yuanyuan.yuanlive.user.domain.enums.UploadScene;
import blog.yuanyuan.yuanlive.user.service.MinioService;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Service
@Slf4j
public class MinioServiceImpl implements MinioService {
    @Resource
    private MinioTemplate minioTemplate;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Value("${redis-key.chunk.prefix}")
    private String chunkPrefix;
    @Value("${redis-key.chunk.ttl}")
    private Duration chunkTtl;

    @Override
    public String upload(FileUploadDTO fileUploadDTO) {
        try {
            MultipartFile file = fileUploadDTO.getFile();
            String scene = fileUploadDTO.getScene();
            InputStream inputStream = file.getInputStream();
            String originalFilename = file.getOriginalFilename();
            String extName = FileUtil.extName(originalFilename);
            String fileName = IdUtil.simpleUUID() + "." + extName;
            return minioTemplate.uploadInputStream(scene, fileName, inputStream, file.getSize());
        } catch (Exception e) {
            throw new ApiException("文件上传失败");
        }
    }

    @Override
    public Result<String> chunk(FileChunkDTO fileChunkDTO, MultipartFile file) {
        UploadScene scene = UploadScene.of(fileChunkDTO.getScene());
        long currentChunkSize = file.getSize();
        boolean isLastChunk = fileChunkDTO.getChunkIndex().equals(fileChunkDTO.getTotalChunks() - 1);
        if (!isLastChunk && currentChunkSize != scene.getChunkSize()) {
            throw new ApiException("分片大小不符合规范");
        }
        try {
            String business = "chunks/" + fileChunkDTO.getScene() + "/" + fileChunkDTO.getFileHash();
            InputStream inputStream = file.getInputStream();
            String url = minioTemplate.uploadChunkFile(business, file.getOriginalFilename(), inputStream, file.getSize());
            String redisKey = chunkPrefix + fileChunkDTO.getFileHash();
            stringRedisTemplate.opsForHash().put(redisKey, String.valueOf(fileChunkDTO.getChunkIndex()), url);
            stringRedisTemplate.expire(redisKey, chunkTtl);
            return Result.success("文件分片上传成功");
        } catch (Exception e) {
            throw new ApiException("文件分片上传失败" + e.getMessage());
        }
    }

    @Override
    public List<Integer> check(FileCheckDTO checkDTO) {
        String redisKey = chunkPrefix + checkDTO.getFileHash();
        if (!stringRedisTemplate.hasKey(redisKey)) {
            return Collections.emptyList();
        }
        return stringRedisTemplate.opsForHash().keys(redisKey).stream()
                .map(key -> Integer.parseInt((String) key))
                .toList();
    }

    @Override
    public Result<String> merge(FileMergeChunkDTO mergeChunkDTO) {
        UploadScene scene = UploadScene.of(mergeChunkDTO.getScene());
        String redisKey = chunkPrefix + mergeChunkDTO.getFileHash();
        if (!stringRedisTemplate.hasKey(redisKey)) {
            throw new ApiException("未开始上传该文件分片或上传已超时");
        }
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(redisKey);
        if (entries.size() != mergeChunkDTO.getTotalChunks()) {
            throw new ApiException("文件分片上传不完整");
        }
        List<String> objects = IntStream.range(0, mergeChunkDTO.getTotalChunks())
                .mapToObj(i -> {
                    Object o = entries.get(String.valueOf(i));
                    if (o == null) {
                        throw new ApiException("缺少分片索引" + i);
                    }
                    return (String) o;
                }).toList();
        String url;
        if (scene.isUseMinioCompose()) {
            url = minioTemplate.composeChunks(scene.getCode(), objects, mergeChunkDTO.getFileName());
        } else {
            // 走后端手动流式合并 (兼容小分片)
            url = minioTemplate.manualStreamMerge(scene.getCode(), objects, mergeChunkDTO.getFileName());
        }
        return Result.success(url);
    }
}
