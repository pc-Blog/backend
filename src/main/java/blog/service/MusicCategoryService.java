package blog.service;

import blog.common.PageDTO;
import blog.common.PageVO;
import blog.entity.MusicCategory;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 音乐分类服务
 *
 * <p>新增/更新走覆写的 {@link IService#save} 与 {@link IService#updateById}，
 * 在其中做名称唯一校验；删除为逻辑删除，不检查引用、不级联。</p>
 */
public interface MusicCategoryService extends IService<MusicCategory> {
    PageVO<MusicCategory> page(PageDTO<MusicCategory> dto);
}
