package blog.yuanyuan.yuanlive.user.controller;

import blog.yuanyuan.yuanlive.common.result.Result;
import blog.yuanyuan.yuanlive.user.domain.dto.FileUploadDTO;
import blog.yuanyuan.yuanlive.user.service.MinioService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/minio")
@Slf4j
@Tag(name = "文件上传接口")
public class MinioController {
    @Resource
    private MinioService minioService;

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("scene")
                                 @Pattern(regexp = "^(avatar|file)$", message = "场景错误, 只能为avatar或file")
                                     String scene,
                                 @RequestParam("file")
                                 @NotNull(message = "文件不能为空")
                                 MultipartFile file){
        FileUploadDTO fileUploadDTO = new FileUploadDTO(scene, file);
        String url = minioService.upload(fileUploadDTO);
        return Result.success(url);
    }
}
