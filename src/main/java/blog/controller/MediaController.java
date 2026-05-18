package blog.controller;

import blog.common.PageDTO;
import blog.common.PageVO;
import blog.common.Result;
import blog.entity.Media;
import blog.service.MediaService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/media")
public class MediaController {

    private final MediaService mediaService;

    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @GetMapping("/{id}")
    public Result<Media> getById(@PathVariable Long id) {
        log.info("根据ID查询媒体文件, id:{}", id);
        return Result.success(mediaService.getById(id));
    }

    @PostMapping("/upload")
    public Result<Media> upload(@RequestParam("file") MultipartFile file,
                                @RequestParam(value = "relationType", required = false) String relationType) {
        log.info("上传文件, 原始文件名:{}, 大小:{}, 类型:{}",
                file.getOriginalFilename(), file.getSize(), file.getContentType());
        Media media = mediaService.upload(file, relationType);
        log.info("上传成功:{}", JSON.toJSONString(media, SerializerFeature.PrettyFormat));
        return Result.success(media);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<InputStreamResource> download(@PathVariable Long id) {
        log.info("下载文件, id:{}", id);
        Media media = mediaService.getById(id);
        InputStream stream = mediaService.download(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(media.getMimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + media.getOriginalFilename() + "\"")
                .body(new InputStreamResource(stream));
    }

    @PutMapping
    public Result<Void> update(@Valid @RequestBody Media media) {
        log.info("更新媒体文件元数据:{}", JSON.toJSONString(media, SerializerFeature.PrettyFormat));
        mediaService.updateById(media);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除媒体文件(含MinIO文件), id:{}", id);
        mediaService.deleteWithFile(id);
        return Result.success();
    }

    @DeleteMapping("/batch")
    public Result<Map<String, Object>> batchDelete(@RequestBody List<Long> ids) {
        log.info("批量删除媒体文件, ids:{}", ids);
        Map<String, Object> result = mediaService.batchDelete(ids);
        return Result.success(result);
    }

    @PostMapping("/page")
    public Result<PageVO<Media>> page(@RequestBody PageDTO<Media> dto) {
        log.info("分页查询媒体文件:{}", JSON.toJSONString(dto, SerializerFeature.PrettyFormat));
        return Result.success(mediaService.page(dto));
    }
}
