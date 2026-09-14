package blog.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 日记统计（响应）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiaryStatsVO {

    /** 活动条目总数 */
    private long total;

    /** 每天的条目数，形如 [["2025-06-20", 3], ...]，按日期升序，供日历热力图使用 */
    private List<List<Object>> dailyCount;

    /** 分类ID -> 条目数 */
    private Map<Integer, Long> categoryCount;

    /** 分类ID -> (小分类名称 -> 条目数) */
    private Map<Integer, Map<String, Long>> subcategoryCount;
}
