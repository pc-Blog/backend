package blog.vo;

import lombok.Data;

/**
 * 媒体引用信息
 *
 * <p>描述某个媒体文件被哪条业务数据引用，用于孤儿扫描结果展示与删除前校验。</p>
 */
@Data
public class MediaRefVO {

    /** 引用来源类型：article / project / about / album / chatter / literature / music / singer / user / friendLink */
    private String type;

    /** 引用者标题，便于在管理页辨认 */
    private String title;

    /** 引用者主键 ID */
    private Long id;

    /** 引用所在字段：coverImage / content / fileUrl / avatar / pictureUrl */
    private String field;

    public MediaRefVO() {
    }

    public MediaRefVO(String type, String title, Long id, String field) {
        this.type = type;
        this.title = title;
        this.id = id;
        this.field = field;
    }
}
