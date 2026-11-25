package com.shiguang.book.controller;

import com.shiguang.book.common.CurrentUser;
import com.shiguang.book.common.Result;
import com.shiguang.book.service.FavoriteService;
import com.shiguang.book.vo.FavoriteVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/{bookId}")
    public Result<Void> add(HttpServletRequest request, @PathVariable Long bookId) {
        favoriteService.addFavorite(CurrentUser.get(request), bookId);
        return Result.ok();
    }

    @DeleteMapping("/{bookId}")
    public Result<Void> remove(HttpServletRequest request, @PathVariable Long bookId) {
        favoriteService.removeFavorite(CurrentUser.get(request), bookId);
        return Result.ok();
    }

    @GetMapping
    public Result<List<FavoriteVO>> list(HttpServletRequest request) {
        return Result.ok(favoriteService.listMyFavorites(CurrentUser.get(request)));
    }

    @GetMapping("/check/{bookId}")
    public Result<Boolean> check(HttpServletRequest request, @PathVariable Long bookId) {
        Long uid = CurrentUser.optional(request);
        return Result.ok(favoriteService.isFavorited(uid, bookId));
    }
}