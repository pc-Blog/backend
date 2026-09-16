package blog.service.impl;

import blog.common.PageDTO;
import blog.common.PageVO;
import blog.entity.MusicCategory;
import blog.exception.BaseException;
import blog.mapper.MusicCategoryMapper;
import blog.service.MusicCategoryService;
import blog.util.PageUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class MusicCategoryServiceImpl extends ServiceImpl<MusicCategoryMapper, MusicCategory>
        implements MusicCategoryService {

    @Override
    public PageVO<MusicCategory> page(PageDTO<MusicCategory> dto) {
        var wrapper = new LambdaQueryWrapper<MusicCategory>().eq(MusicCategory::getDeleted, 0);
        MusicCategory query = dto.getQuery();
        if (query != null && query.getName() != null && !query.getName().isBlank())
            wrapper.like(MusicCategory::getName, query.getName());
        var page = PageUtil.<MusicCategory>toPage(dto);
        page(page, wrapper);
        return new PageVO<>(page.getTotal(), page.getRecords());
    }

    @Override
    public boolean save(MusicCategory category) {
        checkNameUnique(category.getName(), null);
        return super.save(category);
    }

    @Override
    public boolean updateById(MusicCategory category) {
        checkNameUnique(category.getName(), category.getId());
        return super.updateById(category);
    }

    private void checkNameUnique(String name, Long excludeId) {
        if (name == null || name.isBlank()) {
            throw new BaseException("分类名称不能为空");
        }
        var wrapper = new LambdaQueryWrapper<MusicCategory>()
                .eq(MusicCategory::getName, name)
                .eq(MusicCategory::getDeleted, 0);
        if (excludeId != null) {
            wrapper.ne(MusicCategory::getId, excludeId);
        }
        if (count(wrapper) > 0) {
            throw new BaseException("分类名称已存在");
        }
    }
}
