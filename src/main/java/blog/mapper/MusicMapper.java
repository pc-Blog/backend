package blog.mapper;

import blog.entity.Music;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 音乐 Mapper
 *
 * <p>全部查询走 MyBatis-Plus 的 LambdaQueryWrapper / BaseMapper 内置方法，
 * 无需自定义 SQL。</p>
 */
@Mapper
public interface MusicMapper extends BaseMapper<Music> {
}
