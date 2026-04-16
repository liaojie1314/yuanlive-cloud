package blog.yuanyuan.yuanlive.user.domain.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FileMergeChunkDTO {
    @NotEmpty(message = "场景不能为空")
    private String scene;
    @NotEmpty(message = "文件哈希不能为空")
    private String fileHash;
    @NotEmpty(message = "文件名不能为空")
    private String fileName;
    @NotNull(message = "总分片数不能为空")
    private Integer totalChunks;
}
