package blog.service;

import blog.common.PageDTO;
import blog.common.PageVO;
import blog.entity.Album;
import com.baomidou.mybatisplus.extension.service.IService;

public interface AlbumService extends IService<Album> {
    PageVO<Album> page(PageDTO<Album> dto);
}
