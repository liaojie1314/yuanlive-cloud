package blog.yuanyuan.yuanlive.ai.repository;

import blog.yuanyuan.yuanlive.ai.domain.doc.UserQueryDOC;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserQueryRepository extends ElasticsearchRepository<UserQueryDOC, String> {
}
