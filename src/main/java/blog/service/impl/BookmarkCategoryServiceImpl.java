package blog.service.impl;

import blog.entity.BookmarkCategory;
import blog.mapper.BookmarkCategoryMapper;
import blog.service.BookmarkCategoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookmarkCategoryServiceImpl extends ServiceImpl<BookmarkCategoryMapper, BookmarkCategory> implements BookmarkCategoryService {

    @Override
    public List<BookmarkCategory> getTree() {
        return lambdaQuery()
                .eq(BookmarkCategory::getDeleted, 0)
                .orderByAsc(BookmarkCategory::getSortOrder)
                .orderByAsc(BookmarkCategory::getId)
                .list();
    }
}
