package blog.service;

import blog.common.PageDTO;
import blog.common.PageVO;
import blog.entity.PushLog;
import com.baomidou.mybatisplus.extension.service.IService;

public interface PushLogService extends IService<PushLog> {
    PageVO<PushLog> page(PageDTO<PushLog> dto);
}
