package blog.yuanyuan.yuanlive.user.service;

import blog.yuanyuan.yuanlive.user.domain.dto.FileUploadDTO;

import java.io.IOException;

public interface MinioService {
    String upload(FileUploadDTO fileUploadDTO);
}
