package blog.yuanyuan.yuanlive.entity.live.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SearchQueryDTO {
    @NotBlank(message = "搜索不能为空")
    private String query;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
