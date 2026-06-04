package blog.service.impl;

import blog.common.PageDTO;
import blog.common.PageVO;
import blog.entity.Photo;
import blog.mapper.PhotoMapper;
import blog.service.PhotoService;
import blog.util.PageUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class PhotoServiceImpl extends ServiceImpl<PhotoMapper, Photo> implements PhotoService {

    @Override
    public PageVO<Photo> page(PageDTO<Photo> dto) {
        var wrapper = new LambdaQueryWrapper<Photo>().eq(Photo::getDeleted, 0);
        Photo query = dto.getQuery();
        if (query != null && query.getAlbumId() != null)
            wrapper.eq(Photo::getAlbumId, query.getAlbumId());
        var page = PageUtil.<Photo>toPage(dto);
        page(page, wrapper);
        return new PageVO<>(page.getTotal(), page.getRecords());
    }
}
