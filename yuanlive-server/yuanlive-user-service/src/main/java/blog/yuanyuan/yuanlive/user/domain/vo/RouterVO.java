package blog.yuanyuan.yuanlive.user.domain.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Schema(description = "动态路由 VO")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RouterVO {
    @Schema(description = "路由地址")
    private String path;

    @Schema(description = "路由名称")
    private String name;

    @Schema(description = "组件路径")
    private String component;

    @Schema(description = "其他元素")
    private MetaVO meta;

    @Schema(description = "子路由列表")
    private List<RouterVO> children;

    /**
     * MetaVO 内部类
     */
    @Data
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MetaVO {

        @Schema(description = "菜单名称")
        private String title;

        @Schema(description = "菜单图标")
        private String icon;

        @Schema(description = "菜单排序")
        private Integer sort;

        @Schema(description = "当前页面的按钮权限集合", example = "['user:add', 'user:edit']")
        private List<String> auths;

        @Schema(description = "是否在菜单中显示")
        private Boolean showLink;

        @Schema(description = "是否缓存页面")
        private Boolean keepAlive;
    }
}
