package blog.mapper;

import blog.entity.Article;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ArticleMapper extends BaseMapper<Article> {

    @Update("UPDATE t_article SET view_count = view_count + 1 WHERE id = #{id}")
    void incrementViewCount(Long id);
}
