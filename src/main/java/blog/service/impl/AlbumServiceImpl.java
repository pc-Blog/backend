package blog.service.impl;

import blog.common.PageDTO;
import blog.common.PageVO;
import blog.entity.Album;
import blog.mapper.AlbumMapper;
import blog.service.AlbumService;
import blog.util.PageUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class AlbumServiceImpl extends ServiceImpl<AlbumMapper, Album> implements AlbumService {

    @Override
    public PageVO<Album> page(PageDTO<Album> dto) {
        var wrapper = new LambdaQueryWrapper<Album>().eq(Album::getDeleted, 0);
        Album query = dto.getQuery();
        if (query != null && query.getTitle() != null && !query.getTitle().isBlank())
            wrapper.like(Album::getTitle, query.getTitle());
        var page = PageUtil.<Album>toPage(dto);
        page(page, wrapper);
        return new PageVO<>(page.getTotal(), page.getRecords());
    }
}
