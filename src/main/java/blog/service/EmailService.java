package blog.service;

import blog.common.PageDTO;
import blog.common.PageVO;
import blog.entity.Email;
import com.baomidou.mybatisplus.extension.service.IService;

public interface EmailService extends IService<Email> {
    PageVO<Email> page(PageDTO<Email> dto);
}
