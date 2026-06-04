package blog.service.impl;

import blog.common.PageDTO;
import blog.common.PageVO;
import blog.entity.Article;
import blog.entity.Media;
import blog.entity.Project;
import blog.exception.BaseException;
import blog.mapper.ArticleMapper;
import blog.mapper.MediaMapper;
import blog.mapper.ProjectMapper;
import blog.service.MediaService;
import blog.util.MinioUtil;
import blog.util.PageUtil;
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
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/svg+xml",
            "application/pdf",
            "application/zip", "application/x-zip-compressed"
    );

    private final MinioUtil minioUtil;
    private final ArticleMapper articleMapper;
    private final ProjectMapper projectMapper;

    public MediaServiceImpl(MinioUtil minioUtil, ArticleMapper articleMapper, ProjectMapper projectMapper) {
        this.minioUtil = minioUtil;
        this.articleMapper = articleMapper;
        this.projectMapper = projectMapper;
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
    @Transactional
    public void deleteWithFile(Long id) {
        Media media = getById(id);
        if (media == null || media.getDeleted() == 1) {
            throw new BaseException("文件不存在或已删除");
        }

        checkReferences(media.getFileUrl());

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

    private void checkReferences(String fileUrl) {
        var articleWrapper = new LambdaQueryWrapper<Article>()
                .like(Article::getCoverImage, fileUrl)
                .eq(Article::getDeleted, 0);
        long articleCount = articleMapper.selectCount(articleWrapper);

        var projectWrapper = new LambdaQueryWrapper<Project>()
                .like(Project::getCoverImage, fileUrl)
                .eq(Project::getDeleted, 0);
        long projectCount = projectMapper.selectCount(projectWrapper);

        if (articleCount > 0 || projectCount > 0) {
            List<String> refs = new ArrayList<>();
            if (articleCount > 0) refs.add(articleCount + "篇文章");
            if (projectCount > 0) refs.add(projectCount + "个项目");
            throw new BaseException("文件被" + String.join("、", refs) + "引用，无法删除");
        }
    }
}
