package blog.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 日记活动条目（响应）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiaryActivityVO {

    private Long id;

    private String activity;

    /** 分类枚举值 */
    private Integer category;

    /** 小分类名称 */
    private String subcategory;
}
