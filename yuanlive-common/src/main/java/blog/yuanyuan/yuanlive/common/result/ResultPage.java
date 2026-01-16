package blog.yuanyuan.yuanlive.common.result;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "分页结果")
public class ResultPage <T>{
    @Schema(description = "列表")
    private List<T> list;
    @Schema(description = "总条数")
    private long total;
    @Schema(description = "每页条数")
    private long pageSize;
    @Schema(description = "当前页")
    private long currentPage;

    public static <T> ResultPage<T> of(Page<T> page) {
        ResultPage<T> resultPage = new ResultPage<>();
        resultPage.setList(page.getRecords());
        resultPage.setTotal(page.getTotal());
        resultPage.setPageSize(page.getSize());
        resultPage.setCurrentPage(page.getCurrent());
        return resultPage;
    }

}
