package blog.yuanyuan.yuanlive.user.domain.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class FileCheckDTO {
    private String fileName;
    @NotEmpty(message = "文件哈希值不能为空")
    private String fileHash;
    private String scene;
}
