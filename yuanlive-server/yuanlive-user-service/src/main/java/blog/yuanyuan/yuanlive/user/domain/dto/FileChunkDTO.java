package blog.yuanyuan.yuanlive.user.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class FileChunkDTO {
    @NotBlank(message = "文件哈希值不能为空")
    private String fileHash;
    @NotNull(message = "场景不能为空")
    @Pattern(regexp = "^(avatar|file|cover)$", message = "场景错误, 只能为avatar或file")
    private String scene;
    @NotBlank(message = "文件名不能为空")
    private String fileName;
    @NotNull(message = "文件索引不能为空")
    private Integer chunkIndex;
    @NotNull(message = "文件总片数不能为空")
    private Integer totalChunks;
}
