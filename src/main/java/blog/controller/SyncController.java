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

    /** 全量同步所有数据 */
    @PostMapping("/all")
    public Result<?> syncAll(@RequestParam(defaultValue = "false") boolean overwrite) {
        return syncService.syncAll(overwrite);
    }

    /** 同步指定表: /api/sync/table/views */
    @PostMapping("/table/{name}")
    public Result<?> syncTable(@PathVariable String name,
                               @RequestParam(defaultValue = "false") boolean overwrite) {
        return syncService.syncTable(name, overwrite);
    }

    /** 查看各表最近的同步时间 */
    @GetMapping("/status")
    public Result<?> syncStatus() {
        return syncService.syncStatus();
    }
}
