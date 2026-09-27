package blog.service;

import blog.common.Result;

import java.time.LocalDateTime;
import java.util.Map;

public interface SyncService {
    /** 全量覆盖同步所有表 */
    Result<Map<String, Object>> syncAll();

    /** 全量覆盖同步某张表 */
    Result<Map<String, Object>> syncTable(String tableName);

    /** 查看各表数据更新到什么时候 */
    Result<Map<String, LocalDateTime>> syncStatus();
}
