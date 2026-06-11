package blog.service;

import blog.common.PageDTO;
import blog.common.PageVO;
import blog.entity.Chatter;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface ChatterService extends IService<Chatter> {
    PageVO<Chatter> page(PageDTO<Chatter> dto);
    List<Chatter> getPublishedListWithImages();
}
