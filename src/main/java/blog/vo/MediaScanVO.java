package blog.vo;

import blog.entity.Media;
import lombok.Data;

import java.util.List;

/**
 * 媒体文件 + 其引用情况
 *
 * <p>孤儿扫描的返回单元：{@code refs} 为空即表示该文件无人引用。</p>
 */
@Data
public class MediaScanVO {

    private Long id;
    private String filename;
    private String originalFilename;
    private String filePath;
    private String fileUrl;
    private Long fileSize;
    private String mimeType;
    private String relationType;

    /** 引用来源列表；为空表示孤儿 */
    private List<MediaRefVO> refs;

    public static MediaScanVO of(Media media, List<MediaRefVO> refs) {
        MediaScanVO vo = new MediaScanVO();
        vo.setId(media.getId());
        vo.setFilename(media.getFilename());
        vo.setOriginalFilename(media.getOriginalFilename());
        vo.setFilePath(media.getFilePath());
        vo.setFileUrl(media.getFileUrl());
        vo.setFileSize(media.getFileSize());
        vo.setMimeType(media.getMimeType());
        vo.setRelationType(media.getRelationType());
        vo.setRefs(refs);
        return vo;
    }

    /** 是否孤儿（便捷判断，等价于 refs 为空） */
    public boolean isOrphan() {
        return refs == null || refs.isEmpty();
    }
}
