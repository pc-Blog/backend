package blog.service.impl;

import blog.common.PageDTO;
import blog.common.PageVO;
import blog.entity.Category;
import blog.exception.BaseException;
import blog.mapper.CategoryMapper;
import blog.service.CategoryService;
import blog.util.PageUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Override
    public PageVO<Category> page(PageDTO<Category> dto) {
        var wrapper = new LambdaQueryWrapper<Category>().eq(Category::getDeleted, 0);
        Category query = dto.getQuery();
        if (query != null && query.getName() != null && !query.getName().isBlank())
            wrapper.like(Category::getName, query.getName());
        var page = PageUtil.<Category>toPage(dto);
        page(page, wrapper);
        return new PageVO<>(page.getTotal(), page.getRecords());
    }

    @Override
    public boolean save(Category category) {
        checkNameUniqueInType(category.getName(), category.getType(), null);
        return super.save(category);
    }

    @Override
    public boolean updateById(Category category) {
        checkNameUniqueInType(category.getName(), category.getType(), category.getId());
        return super.updateById(category);
    }

    private void checkNameUniqueInType(String name, String type, Long excludeId) {
        var wrapper = new LambdaQueryWrapper<Category>()
                .eq(Category::getName, name)
                .eq(Category::getType, type)
                .eq(Category::getDeleted, 0);
        if (excludeId != null) {
            wrapper.ne(Category::getId, excludeId);
        }
        if (count(wrapper) > 0) {
            throw new BaseException("该类型下分类名称已存在");
        }
    }
}
