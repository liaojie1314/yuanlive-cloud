package blog.yuanyuan.yuanlive.ai.domain.doc;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.util.Date;

@Data
@Document(indexName = "user_queries")
@Setting(shards = 1, replicas = 0)
public class UserQueryDOC {
    @Id
    private String id;
    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "ik_smart", searchAnalyzer = "ik_smart"),
            otherFields = {
                    @InnerField(type = FieldType.Keyword, suffix = "keyword", ignoreAbove = 256)
            }
    )
    private String content;
    @Field(type = FieldType.Dense_Vector, dims = 1024)
    private float[] contentVector;
    @Field(type = FieldType.Date, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
