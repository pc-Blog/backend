package blog.controller;

import blog.common.PageDTO;
import blog.common.PageVO;
import blog.common.Result;
import blog.entity.Skill;
import blog.service.SkillService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/skill")
public class SkillController {

    private final SkillService skillService;

    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    @GetMapping("/list")
    public Result<List<Skill>> list() {
        log.info("访客端查询技能列表");
        List<Skill> list = skillService.list(
                new LambdaQueryWrapper<Skill>()
                        .eq(Skill::getDeleted, 0)
                        .orderByDesc(Skill::getProficiency));
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result<Skill> getById(@PathVariable Long id) {
        log.info("根据ID查询技能, id:{}", id);
        return Result.success(skillService.getById(id));
    }

    @PostMapping
    public Result<Void> save(@Valid @RequestBody Skill skill) {
        log.info("新增技能:{}", JSON.toJSONString(skill, SerializerFeature.PrettyFormat));
        skillService.save(skill);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@Valid @RequestBody Skill skill) {
        log.info("更新技能:{}", JSON.toJSONString(skill, SerializerFeature.PrettyFormat));
        skillService.updateById(skill);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除技能, id:{}", id);
        skillService.removeById(id);
        return Result.success();
    }

    @PostMapping("/page")
    public Result<PageVO<Skill>> page(@RequestBody PageDTO<Skill> dto) {
        log.info("分页查询技能:{}", JSON.toJSONString(dto, SerializerFeature.PrettyFormat));
        return Result.success(skillService.page(dto));
    }
}
