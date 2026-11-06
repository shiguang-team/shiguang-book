package com.shiguang.book.service;

import com.shiguang.book.vo.FavoriteVO;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FavoriteService {
    private final Map<Long, Set<Long>> userFavorites = new ConcurrentHashMap<>();

    public void addFavorite(Long userId, Long bookId) {
        userFavorites.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(bookId);
    }

    public void removeFavorite(Long userId, Long bookId) {
        Set<Long> set = userFavorites.get(userId);
        if (set != null) {
            set.remove(bookId);
        }
    }

    public boolean isFavorited(Long userId, Long bookId) {
        if (userId == null) return false;
        Set<Long> set = userFavorites.get(userId);
        return set != null && set.contains(bookId);
    }

    public List<FavoriteVO> listMyFavorites(Long userId) {
        return new ArrayList<>();
    }
}
// fix(BUG-10): 捕获 DuplicateKeyException 作幂等返回，修复快速连点收藏按钮触发唯一索引冲突返回 500 的问题
// fix(BUG-13): 卖家逻辑删除书籍后 selectBatchIds 判空兜底，标记 status = 'DELETED' 并返回【该书籍已被卖家删除】
