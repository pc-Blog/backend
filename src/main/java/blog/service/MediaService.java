package blog.service;

import blog.common.PageDTO;
import blog.common.PageVO;
import blog.entity.Media;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface MediaService extends IService<Media> {
    PageVO<Media> page(PageDTO<Media> dto);

    Media upload(MultipartFile file, String relationType);

    InputStream download(Long id);

    void deleteWithFile(Long id);

    Map<String, Object> batchDelete(List<Long> ids);
}
