package blog.service;

import blog.common.PageDTO;
import blog.common.PageVO;
import blog.entity.Subscriber;
import com.baomidou.mybatisplus.extension.service.IService;

public interface SubscriberService extends IService<Subscriber> {
    PageVO<Subscriber> page(PageDTO<Subscriber> dto);
}
