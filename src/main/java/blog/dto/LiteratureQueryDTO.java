package blog.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 文学作品查询条件
 */
@Data
public class LiteratureQueryDTO {
    /** 标题模糊匹配 */
    private String title;

    /** 分类ID */
    private Long categoryId;

    /** 发布状态：0=隐藏 1=发布 */
    private Integer isPublished;

    /** 写作日期起（含） */
    private LocalDate startDate;

    /** 写作日期止（含） */
    private LocalDate endDate;
}
