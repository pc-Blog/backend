package blog.service;

import blog.common.PageDTO;
import blog.common.PageVO;
import blog.entity.Photo;
import com.baomidou.mybatisplus.extension.service.IService;

public interface PhotoService extends IService<Photo> {
    PageVO<Photo> page(PageDTO<Photo> dto);
}
