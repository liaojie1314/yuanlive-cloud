package blog.yuanyuan.yuanlive.common.util;

import blog.yuanyuan.yuanlive.common.domain.BucketPolicyConfigDTO;
import blog.yuanyuan.yuanlive.common.properties.MinioProperties;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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
    public void removeFile(String objectName) throws Exception {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(properties.getBucketName())
                        .object(objectName)
                        .build()
        );
        log.info("MinIO 文件已删除: {}", objectName);
    }

    private String getContentType(String fileName) {
        if (fileName.endsWith(".mp4")) return "video/mp4";
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) return "image/jpeg";
        if (fileName.endsWith(".png")) return "image/png";
        return "application/octet-stream";
    }
}
