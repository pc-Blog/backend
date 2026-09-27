package blog.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论（D1 → PostgreSQL 归档镜像）
 *
 * <p>评论正文主库在 Worker 的 D1，此表仅由同步任务从 Worker 拉取归档，
 * 供后台查看与备份，站点不直接读写。</p>
 *
 * @author blog
 */
@Data
@TableName("t_comment")
public class Comment {
    /** 主键，沿用 D1 的 comment.id */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 评论挂载位置，如 /article/12、/friends */
    private String path;

    /** 父评论 id，顶层评论为 null */
    private Long parentId;

    /** 作者 user id */
    private Long userId;

    /** 评论正文（Markdown 原文） */
    private String content;

    /**
     * 是否已删除：0 正常 / 1 已删除。
     *
     * <p>D1 的列名是 {@code deleted}，属性名对不上，Jackson 与 fastjson 都需要显式指明列名。
     */
    @TableField("deleted")
    @JsonProperty("deleted")
    @JSONField(name = "deleted")
    private Integer deletedFlag;

    /** 创建时间（D1 的记录时间） */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
