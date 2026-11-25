package com.shiguang.book.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shiguang.book.common.BizException;
import com.shiguang.book.entity.Book;
import com.shiguang.book.entity.Favorite;
import com.shiguang.book.entity.User;
import com.shiguang.book.mapper.BookMapper;
import com.shiguang.book.mapper.FavoriteMapper;
import com.shiguang.book.mapper.UserMapper;
import com.shiguang.book.vo.FavoriteVO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteMapper favoriteMapper;
    private final BookMapper bookMapper;
    private final UserMapper userMapper;

    public void addFavorite(Long userId, Long bookId) {
        Book book = bookMapper.selectById(bookId);
        if (book == null) {
            throw new BizException(404, "书籍不存在或已删除");
        }
        if (isFavorited(userId, bookId)) {
            return;
        }
        Favorite fav = new Favorite();
        fav.setUserId(userId);
        fav.setBookId(bookId);
        fav.setCreatedAt(LocalDateTime.now());
        try {
            favoriteMapper.insert(fav);
        } catch (DuplicateKeyException ignored) {
            // 并发重复点击时依赖唯一索引幂等忽略
        }
    }

    public void removeFavorite(Long userId, Long bookId) {
        favoriteMapper.delete(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getBookId, bookId));
    }

    public boolean isFavorited(Long userId, Long bookId) {
        if (userId == null || bookId == null) {
            return false;
        }
        Long count = favoriteMapper.selectCount(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getBookId, bookId));
        return count != null && count > 0;
    }

    public List<FavoriteVO> listMyFavorites(Long userId) {
        List<Favorite> favorites = favoriteMapper.selectList(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .orderByDesc(Favorite::getCreatedAt));
        if (favorites == null || favorites.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> bookIds = favorites.stream()
                .map(Favorite::getBookId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, Book> bookMap = new HashMap<>();
        if (!bookIds.isEmpty()) {
            for (Book b : bookMapper.selectBatchIds(bookIds)) {
                bookMap.put(b.getId(), b);
            }
        }

        Set<Long> sellerIds = bookMap.values().stream()
                .map(Book::getSellerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, User> sellerMap = new HashMap<>();
        if (!sellerIds.isEmpty()) {
            for (User u : userMapper.selectBatchIds(sellerIds)) {
                sellerMap.put(u.getId(), u);
            }
        }

        List<FavoriteVO> result = new ArrayList<>(favorites.size());
        for (Favorite fav : favorites) {
            FavoriteVO vo = new FavoriteVO();
            vo.setId(fav.getId());
            vo.setBookId(fav.getBookId());
            vo.setCreatedAt(fav.getCreatedAt());

            Book book = bookMap.get(fav.getBookId());
            if (book == null) {
                // 卖家已逻辑删除该书，做安全降级展示
                vo.setTitle("【该书籍已被卖家删除】");
                vo.setStatus("DELETED");
            } else {
                vo.setTitle(book.getTitle());
                vo.setCoverUrl(book.getCover());
                vo.setPrice(book.getPrice());
                vo.setConditionLevel(book.getConditionLevel());
                vo.setStatus(book.getStatus() != null && book.getStatus() == 1 ? "ON_SHELF" : "OFF_SHELF");
                User seller = sellerMap.get(book.getSellerId());
                if (seller != null) {
                    vo.setSellerNickname(seller.getNickname());
                }
            }
            result.add(vo);
        }
        return result;
    }
}