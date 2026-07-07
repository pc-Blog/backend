package blog.service.impl;

import blog.common.PageDTO;
import blog.common.PageVO;
import blog.entity.PushLog;
import blog.mapper.PushLogMapper;
import blog.service.PushLogService;
import blog.util.PageUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class PushLogServiceImpl extends ServiceImpl<PushLogMapper, PushLog> implements PushLogService {
    @Override
    public PageVO<PushLog> page(PageDTO<PushLog> dto) {
        var wrapper = new LambdaQueryWrapper<PushLog>().orderByDesc(PushLog::getPushedAt);
        var page = PageUtil.<PushLog>toPage(dto);
        page(page, wrapper);
        return new PageVO<>(page.getTotal(), page.getRecords());
    }
}
