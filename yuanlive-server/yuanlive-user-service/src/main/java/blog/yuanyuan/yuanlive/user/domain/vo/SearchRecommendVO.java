package blog.yuanyuan.yuanlive.user.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchRecommendVO {
    private Integer id;
    private String content;
    private Boolean isMostRecommended;
}
