package blog.yuanyuan.yuanlive.live.domain.document;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;
import org.springframework.data.elasticsearch.core.suggest.Completion;

import java.time.LocalDateTime;

@Data
@Document(indexName = "yuanlive_search", createIndex = false)
public class SearchDoc {
    @Id
    private Long id;
    @Field(name = "uid", type = FieldType.Long)
    private Long uid;
    @Field(name = "biz_type", type = FieldType.Integer)
    private Integer bizType;

    // 处理 title 及其 keyword 子字段
    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart"),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword, ignoreAbove = 256)
            }
    )
    private String title;

    @MultiField(
            mainField = @Field(name = "anchor_name", type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart"),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword)
            }
    )
    private String anchorName;

    @Field(name = "room_title", type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String roomTitle;

    @Field(name = "category_id", type = FieldType.Integer)
    private Integer categoryId;

    @MultiField(
            mainField = @Field(name = "category_name", type = FieldType.Text, analyzer = "ik_smart"),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword)
            }
    )
    private String categoryName;

    @Field(name = "cover_url", type = FieldType.Keyword, index = false)
    private String coverUrl;

    @Field(name = "video_url", type = FieldType.Keyword, index = false)
    private String videoUrl;

    @Field(name = "hot_score", type = FieldType.Double)
    private Double hotScore;

    // 匹配你定义的 yyyy-MM-dd HH:mm:ss 格式
    @Field(name = "create_time", type = FieldType.Date, pattern = "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern ="yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @Field(type = FieldType.Text, analyzer = "ik_smart")
    private String description;

    // 自动完成字段，需使用 Completion 类型
    @CompletionField(analyzer = "ik_max_word")
    private Object suggestion;
}
