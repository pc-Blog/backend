package blog.service;

import blog.dto.DiaryRequestDTO;
import blog.vo.DiaryStatsVO;
import blog.vo.DiaryVO;

import java.util.List;

/**
 * 日记服务
 *
 * <p>仅本地使用：没有 public 语义，也没有发布状态。</p>
 */
public interface DiaryService {

    /** 全量日记（按日期降序），活动条目内嵌 */
    List<DiaryVO> listAll();

    /** 单天详情 */
    DiaryVO get(Long id);

    /** 新建一天，返回新记录的ID */
    Long create(DiaryRequestDTO request);

    /** 更新一天（活动整体替换，日期不可修改） */
    void update(DiaryRequestDTO request);

    /** 删除一天及其全部活动条目 */
    void delete(Long id);

    /** 统计：总数 / 每日条目数 / 分类分布 / 小分类分布 */
    DiaryStatsVO stats();

    /** 指定分类下已使用过的小分类名称（去重、升序） */
    List<String> subcategories(Integer category);
}
