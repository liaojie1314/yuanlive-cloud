package blog.yuanyuan.yuanlive.user.service;

import blog.yuanyuan.yuanlive.common.result.Result;
import blog.yuanyuan.yuanlive.user.domain.dto.FileCheckDTO;
import blog.yuanyuan.yuanlive.user.domain.dto.FileChunkDTO;
import blog.yuanyuan.yuanlive.user.domain.dto.FileMergeChunkDTO;
import blog.yuanyuan.yuanlive.user.domain.dto.FileUploadDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MinioService {
    String upload(FileUploadDTO fileUploadDTO);

    Result<String> chunk(FileChunkDTO fileChunkDTO, MultipartFile file);

    List<Integer> check(FileCheckDTO checkDTO);

    Result<String> merge(FileMergeChunkDTO mergeChunkDTO);
}
