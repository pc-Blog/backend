package blog.service.impl;

import blog.common.PageDTO;
import blog.common.PageVO;
import blog.entity.Timeline;
import blog.mapper.TimelineMapper;
import blog.service.TimelineService;
import blog.util.PageUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class TimelineServiceImpl extends ServiceImpl<TimelineMapper, Timeline> implements TimelineService {

    @Override
    public PageVO<Timeline> page(PageDTO<Timeline> dto) {
        var wrapper = new LambdaQueryWrapper<Timeline>().eq(Timeline::getDeleted, 0);
        Timeline query = dto.getQuery();
        if (query != null && query.getTitle() != null && !query.getTitle().isBlank())
            wrapper.like(Timeline::getTitle, query.getTitle());
        var page = PageUtil.<Timeline>toPage(dto);
        page(page, wrapper);
        return new PageVO<>(page.getTotal(), page.getRecords());
    }
}
