package blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import blog.entity.Chatter;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatterMapper extends BaseMapper<Chatter> {
}
