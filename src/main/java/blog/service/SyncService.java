package blog.service;

import blog.common.Result;

import java.time.LocalDateTime;
import java.util.Map;

public interface SyncService {
    /** 全量同步所有数据 */
    Result<Map<String, Object>> syncAll(boolean overwrite);

    /** 同步某张表的数据 */
    Result<Map<String, Object>> syncTable(String tableName, boolean overwrite);

    /** 查看各表最近的同步时间 */
    Result<Map<String, LocalDateTime>> syncStatus();
}
