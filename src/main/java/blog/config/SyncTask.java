package blog.config;

import blog.service.SyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SyncTask {

    private final SyncService syncService;

    public SyncTask(SyncService syncService) {
        this.syncService = syncService;
    }

    /** 每周日凌晨 3:00 自动同步一次 */
    @Scheduled(cron = "0 0 3 * * 0")
    public void autoSync() {
        log.info("定时同步开始");
        var result = syncService.syncAll(false);
        log.info("定时同步结束: {}", result);
    }
}
