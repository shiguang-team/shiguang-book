package com.shiguang.book.controller;

import com.shiguang.book.service.FavoriteService;
import com.shiguang.book.vo.FavoriteVO;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {
    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @PostMapping("/{bookId}")
    public Map<String, Object> add(@RequestAttribute("userId") Long userId, @PathVariable Long bookId) {
        favoriteService.addFavorite(userId, bookId);
        return Map.of("code", 200, "message", "收藏成功", "data", true);
    }

    @DeleteMapping("/{bookId}")
    public Map<String, Object> remove(@RequestAttribute("userId") Long userId, @PathVariable Long bookId) {
        favoriteService.removeFavorite(userId, bookId);
        return Map.of("code", 200, "message", "已取消收藏", "data", true);
    }

    @GetMapping
    public Map<String, Object> list(@RequestAttribute("userId") Long userId) {
        List<FavoriteVO> data = favoriteService.listMyFavorites(userId);
        return Map.of("code", 200, "message", "ok", "data", data);
    }
}
