package blog.mapper;

import blog.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    /** 物理删除所有用户（绕过逻辑删除，仅同步覆盖时使用）*/
    @Delete("DELETE FROM t_user")
    void deleteAllPhysically();
}
