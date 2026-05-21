package blog.controller;

import blog.common.Result;
import blog.service.AboutService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/about")
public class AboutController {

    private final AboutService aboutService;

    public AboutController(AboutService aboutService) {
        this.aboutService = aboutService;
    }

    @GetMapping
    public Result<Map<String, String>> get() {
        log.info("查询关于信息");
        return Result.success(aboutService.getAboutMap());
    }

    @PutMapping
    public Result<Void> update(@RequestBody Map<String, String> map) {
        log.info("更新关于信息:{}", JSON.toJSONString(map, SerializerFeature.PrettyFormat));
        aboutService.updateAboutMap(map);
        return Result.success();
    }
}
