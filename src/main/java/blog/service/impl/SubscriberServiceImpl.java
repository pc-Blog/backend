package blog.service.impl;

import blog.common.PageDTO;
import blog.common.PageVO;
import blog.entity.Subscriber;
import blog.mapper.SubscriberMapper;
import blog.service.SubscriberService;
import blog.util.PageUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class SubscriberServiceImpl extends ServiceImpl<SubscriberMapper, Subscriber> implements SubscriberService {
    @Override
    public PageVO<Subscriber> page(PageDTO<Subscriber> dto) {
        var wrapper = new LambdaQueryWrapper<Subscriber>().orderByDesc(Subscriber::getCreatedAt);
        var page = PageUtil.<Subscriber>toPage(dto);
        page(page, wrapper);
        return new PageVO<>(page.getTotal(), page.getRecords());
    }
}
