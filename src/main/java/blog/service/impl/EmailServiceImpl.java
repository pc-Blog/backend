package blog.service.impl;

import blog.common.PageDTO;
import blog.common.PageVO;
import blog.entity.Email;
import blog.mapper.EmailMapper;
import blog.service.EmailService;
import blog.util.PageUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl extends ServiceImpl<EmailMapper, Email> implements EmailService {
    @Override
    public PageVO<Email> page(PageDTO<Email> dto) {
        var wrapper = new LambdaQueryWrapper<Email>().orderByDesc(Email::getCreatedAt);
        var page = PageUtil.<Email>toPage(dto);
        page(page, wrapper);
        return new PageVO<>(page.getTotal(), page.getRecords());
    }
}
