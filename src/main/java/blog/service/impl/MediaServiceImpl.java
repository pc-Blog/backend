package blog.service.impl;

import blog.common.PageDTO;
import blog.common.PageVO;
import blog.entity.Media;
import blog.exception.BaseException;
import blog.mapper.MediaMapper;
import blog.service.MediaRefResolver;
import blog.service.MediaService;
import blog.util.MinioUtil;
import blog.util.PageUtil;
import blog.vo.MediaRefVO;
import blog.vo.MediaScanResultVO;
import blog.vo.MediaScanVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class MediaServiceImpl extends ServiceImpl<MediaMapper, Media> implements MediaService {

    private static final long MAX_SIZE = 100 * 1024 * 1024;

    /**
     * 允许上传的 MIME 白名单。
     * 音频部分用于音乐模块：迁移既有曲库时经由本接口入库，
     * 以便统一登记到 t_media（relation_type='music'）并由孤儿扫描覆盖。
     * FLAC 在不同浏览器/系统上报的 MIME 不一致，故同时收录两种写法。
     * Opus 的规范 MIME 是 audio/opus（RFC 7845），但 ogg 封装时浏览器多报
     * audio/ogg，故两种写法都收，避免管理页手动上传 .opus 被误拒。
     */
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/svg+xml",
            "application/pdf",
            "application/zip", "application/x-zip-compressed",
            "audio/mpeg", "audio/mp4", "audio/aac", "audio/wav", "audio/x-wav",
            "audio/ogg", "audio/opus", "audio/flac", "audio/x-flac"
    );

    private final MinioUtil minioUtil;
    private final MediaRefResolver mediaRefResolver;

    public MediaServiceImpl(MinioUtil minioUtil, MediaRefResolver mediaRefResolver) {
        this.minioUtil = minioUtil;
        this.mediaRefResolver = mediaRefResolver;
    }

    @Override
    public PageVO<Media> page(PageDTO<Media> dto) {
        var wrapper = new LambdaQueryWrapper<Media>().eq(Media::getDeleted, 0);
        Media query = dto.getQuery();
        if (query != null && query.getFilename() != null && !query.getFilename().isBlank())
            wrapper.like(Media::getFilename, query.getFilename());
        if (query != null && query.getOriginalFilename() != null && !query.getOriginalFilename().isBlank())
            wrapper.like(Media::getOriginalFilename, query.getOriginalFilename());
        var page = PageUtil.<Media>toPage(dto);
        page(page, wrapper);
        return new PageVO<>(page.getTotal(), page.getRecords());
    }

    @Override
    @Transactional
    public Media upload(MultipartFile file, String relationType) {
        if (file.isEmpty()) {
            throw new BaseException("请选择文件");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BaseException("文件大小不能超过100MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new BaseException("不支持的文件类型: " + contentType);
        }

        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String objectName = datePath + "/" + uuid + ext;

        minioUtil.loadFile(file, objectName);

        Media media = new Media();
        media.setFilename(uuid + ext);
        media.setOriginalFilename(originalName);
        media.setFilePath(objectName);
        media.setFileUrl(minioUtil.getFileUrl(objectName));
        media.setFileSize(file.getSize());
        media.setMimeType(contentType);
        media.setRelationType(relationType);
        save(media);

        return media;
    }

    @Override
    public InputStream download(Long id) {
        Media media = getById(id);
        if (media == null || media.getDeleted() == 1) {
            throw new BaseException("文件不存在或已删除");
        }
        return minioUtil.downLoadFile(media.getFilePath());
    }

    @Override
    public MediaScanResultVO scanOrphans() {
        // 一次扫描全部媒体
        List<Media> allMedia = list(new LambdaQueryWrapper<Media>()
                .eq(Media::getDeleted, 0)
                .orderByDesc(Media::getId));
        // 一次建立「文件URL -> 引用列表」索引，之后按 URL 等值查找
        Map<String, List<MediaRefVO>> index = mediaRefResolver.buildRefIndex();

        List<MediaScanVO> items = new ArrayList<>(allMedia.size());
        for (Media m : allMedia) {
            List<MediaRefVO> refs = index.getOrDefault(m.getFileUrl(), List.of());
            items.add(MediaScanVO.of(m, refs));
        }
        return MediaScanResultVO.of(items);
    }

    @Override
    @Transactional
    public void deleteWithFile(Long id) {
        Media media = getById(id);
        if (media == null || media.getDeleted() == 1) {
            throw new BaseException("文件不存在或已删除");
        }

        // 不做引用校验：删除的依据是孤儿扫描的结果，
        // 判定集中在 MediaRefResolver，避免删除时再拦一道造成两处规则不一致
        minioUtil.deleteFile(media.getFilePath());
        removeById(id);
    }

    @Override
    @Transactional
    public Map<String, Object> batchDelete(List<Long> ids) {
        int success = 0;
        List<String> errors = new ArrayList<>();

        for (Long id : ids) {
            try {
                deleteWithFile(id);
                success++;
            } catch (BaseException e) {
                errors.add("id=" + id + ": " + e.getMessage());
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("errors", errors);
        return result;
    }
}
