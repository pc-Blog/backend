package blog.controller;

import blog.common.PageDTO;
import blog.common.PageVO;
import blog.common.Result;
import blog.entity.*;
import blog.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/sync-data")
public class SyncDataController {

    private final EmailService emailService;
    private final SubscriberService subscriberService;
    private final CommentReactionService commentReactionService;
    private final CommentUpvoteService commentUpvoteService;
    private final PushLogService pushLogService;

    public SyncDataController(EmailService emailService,
                              SubscriberService subscriberService,
                              CommentReactionService commentReactionService,
                              CommentUpvoteService commentUpvoteService,
                              PushLogService pushLogService) {
        this.emailService = emailService;
        this.subscriberService = subscriberService;
        this.commentReactionService = commentReactionService;
        this.commentUpvoteService = commentUpvoteService;
        this.pushLogService = pushLogService;
    }

    // ── Emails ──

    @GetMapping("/email/{id}")
    public Result<Email> getEmail(@PathVariable Long id) {
        return Result.success(emailService.getById(id));
    }

    @PostMapping("/email/page")
    public Result<PageVO<Email>> pageEmail(@RequestBody PageDTO<Email> dto) {
        return Result.success(emailService.page(dto));
    }

    // ── Subscribers ──

    @GetMapping("/subscriber/{id}")
    public Result<Subscriber> getSubscriber(@PathVariable Long id) {
        return Result.success(subscriberService.getById(id));
    }

    @PostMapping("/subscriber/page")
    public Result<PageVO<Subscriber>> pageSubscriber(@RequestBody PageDTO<Subscriber> dto) {
        return Result.success(subscriberService.page(dto));
    }

    // ── Comment Reactions ──

    @GetMapping("/reaction/{id}")
    public Result<CommentReaction> getReaction(@PathVariable Long id) {
        return Result.success(commentReactionService.getById(id));
    }

    @PostMapping("/reaction/page")
    public Result<PageVO<CommentReaction>> pageReaction(@RequestBody PageDTO<CommentReaction> dto) {
        return Result.success(commentReactionService.page(dto));
    }

    // ── Comment Upvotes ──

    @GetMapping("/upvote/{id}")
    public Result<CommentUpvote> getUpvote(@PathVariable Long id) {
        return Result.success(commentUpvoteService.getById(id));
    }

    @PostMapping("/upvote/page")
    public Result<PageVO<CommentUpvote>> pageUpvote(@RequestBody PageDTO<CommentUpvote> dto) {
        return Result.success(commentUpvoteService.page(dto));
    }

    // ── Push Logs ──

    @GetMapping("/push-log/{id}")
    public Result<PushLog> getPushLog(@PathVariable Long id) {
        return Result.success(pushLogService.getById(id));
    }

    @PostMapping("/push-log/page")
    public Result<PageVO<PushLog>> pagePushLog(@RequestBody PageDTO<PushLog> dto) {
        return Result.success(pushLogService.page(dto));
    }
}
