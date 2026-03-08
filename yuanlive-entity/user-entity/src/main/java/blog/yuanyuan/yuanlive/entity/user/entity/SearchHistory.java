package blog.yuanyuan.yuanlive.entity.user.entity;


import java.time.LocalDateTime;
import java.util.Date;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class SearchHistory {
    @Schema(description="")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @Schema(description="")
    private Long userId;
    @Schema(description="")
    private String keyword;
    @Schema(description="记录搜索时的关联分类，方便AI分析")
    private Integer categoryId;
    @Schema(description="")
    private LocalDateTime createTime;
}
