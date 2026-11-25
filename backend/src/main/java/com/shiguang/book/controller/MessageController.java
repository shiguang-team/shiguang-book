package com.shiguang.book.controller;

import com.shiguang.book.common.CurrentUser;
import com.shiguang.book.common.Result;
import com.shiguang.book.dto.MessageDTO;
import com.shiguang.book.entity.Message;
import com.shiguang.book.service.MessageService;
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
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /** 某本书的留言列表：详情页用，无需登录 */
    @GetMapping
    public Result<List<Message>> listByBook(@RequestParam Long bookId) {
        return Result.ok(messageService.listByBook(bookId));
    }

    @PostMapping
    public Result<Message> create(HttpServletRequest request, @Valid @RequestBody MessageDTO dto) {
        return Result.ok(messageService.create(CurrentUser.get(request), dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(HttpServletRequest request, @PathVariable Long id) {
        messageService.delete(CurrentUser.get(request), id);
        return Result.ok();
    }

    /** 我收到的留言（个人中心） */
    @GetMapping("/received")
    public Result<List<Message>> received(HttpServletRequest request) {
        return Result.ok(messageService.received(CurrentUser.get(request)));
    }

    /** 未读留言数 */
    @GetMapping("/unread-count")
    public Result<Map<String, Long>> unreadCount(HttpServletRequest request) {
        long count = messageService.unreadCount(CurrentUser.get(request));
        return Result.ok(Map.of("unreadCount", count));
    }

    /** 将收到的留言全部标为已读 */
    @PutMapping("/read-all")
    public Result<Void> readAll(HttpServletRequest request) {
        messageService.markAllRead(CurrentUser.get(request));
        return Result.ok();
    }
}