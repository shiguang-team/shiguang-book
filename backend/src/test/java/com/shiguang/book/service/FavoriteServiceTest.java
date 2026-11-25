package com.shiguang.book.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.shiguang.book.common.BizException;
import com.shiguang.book.entity.Book;
import com.shiguang.book.entity.Favorite;
import com.shiguang.book.entity.User;
import com.shiguang.book.mapper.BookMapper;
import com.shiguang.book.mapper.FavoriteMapper;
import com.shiguang.book.mapper.UserMapper;
import com.shiguang.book.vo.FavoriteVO;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FavoriteServiceTest {

    @Mock
    private FavoriteMapper favoriteMapper;

    @Mock
    private BookMapper bookMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private FavoriteService favoriteService;

    @BeforeAll
    static void initMybatisCache() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfoHelper.initTableInfo(assistant, Favorite.class);
        TableInfoHelper.initTableInfo(assistant, Book.class);
        TableInfoHelper.initTableInfo(assistant, User.class);
    }

    @Test
    void testAddFavoriteInsertsRecordWhenNew() {
        Book book = new Book();
        book.setId(100L);
        when(bookMapper.selectById(100L)).thenReturn(book);
        when(favoriteMapper.selectCount(any())).thenReturn(0L);

        favoriteService.addFavorite(1L, 100L);

        ArgumentCaptor<Favorite> captor = ArgumentCaptor.forClass(Favorite.class);
        verify(favoriteMapper).insert(captor.capture());
        assertEquals(1L, captor.getValue().getUserId());
        assertEquals(100L, captor.getValue().getBookId());
    }

    @Test
    void testAddFavoriteIdempotentWhenAlreadyFavorited() {
        when(bookMapper.selectById(100L)).thenReturn(new Book());
        when(favoriteMapper.selectCount(any())).thenReturn(1L);

        favoriteService.addFavorite(1L, 100L);
        verify(favoriteMapper, never()).insert(any(Favorite.class));
    }

    @Test
    void testAddFavoriteHandlesConcurrentDuplicateKeySafely() {
        when(bookMapper.selectById(100L)).thenReturn(new Book());
        when(favoriteMapper.selectCount(any())).thenReturn(0L);
        when(favoriteMapper.insert(any(Favorite.class))).thenThrow(new DuplicateKeyException("dup"));

        assertDoesNotThrow(() -> favoriteService.addFavorite(1L, 100L));
    }

    @Test
    void testAddFavoriteThrows404WhenBookMissing() {
        when(bookMapper.selectById(404L)).thenReturn(null);
        BizException ex = assertThrows(BizException.class, () -> favoriteService.addFavorite(1L, 404L));
        assertEquals(404, ex.getCode());
    }

    @Test
    void testRemoveAndIsFavorited() {
        assertFalse(favoriteService.isFavorited(null, 100L));

        when(favoriteMapper.selectCount(any())).thenReturn(1L);
        assertTrue(favoriteService.isFavorited(1L, 100L));

        favoriteService.removeFavorite(1L, 100L);
        verify(favoriteMapper).delete(any());
    }

    @Test
    void testListMyFavoritesHandlesOnShelfOffShelfAndDeletedBooks() {
        Favorite f1 = new Favorite();
        f1.setId(1L);
        f1.setUserId(9L);
        f1.setBookId(10L);
        f1.setCreatedAt(LocalDateTime.now());

        Favorite f2 = new Favorite();
        f2.setId(2L);
        f2.setUserId(9L);
        f2.setBookId(20L);
        f2.setCreatedAt(LocalDateTime.now());

        Favorite f3 = new Favorite();
        f3.setId(3L);
        f3.setUserId(9L);
        f3.setBookId(30L);
        f3.setCreatedAt(LocalDateTime.now());

        Book b1 = new Book();
        b1.setId(10L);
        b1.setTitle("高数上册");
        b1.setPrice(new BigDecimal("18.00"));
        b1.setConditionLevel(9);
        b1.setStatus(1);
        b1.setSellerId(2L);

        Book b2 = new Book();
        b2.setId(20L);
        b2.setTitle("概率论");
        b2.setPrice(new BigDecimal("15.00"));
        b2.setConditionLevel(8);
        b2.setStatus(0);
        b2.setSellerId(2L);

        User seller = new User();
        seller.setId(2L);
        seller.setNickname("张学长");

        when(favoriteMapper.selectList(any())).thenReturn(List.of(f1, f2, f3));
        when(bookMapper.selectBatchIds(any())).thenReturn(List.of(b1, b2));
        when(userMapper.selectBatchIds(any())).thenReturn(List.of(seller));

        List<FavoriteVO> list = favoriteService.listMyFavorites(9L);
        assertEquals(3, list.size());

        assertEquals("ON_SHELF", list.get(0).getStatus());
        assertEquals("张学长", list.get(0).getSellerNickname());
        assertEquals("OFF_SHELF", list.get(1).getStatus());
        assertEquals("DELETED", list.get(2).getStatus());
        assertEquals("【该书籍已被卖家删除】", list.get(2).getTitle());
    }

    @Test
    void testListMyFavoritesEmptyReturnsEmptyList() {
        when(favoriteMapper.selectList(any())).thenReturn(Collections.emptyList());
        List<FavoriteVO> list = favoriteService.listMyFavorites(9L);
        assertTrue(list.isEmpty());
        verify(bookMapper, never()).selectBatchIds(any());
    }
}