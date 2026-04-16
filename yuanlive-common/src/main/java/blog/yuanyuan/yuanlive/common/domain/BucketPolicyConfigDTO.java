package blog.yuanyuan.yuanlive.common.domain;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BucketPolicyConfigDTO {
    private String Version;
    private List<Statement> Statement;

    @Data
    @Builder
    public static class Statement {
        private String Effect;
        private String Principal;
        private String Action;
        private String Resource;
    }
}
