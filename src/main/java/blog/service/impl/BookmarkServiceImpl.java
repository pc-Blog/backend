package blog.service.impl;

import blog.common.PageDTO;
import blog.common.PageVO;
import blog.entity.Bookmark;
import blog.mapper.BookmarkMapper;
import blog.service.BookmarkService;
import blog.util.PageUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookmarkServiceImpl extends ServiceImpl<BookmarkMapper, Bookmark> implements BookmarkService {

    @Override
    public PageVO<Bookmark> page(PageDTO<Bookmark> dto) {
        var wrapper = new LambdaQueryWrapper<Bookmark>().eq(Bookmark::getDeleted, 0);
        Bookmark query = dto.getQuery();
        if (query != null && query.getName() != null && !query.getName().isBlank())
            wrapper.like(Bookmark::getName, query.getName());
        var page = PageUtil.<Bookmark>toPage(dto);
        page(page, wrapper);
        return new PageVO<>(page.getTotal(), page.getRecords());
    }

    @Override
    public List<Bookmark> getPinnedList() {
        return lambdaQuery()
                .eq(Bookmark::getDeleted, 0)
                .eq(Bookmark::getIsPin, 1)
                .orderByAsc(Bookmark::getSortOrder)
                .orderByDesc(Bookmark::getId)
                .last("LIMIT 11")
                .list();
    }

    @Override
    public List<Bookmark> getFullList() {
        return lambdaQuery()
                .eq(Bookmark::getDeleted, 0)
                .orderByAsc(Bookmark::getSortOrder)
                .orderByDesc(Bookmark::getId)
                .list();
    }
}
