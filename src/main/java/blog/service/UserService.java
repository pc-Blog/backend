package blog.service;

import blog.common.PageDTO;
import blog.common.PageVO;
import blog.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

public interface UserService extends IService<User> {
    PageVO<User> page(PageDTO<User> dto);

    Map<String, Object> login(String username, String password);

    Map<String, Object> loginByGithub(String code);
}
