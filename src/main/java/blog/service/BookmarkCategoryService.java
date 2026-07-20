package blog.service;

import blog.entity.BookmarkCategory;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface BookmarkCategoryService extends IService<BookmarkCategory> {
    List<BookmarkCategory> getTree();
}
