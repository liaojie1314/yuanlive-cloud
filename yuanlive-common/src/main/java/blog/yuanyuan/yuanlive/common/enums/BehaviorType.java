package blog.yuanyuan.yuanlive.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BehaviorType {
    RECOMMEND(8.0, true),
    SEARCH(5.0, false),
    COLLECT(4.0, false),
    SHARE(3.0, true),
    LIKE(1.0, true),
    UNLIKE(-5.0, true);

    private final double weight;
    private final boolean isStateful; // 是否需要存储在数据库中
    public String getSqlColumn() {
        return switch (this) {
            case LIKE -> "has_liked";
            case SHARE -> "has_shared";
            case RECOMMEND -> "has_recommended";
            case UNLIKE -> "has_unliked";
            default -> null;
        };
    }
    public Integer getBitOffset() {
        return switch (this) {
            case LIKE -> 0;
            case SHARE -> 1;
            case RECOMMEND -> 2;
            case UNLIKE -> 3;
            default -> null;
        };
    }
}
