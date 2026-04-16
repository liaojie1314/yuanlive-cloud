package blog.yuanyuan.yuanlive.common.util;

import blog.yuanyuan.yuanlive.common.domain.BucketPolicyConfigDTO;
import blog.yuanyuan.yuanlive.common.exception.ApiException;
import blog.yuanyuan.yuanlive.common.properties.MinioProperties;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "minio.endpoint")
public class MinioTemplate {
    private final MinioClient minioClient;
    private final MinioProperties properties;

    /**
     * 1. 内部辅助：自动拼装业务路径
     * 生成格式：业务名/2026-02-06/文件名
     */
    private String getFullObjectName(String business, String fileName) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return business + "/" + date + "/" + fileName;
    }

    private void makeBucket(String bucketName) throws Exception {
        boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!found) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            BucketPolicyConfigDTO configDto = createBucketPolicyConfigDto(bucketName);
            SetBucketPolicyArgs policyArgs = SetBucketPolicyArgs.builder()
                    .bucket(bucketName)
                    .config(JSONUtil.toJsonStr(configDto))
                    .build();
            minioClient.setBucketPolicy(policyArgs);
            // 在创建时设置chunk分片目录过期时间
            if (properties.getBucketName().equals(bucketName)) {
                List<LifecycleRule> rules = new ArrayList<>();
                rules.add(new LifecycleRule(
                        Status.ENABLED,
                        null,
                        new Expiration((ResponseDate) null, 1, (Boolean) null), // 2天后过期
                        new RuleFilter("chunks/"), // 只针对分片目录
                        "delete-temp-chunks",
                        null,
                        null,
                        null
                ));
                LifecycleConfiguration config = new LifecycleConfiguration(rules);
                minioClient.setBucketLifecycle(
                        SetBucketLifecycleArgs.builder()
                                .bucket(bucketName)
                                .config(config)
                                .build()
                );
            }
        }
    }

    private BucketPolicyConfigDTO createBucketPolicyConfigDto(String bucketName) {
        BucketPolicyConfigDTO.Statement statement = BucketPolicyConfigDTO.Statement.builder()
                .Effect("Allow")
                .Principal("*")
                .Action("s3:GetObject")
                .Resource("arn:aws:s3:::" + bucketName + "/*.**").build();
        return BucketPolicyConfigDTO.builder()
                .Version("2012-10-17")
                .Statement(CollUtil.toList(statement))
                .build();
    }

    public String uploadLocalFile(String business, String fileName, String localFilePath) throws Exception {
        makeBucket(properties.getBucketName());

        // 自动构造逻辑路径
        String objectName = getFullObjectName(business, fileName);

        minioClient.uploadObject(
                UploadObjectArgs.builder()
                        .bucket(properties.getBucketName())
                        .object(objectName)
                        .filename(localFilePath)
                        .contentType(getContentType(fileName)) // 自动识别类型
                        .build()
        );

        return getFileUrl(objectName);
    }

    /**
     * 3. 流上传
     */
    public String uploadInputStream(String business, String fileName, InputStream inputStream, long size) throws Exception {
        makeBucket(properties.getBucketName());

        String objectName = getFullObjectName(business, fileName);

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(properties.getBucketName())
                        .object(objectName)
                        .stream(inputStream, size, -1)
                        .contentType(getContentType(fileName))
                        .build()
        );
        return getFileUrl(objectName);
    }

    public String uploadChunkFile(String business, String fileName, InputStream inputStream, long size) throws Exception {
        makeBucket(properties.getBucketName());
        String objectName = business + "/" + fileName;
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(properties.getBucketName())
                        .object(objectName)
                        .stream(inputStream, size, -1)
                        .contentType(getContentType(fileName))
                        .build()
        );
        return objectName;
    }

    public String composeChunks(String business, List<String> chunkObjects, String targetName) {
        try {
            List<ComposeSource> sources = chunkObjects.stream()
                    .map(chunk -> ComposeSource.builder()
                            .bucket(properties.getBucketName())
                            .object(chunk)
                            .build()).toList();
            String target = getFullObjectName(business, targetName);
            minioClient.composeObject(
                    ComposeObjectArgs.builder()
                            .bucket(properties.getBucketName())
                            .object(target)
                            .sources(sources)
                            .build()
            );
            // 清空临时分片
            chunkObjects.forEach(this::removeFile);
            return getFileUrl(target);
        } catch (Exception e) {
            throw new ApiException("文件合并失败:" + e.getMessage());
        }
    }

    public String manualStreamMerge(String business, List<String> chunkObjects, String targetName) {
        try {
            List<InputStream> inputStreams = new ArrayList<>();
            long totalSize = 0;
            for (String objectName : chunkObjects) {
                // 获取对象状态以取得准确大小
                StatObjectResponse stat = minioClient.statObject(
                        StatObjectArgs.builder().bucket(properties.getBucketName()).object(objectName).build()
                );
                totalSize += stat.size();
                InputStream stream = minioClient.getObject(
                        GetObjectArgs.builder().bucket(properties.getBucketName()).object(objectName).build()
                );
                inputStreams.add(stream);
            }
            Enumeration<InputStream> en = Collections.enumeration(inputStreams);
            SequenceInputStream sis = new SequenceInputStream(en);

            // 重新上传完整文件
            String targetPath = getFullObjectName(business, targetName);
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(properties.getBucketName())
                            .object(targetPath)
                            .stream(sis, totalSize, -1)
                            .contentType(getContentType(targetName))
                            .build()
            );

            // 清理分片并关闭流
            sis.close();
            chunkObjects.forEach(this::removeFile);
            return getFileUrl(targetPath);
        } catch (Exception e) {
            throw new ApiException("流式合并异常: " + e.getMessage());
        }
    }

    /**
     * 4. 获取文件外网访问地址
     */
    public String getFileUrl(String objectName) {
        // 确保路径拼接时不会出现双斜杠
        String baseUrl = properties.getReadPath().endsWith("/") ?
                properties.getReadPath().substring(0, properties.getReadPath().length() - 1) :
                properties.getReadPath();

        return baseUrl + "/" + properties.getBucketName() + "/" + objectName;
    }

    /**
     * 5. 删除文件 (objectName 需包含业务前缀)
     */
    public void removeFile(String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(properties.getBucketName())
                            .object(objectName)
                            .build()
            );
            log.info("MinIO 文件已删除: {}", objectName);
        } catch (Exception e) {
            throw new ApiException("文件删除失败:" + e.getMessage());
        }
    }

    private String getContentType(String fileName) {
        if (fileName.endsWith(".mp4")) return "video/mp4";
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) return "image/jpeg";
        if (fileName.endsWith(".png")) return "image/png";
        return "application/octet-stream";
    }
}
