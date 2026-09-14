package blog.mapper;

import blog.entity.Diary;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 日记 Mapper
 *
 * <p>全部查询走 MyBatis-Plus 的 LambdaQueryWrapper / BaseMapper 内置方法，
 * 无需自定义 SQL。</p>
 */
@Mapper
public interface DiaryMapper extends BaseMapper<Diary> {
}
