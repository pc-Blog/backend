package blog.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 日记写入请求（新建 / 更新共用）
 *
 * <p>更新时忽略 {@code recordDate}：日期不可修改，改了日期等于换了另一天，
 * 应当删除后重建。</p>
 */
@Data
public class DiaryRequestDTO {

    /** 更新时必填 */
    private Long id;

    /** 记录日期，新建时必填；更新时忽略 */
    private LocalDate recordDate;

    /** 天气枚举值 */
    @NotNull(message = "天气不能为空")
    private Integer weather;

    /** 当天的活动条目，整体替换 */
    @NotEmpty(message = "至少需要一条活动记录")
    @Valid
    private List<LogEntryDTO> logs;

    /** 单条活动 */
    @Data
    public static class LogEntryDTO {

        @NotEmpty(message = "活动内容不能为空")
        private String activity;

        /** 分类枚举值 */
        @NotNull(message = "活动分类不能为空")
        private Integer category;

        /** 小分类名称，可空 */
        private String subcategory;
    }
}
