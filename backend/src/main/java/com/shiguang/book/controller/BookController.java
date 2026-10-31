package com.shiguang.book.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shiguang.book.common.CurrentUser;
import com.shiguang.book.common.Result;
import com.shiguang.book.dto.BookDTO;
import com.shiguang.book.dto.BookQuery;
import com.shiguang.book.entity.Book;
import com.shiguang.book.service.BookService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    /** 首页列表 / 搜索：无需登录 */
    @GetMapping
    public Result<Page<Book>> page(BookQuery query) {
        return Result.ok(bookService.page(query));
    }

    /** 书籍详情：无需登录，已下架的书也能看（场景测试三的规则） */
    @GetMapping("/{id}")
    public Result<Book> detail(@PathVariable Long id) {
        return Result.ok(bookService.detail(id));
    }

    /** 我发布的书籍（个人中心） */
    @GetMapping("/mine")
    public Result<List<Book>> mine(HttpServletRequest request) {
        return Result.ok(bookService.mine(CurrentUser.get(request)));
    }

    @PostMapping
    public Result<Book> create(HttpServletRequest request, @Valid @RequestBody BookDTO dto) {
        return Result.ok(bookService.create(CurrentUser.get(request), dto));
    }

    @PutMapping("/{id}")
    public Result<Book> update(HttpServletRequest request,
                               @PathVariable Long id,
                               @Valid @RequestBody BookDTO dto) {
        return Result.ok(bookService.update(CurrentUser.get(request), id, dto));
    }

    /** 上架 / 下架 */
    @PutMapping("/{id}/status")
    public Result<Book> changeStatus(HttpServletRequest request,
                                     @PathVariable Long id,
                                     @RequestParam Integer status) {
        return Result.ok(bookService.changeStatus(CurrentUser.get(request), id, status));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(HttpServletRequest request, @PathVariable Long id) {
        bookService.delete(CurrentUser.get(request), id);
        return Result.ok();
    }
}
