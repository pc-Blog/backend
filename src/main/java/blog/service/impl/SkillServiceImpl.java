package blog.service.impl;

import blog.common.PageDTO;
import blog.common.PageVO;
import blog.entity.Skill;
import blog.exception.BaseException;
import blog.mapper.SkillMapper;
import blog.service.SkillService;
import blog.util.PageUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class SkillServiceImpl extends ServiceImpl<SkillMapper, Skill> implements SkillService {

    @Override
    public PageVO<Skill> page(PageDTO<Skill> dto) {
        var wrapper = new LambdaQueryWrapper<Skill>().eq(Skill::getDeleted, 0);
        Skill query = dto.getQuery();
        if (query != null && query.getName() != null && !query.getName().isBlank())
            wrapper.like(Skill::getName, query.getName());
        var page = PageUtil.<Skill>toPage(dto);
        page(page, wrapper);
        return new PageVO<>(page.getTotal(), page.getRecords());
    }

    @Override
    public boolean save(Skill skill) {
        checkNameUnique(skill.getName(), null);
        return super.save(skill);
    }

    @Override
    public boolean updateById(Skill skill) {
        checkNameUnique(skill.getName(), skill.getId());
        return super.updateById(skill);
    }

    private void checkNameUnique(String name, Long excludeId) {
        var wrapper = new LambdaQueryWrapper<Skill>()
                .eq(Skill::getName, name)
                .eq(Skill::getDeleted, 0);
        if (excludeId != null) {
            wrapper.ne(Skill::getId, excludeId);
        }
        if (count(wrapper) > 0) {
            throw new BaseException("技能名称已存在");
        }
    }
}
