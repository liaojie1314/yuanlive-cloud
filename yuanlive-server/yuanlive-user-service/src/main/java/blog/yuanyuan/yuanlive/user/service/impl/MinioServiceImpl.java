package blog.yuanyuan.yuanlive.user.service.impl;

import blog.yuanyuan.yuanlive.common.exception.ApiException;
import blog.yuanyuan.yuanlive.common.util.MinioTemplate;
import blog.yuanyuan.yuanlive.user.domain.dto.FileUploadDTO;
import blog.yuanyuan.yuanlive.user.service.MinioService;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Service
@Slf4j
public class MinioServiceImpl implements MinioService {
    @Resource
    private MinioTemplate minioTemplate;

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
}
