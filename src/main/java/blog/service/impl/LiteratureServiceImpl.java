package blog.service.impl;

import blog.common.PageDTO;
import blog.common.PageVO;
import blog.dto.LiteratureQueryDTO;
import blog.entity.Category;
import blog.entity.Literature;
import blog.exception.BaseException;
import blog.mapper.CategoryMapper;
import blog.mapper.LiteratureMapper;
import blog.service.LiteratureService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class LiteratureServiceImpl implements LiteratureService {

    /** 文学分类的类型标识 */
    private static final String TYPE_LITERATURE = "LITERATURE";

    private final LiteratureMapper literatureMapper;
    private final CategoryMapper categoryMapper;

    public LiteratureServiceImpl(LiteratureMapper literatureMapper, CategoryMapper categoryMapper) {
        this.literatureMapper = literatureMapper;
        this.categoryMapper = categoryMapper;
    }

    // ==================== 访客端 ====================

    @Override
    public List<Literature> publicList() {
        List<Literature> list = literatureMapper.selectList(new LambdaQueryWrapper<Literature>()
                .eq(Literature::getIsPublished, 1));

        // 按分类 sort_order 排序：分类名由前端用 categoryOptions() 的映射解析，
        // 这里只保证返回顺序即为分组顺序。
        Map<Long, Category> categoryMap = categoryOptions().stream()
                .collect(Collectors.toMap(Category::getId, c -> c));

        return list.stream()
                .sorted(Comparator
                        .comparingInt((Literature l) -> sortOrderOf(l.getCategoryId(), categoryMap))
                        .thenComparing(Literature::getWrittenAt,
                                Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Literature::getId, Comparator.reverseOrder()))
                .collect(Collectors.toList());
    }

    private static int sortOrderOf(Long categoryId, Map<Long, Category> categoryMap) {
        Category category = categoryId != null ? categoryMap.get(categoryId) : null;
        return category != null && category.getSortOrder() != null
                ? category.getSortOrder()
                : Integer.MAX_VALUE;
    }

    @Override
    public long publicCount() {
        return literatureMapper.selectCount(new LambdaQueryWrapper<Literature>()
                .eq(Literature::getIsPublished, 1));
    }

    @Override
    public Literature publicDetail(Long id) {
        Literature literature = getExisting(id);
        if (!Objects.equals(literature.getIsPublished(), 1)) {
            throw new BaseException("作品未发布");
        }
        return literature;
    }

    @Override
    public List<Category> categoryOptions() {
        return categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .eq(Category::getType, TYPE_LITERATURE)
                .orderByAsc(Category::getSortOrder));
    }

    // ==================== 管理端 ====================

    @Override
    public PageVO<Literature> adminPage(PageDTO<LiteratureQueryDTO> dto) {
        LambdaQueryWrapper<Literature> wrapper = new LambdaQueryWrapper<>();
        LiteratureQueryDTO query = dto.getQuery();

        if (query != null) {
            wrapper.like(query.getTitle() != null && !query.getTitle().isBlank(),
                            Literature::getTitle, query.getTitle())
                    .eq(query.getCategoryId() != null, Literature::getCategoryId, query.getCategoryId())
                    .eq(query.getIsPublished() != null, Literature::getIsPublished, query.getIsPublished())
                    .ge(query.getStartDate() != null, Literature::getWrittenAt, query.getStartDate())
                    .le(query.getEndDate() != null, Literature::getWrittenAt, query.getEndDate());
        }

        wrapper.orderByDesc(Literature::getWrittenAt).orderByDesc(Literature::getId);

        IPage<Literature> page = literatureMapper.selectPage(
                Page.of(dto.getPageNum(), dto.getPageSize()), wrapper);

        return new PageVO<>(page.getTotal(), page.getRecords());
    }

    @Override
    public Literature adminDetail(Long id) {
        return getExisting(id);
    }

    @Override
    public void save(Literature literature) {
        literatureMapper.insert(literature);
    }

    @Override
    public void update(Literature literature) {
        getExisting(literature.getId());
        literatureMapper.updateById(literature);
    }

    @Override
    public void delete(Long id) {
        getExisting(id);
        literatureMapper.deleteById(id);
    }

    @Override
    public void publish(Long id) {
        getExisting(id);
        setPublished(id, 1);
    }

    @Override
    public void unpublish(Long id) {
        getExisting(id);
        setPublished(id, 0);
    }

    private void setPublished(Long id, int value) {
        Literature update = new Literature();
        update.setId(id);
        update.setIsPublished(value);
        literatureMapper.updateById(update);
    }

    // ==================== 校验 ====================

    @Override
    public void checkCategoryValid(Long categoryId) {
        if (categoryId == null) {
            return;
        }
        Category category = categoryMapper.selectById(categoryId);
        if (category == null || Objects.equals(category.getDeleted(), 1)) {
            throw new BaseException("文学分类不存在");
        }
        if (!TYPE_LITERATURE.equals(category.getType())) {
            throw new BaseException("所选分类不是文学类型");
        }
    }

    private Literature getExisting(Long id) {
        Literature literature = literatureMapper.selectById(id);
        if (literature == null) {
            throw new BaseException("作品不存在");
        }
        return literature;
    }
}
