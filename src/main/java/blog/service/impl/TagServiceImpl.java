package blog.service.impl;

import blog.common.PageDTO;
import blog.common.PageVO;
import blog.entity.Tag;
import blog.exception.BaseException;
import blog.mapper.TagMapper;
import blog.service.TagService;
import blog.util.PageUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

    @Override
    public PageVO<Tag> page(PageDTO<Tag> dto) {
        var wrapper = new LambdaQueryWrapper<Tag>().eq(Tag::getDeleted, 0);
        Tag query = dto.getQuery();
        if (query != null && query.getName() != null && !query.getName().isBlank())
            wrapper.like(Tag::getName, query.getName());
        var page = PageUtil.<Tag>toPage(dto);
        page(page, wrapper);
        return new PageVO<>(page.getTotal(), page.getRecords());
    }

    @Override
    public boolean save(Tag tag) {
        checkNameUnique(tag.getName(), null);
        return super.save(tag);
    }

    @Override
    public boolean updateById(Tag tag) {
        checkNameUnique(tag.getName(), tag.getId());
        return super.updateById(tag);
    }

    private void checkNameUnique(String name, Long excludeId) {
        var wrapper = new LambdaQueryWrapper<Tag>()
                .eq(Tag::getName, name)
                .eq(Tag::getDeleted, 0);
        if (excludeId != null) {
            wrapper.ne(Tag::getId, excludeId);
        }
        if (count(wrapper) > 0) {
            throw new BaseException("标签名称已存在");
        }
    }
}
