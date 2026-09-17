package blog.service;

import blog.vo.MediaRefVO;

import java.util.List;
import java.util.Map;

/**
 * 媒体引用解析
 *
 * <p>集中定义「哪些业务数据的哪些字段会引用 {@code t_media} 的文件 URL」。
 * 孤儿扫描是唯一的判定入口，删除操作直接依据扫描结果、不做二次校验，
 * 因此引用规则只有这一处实现，不存在两套规则漂移的可能。</p>
 *
 * <p>新增持有文件 URL 的业务表时，只需在实现中补充来源。</p>
 */
public interface MediaRefResolver {

    /**
     * 建立「文件URL -> 引用列表」的索引。
     *
     * <p>一次性扫描全部引用来源并归约为 URL 键，之后按 URL 等值查找。
     * 返回的 map 仅包含被引用过的 URL；查询时用
     * {@code getOrDefault(url, List.of())}，取到空列表即孤儿。</p>
     */
    Map<String, List<MediaRefVO>> buildRefIndex();
}
