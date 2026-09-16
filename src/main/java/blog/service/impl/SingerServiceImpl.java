package blog.service.impl;

import blog.common.PageDTO;
import blog.common.PageVO;
import blog.entity.Singer;
import blog.exception.BaseException;
import blog.mapper.SingerMapper;
import blog.service.SingerService;
import blog.util.PageUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class SingerServiceImpl extends ServiceImpl<SingerMapper, Singer> implements SingerService {

    @Override
    public PageVO<Singer> page(PageDTO<Singer> dto) {
        var wrapper = new LambdaQueryWrapper<Singer>().eq(Singer::getDeleted, 0);
        Singer query = dto.getQuery();
        if (query != null && query.getName() != null && !query.getName().isBlank())
            wrapper.like(Singer::getName, query.getName());
        var page = PageUtil.<Singer>toPage(dto);
        page(page, wrapper);
        return new PageVO<>(page.getTotal(), page.getRecords());
    }

    @Override
    public boolean save(Singer singer) {
        checkNameUnique(singer.getName(), null);
        return super.save(singer);
    }

    @Override
    public boolean updateById(Singer singer) {
        checkNameUnique(singer.getName(), singer.getId());
        return super.updateById(singer);
    }

    private void checkNameUnique(String name, Long excludeId) {
        if (name == null || name.isBlank()) {
            throw new BaseException("歌手名称不能为空");
        }
        var wrapper = new LambdaQueryWrapper<Singer>()
                .eq(Singer::getName, name)
                .eq(Singer::getDeleted, 0);
        if (excludeId != null) {
            wrapper.ne(Singer::getId, excludeId);
        }
        if (count(wrapper) > 0) {
            throw new BaseException("歌手名称已存在");
        }
    }
}
