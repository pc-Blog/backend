package blog.mapper;

import blog.entity.DiaryActivity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 日记活动条目 Mapper
 *
 * <p>全部查询走 MyBatis-Plus 的 LambdaQueryWrapper / BaseMapper 内置方法，
 * 无需自定义 SQL。统计所需的聚合在内存中完成（数据量极小）。</p>
 */
@Mapper
public interface DiaryActivityMapper extends BaseMapper<DiaryActivity> {
}
