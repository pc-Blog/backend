package blog.vo;

import lombok.Data;

import java.util.List;

/**
 * 孤儿扫描结果汇总
 */
@Data
public class MediaScanResultVO {

    /** 全部媒体文件及其引用情况 */
    private List<MediaScanVO> items;

    /** 媒体总数 */
    private long totalMedia;

    /** 孤儿数量（refs 为空的条数） */
    private long orphanCount;

    public static MediaScanResultVO of(List<MediaScanVO> items) {
        MediaScanResultVO result = new MediaScanResultVO();
        result.setItems(items);
        result.setTotalMedia(items.size());
        result.setOrphanCount(items.stream().filter(MediaScanVO::isOrphan).count());
        return result;
    }
}
