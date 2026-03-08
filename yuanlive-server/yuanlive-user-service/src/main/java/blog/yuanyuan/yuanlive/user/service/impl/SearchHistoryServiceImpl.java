package blog.yuanyuan.yuanlive.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import blog.yuanyuan.yuanlive.entity.user.entity.SearchHistory;
import blog.yuanyuan.yuanlive.user.service.SearchHistoryService;
import blog.yuanyuan.yuanlive.user.mapper.SearchHistoryMapper;
import org.springframework.stereotype.Service;

/**
* @author frodepu
* @description 针对表【search_history(用户搜索记录表)】的数据库操作Service实现
* @createDate 2026-03-05 13:43:44
*/
@Service
public class SearchHistoryServiceImpl extends ServiceImpl<SearchHistoryMapper, SearchHistory>
    implements SearchHistoryService{

}




