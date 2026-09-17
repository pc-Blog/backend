package blog.service;

import blog.common.PageDTO;
import blog.common.PageVO;
import blog.entity.Media;
import blog.vo.MediaScanResultVO;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface MediaService extends IService<Media> {
    PageVO<Media> page(PageDTO<Media> dto);

    Media upload(MultipartFile file, String relationType);

    InputStream download(Long id);

    /**
     * 孤儿扫描：返回全部媒体文件及其引用来源。
     *
     * <p>判定依据为 {@link MediaRefResolver} 中统一定义的引用来源清单，
     * 与删除前的 {@code checkReferences} 共用同一套规则。</p>
     */
    MediaScanResultVO scanOrphans();

    void deleteWithFile(Long id);

    Map<String, Object> batchDelete(List<Long> ids);
}
