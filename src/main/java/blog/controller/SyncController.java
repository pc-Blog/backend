package blog.controller;

import blog.common.Result;
import blog.service.SyncService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sync")
public class SyncController {

    private final SyncService syncService;

    public SyncController(SyncService syncService) {
        this.syncService = syncService;
    }

    /** 全量覆盖同步所有数据 */
    @PostMapping("/all")
    public Result<?> syncAll() {
        return syncService.syncAll();
    }

    /** 全量覆盖同步指定表: /api/sync/table/views */
    @PostMapping("/table/{name}")
    public Result<?> syncTable(@PathVariable String name) {
        return syncService.syncTable(name);
    }

    /** 查看各表数据更新到什么时候 */
    @GetMapping("/status")
    public Result<?> syncStatus() {
        return syncService.syncStatus();
    }
}
