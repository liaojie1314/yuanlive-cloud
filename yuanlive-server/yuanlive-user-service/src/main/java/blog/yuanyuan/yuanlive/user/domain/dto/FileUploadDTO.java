package blog.yuanyuan.yuanlive.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Schema(description = "文件上传DTO")
@AllArgsConstructor
@NoArgsConstructor
public class FileUploadDTO {
    @Pattern(regexp = "^(avatar|file)$", message = "场景错误, 只能为avatar或file")
    private String scene;
    @NotNull(message = "文件不能为空")
    private MultipartFile file;
}
