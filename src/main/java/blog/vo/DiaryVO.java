package blog.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 日记（响应），活动条目内嵌
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiaryVO {

    private Long id;

    private LocalDate recordDate;

    /** 天气枚举值 */
    private Integer weather;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /** 当天的活动条目 */
    private List<DiaryActivityVO> logs;
}
