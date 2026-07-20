package blog.controller;

import blog.common.PageDTO;
import blog.common.PageVO;
import blog.common.Result;
import blog.entity.Bookmark;
import blog.entity.BookmarkCategory;
import blog.service.BookmarkCategoryService;
import blog.service.BookmarkService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/bookmark")
public class BookmarkController {

    private final BookmarkService bookmarkService;
    private final BookmarkCategoryService bookmarkCategoryService;

    public BookmarkController(BookmarkService bookmarkService, BookmarkCategoryService bookmarkCategoryService) {
        this.bookmarkService = bookmarkService;
        this.bookmarkCategoryService = bookmarkCategoryService;
    }

    @GetMapping("/pinned")
    public Result<List<Bookmark>> getPinnedList() {
        log.info("查询已 pin 的收藏网站");
        return Result.success(bookmarkService.getPinnedList());
    }

    @GetMapping("/list")
    public Result<List<Bookmark>> getFullList() {
        log.info("查询全部收藏网站");
        return Result.success(bookmarkService.getFullList());
    }

    @GetMapping("/{id}")
    public Result<Bookmark> getById(@PathVariable Long id) {
        log.info("根据ID查询收藏网站, id:{}", id);
        return Result.success(bookmarkService.getById(id));
    }

    @PostMapping
    public Result<Void> save(@Valid @RequestBody Bookmark bookmark) {
        log.info("新增收藏网站:{}", JSON.toJSONString(bookmark, SerializerFeature.PrettyFormat));
        bookmarkService.save(bookmark);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@Valid @RequestBody Bookmark bookmark) {
        log.info("更新收藏网站:{}", JSON.toJSONString(bookmark, SerializerFeature.PrettyFormat));
        bookmarkService.updateById(bookmark);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除收藏网站, id:{}", id);
        bookmarkService.removeById(id);
        return Result.success();
    }

    @PostMapping("/page")
    public Result<PageVO<Bookmark>> page(@RequestBody PageDTO<Bookmark> dto) {
        log.info("分页查询收藏网站:{}", JSON.toJSONString(dto, SerializerFeature.PrettyFormat));
        return Result.success(bookmarkService.page(dto));
    }

    @GetMapping("/category/tree")
    public Result<List<BookmarkCategory>> getCategoryTree() {
        log.info("查询收藏分类树");
        return Result.success(bookmarkCategoryService.getTree());
    }

    @PostMapping("/category")
    public Result<Void> saveCategory(@Valid @RequestBody BookmarkCategory category) {
        log.info("新增收藏分类:{}", JSON.toJSONString(category, SerializerFeature.PrettyFormat));
        bookmarkCategoryService.save(category);
        return Result.success();
    }

    @PutMapping("/category")
    public Result<Void> updateCategory(@Valid @RequestBody BookmarkCategory category) {
        log.info("更新收藏分类:{}", JSON.toJSONString(category, SerializerFeature.PrettyFormat));
        bookmarkCategoryService.updateById(category);
        return Result.success();
    }

    @DeleteMapping("/category/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        log.info("删除收藏分类, id:{}", id);
        bookmarkCategoryService.removeById(id);
        return Result.success();
    }
}
