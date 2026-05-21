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
        List<About> items = list(new LambdaQueryWrapper<About>().orderByAsc(About::getSortOrder));
        return items.stream()
                .collect(Collectors.toMap(About::getItemKey, About::getItemValue,
                        (a, b) -> b, LinkedHashMap::new));
    }

    @Override
    @Transactional
    public void updateAboutMap(Map<String, String> map) {
        remove(new LambdaQueryWrapper<>());
        if (map == null || map.isEmpty()) {
            return;
        }
        List<About> items = map.entrySet().stream().map(entry -> {
            About a = new About();
            a.setItemKey(entry.getKey());
            a.setItemValue(entry.getValue());
            return a;
        }).toList();
        saveBatch(items);
    }
}
