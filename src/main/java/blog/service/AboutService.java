package blog.service;

import blog.entity.About;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

public interface AboutService extends IService<About> {
    Map<String, String> getAboutMap();

    void updateAboutMap(Map<String, String> map);
}
