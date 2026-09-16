package blog.service.impl;

import blog.entity.About;
import blog.mapper.AboutMapper;
import blog.service.AboutService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AboutServiceImpl extends ServiceImpl<AboutMapper, About> implements AboutService {

    @Override
    public Map<String, String> getAboutMap() {
        // 只返回前端可编辑的项；is_system=1 的后端专用值（如音乐累计时长）对外不可见
        List<About> items = list(new LambdaQueryWrapper<About>()
                .eq(About::getIsSystem, 0)
                .orderByAsc(About::getSortOrder));
        return items.stream()
                .collect(Collectors.toMap(About::getItemKey, About::getItemValue,
                        (a, b) -> b, LinkedHashMap::new));
    }

    @Override
    @Transactional
    public void updateAboutMap(Map<String, String> map) {
        // 只清空前端可编辑的项，保留 is_system=1 的后端专用值
        remove(new LambdaQueryWrapper<About>().eq(About::getIsSystem, 0));
        if (map == null || map.isEmpty()) {
            return;
        }
        List<About> items = map.entrySet().stream().map(entry -> {
            About a = new About();
            a.setItemKey(entry.getKey());
            a.setItemValue(entry.getValue());
            a.setIsSystem(0);
            return a;
        }).toList();
        saveBatch(items);
    }

    // ==================== 后端模块内部使用：只读写 is_system=1 ====================

    @Override
    public String getSystemValue(String key) {
        About item = getOne(new LambdaQueryWrapper<About>()
                .eq(About::getItemKey, key)
                .eq(About::getIsSystem, 1));
        return item == null ? null : item.getItemValue();
    }

    @Override
    @Transactional
    public void setSystemValue(String key, String value) {
        About existing = getOne(new LambdaQueryWrapper<About>()
                .eq(About::getItemKey, key)
                .eq(About::getIsSystem, 1));
        if (existing == null) {
            About item = new About();
            item.setItemKey(key);
            item.setItemValue(value);
            // is_system 固定为 1，调用方无法指定，避免误改关于页配置
            item.setIsSystem(1);
            save(item);
        } else {
            // 仅更新值，不触碰 is_system 与 sort_order
            existing.setItemValue(value);
            updateById(existing);
        }
    }
}
