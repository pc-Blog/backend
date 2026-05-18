package blog.mapper;

import blog.entity.ArticleTag;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ArticleTagMapper extends BaseMapper<ArticleTag> {

    @Select("SELECT tag_id FROM t_article_tag WHERE article_id = #{articleId}")
    List<Long> selectTagIdsByArticleId(Long articleId);
}
