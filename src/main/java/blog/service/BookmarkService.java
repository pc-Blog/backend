package blog.service;

import blog.common.PageDTO;
import blog.common.PageVO;
import blog.entity.Bookmark;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface BookmarkService extends IService<Bookmark> {
    PageVO<Bookmark> page(PageDTO<Bookmark> dto);
    List<Bookmark> getPinnedList();
    List<Bookmark> getFullList();
}
