package com.shiguang.book.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shiguang.book.common.BizException;
import com.shiguang.book.dto.BookDTO;
import com.shiguang.book.dto.BookQuery;
import com.shiguang.book.entity.Book;
import com.shiguang.book.entity.User;
import com.shiguang.book.mapper.BookMapper;
import com.shiguang.book.mapper.UserMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookServiceTest {

    @Mock
    private BookMapper bookMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private BookService bookService;

    @BeforeAll
    static void initMybatisCache() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfoHelper.initTableInfo(assistant, Book.class);
        TableInfoHelper.initTableInfo(assistant, User.class);
    }

    @Test
    void testPageWithFiltersAndSortsFillsSellerInfo() {
        Book book = new Book();
        book.setId(1L);
        book.setTitle("计算机网络");
        book.setSellerId(5L);

        Page<Book> mockPage = new Page<>(1, 12);
        mockPage.setRecords(List.of(book));
        mockPage.setTotal(1);

        User seller = new User();
        seller.setId(5L);
        seller.setNickname("小张");
        seller.setDept("计算机学院");

        when(bookMapper.selectPage(any(), any())).thenReturn(mockPage);
        when(userMapper.selectBatchIds(any())).thenReturn(List.of(seller));

        for (String sort : List.of("newest", "priceAsc", "priceDesc", "conditionDesc")) {
            BookQuery q = new BookQuery();
            q.setKeyword("网络");
            q.setCategory("教材");
            q.setMinPrice(new BigDecimal("10"));
            q.setMaxPrice(new BigDecimal("50"));
            q.setSort(sort);

            Page<Book> result = bookService.page(q);
            assertEquals(1, result.getTotal());
            assertEquals("小张", result.getRecords().get(0).getSellerNickname());
            assertEquals("计算机学院", result.getRecords().get(0).getSellerDept());
        }
    }

    @Test
    void testDetailFoundAndMissingThrows404() {
        Book book = new Book();
        book.setId(10L);
        book.setTitle("算法导论");
        book.setSellerId(2L);

        User seller = new User();
        seller.setId(2L);
        seller.setNickname("李同学");
        seller.setDept("软件学院");

        when(bookMapper.selectById(10L)).thenReturn(book);
        when(userMapper.selectBatchIds(any())).thenReturn(List.of(seller));
        when(bookMapper.selectById(99L)).thenReturn(null);

        Book found = bookService.detail(10L);
        assertEquals("李同学", found.getSellerNickname());

        BizException ex = assertThrows(BizException.class, () -> bookService.detail(99L));
        assertEquals(404, ex.getCode());
    }

    @Test
    void testCreateTrimsTitleAndSetsDefaults() {
        BookDTO dto = new BookDTO();
        dto.setTitle("  线性代数  ");
        dto.setCategory("教材");
        dto.setConditionLevel(9);
        dto.setPrice(new BigDecimal("18.50"));
        dto.setDescription("九成新");

        Book created = bookService.create(7L, dto);

        ArgumentCaptor<Book> captor = ArgumentCaptor.forClass(Book.class);
        verify(bookMapper).insert(captor.capture());
        Book saved = captor.getValue();

        assertEquals("线性代数", saved.getTitle());
        assertEquals(7L, saved.getSellerId());
        assertEquals(1, saved.getStatus());
        assertEquals(0, saved.getDeleted());
        assertEquals("线性代数", created.getTitle());
    }

    @Test
    void testUpdateSucceedsWhenOwnerMatches() {
        Book existing = new Book();
        existing.setId(11L);
        existing.setSellerId(3L);
        when(bookMapper.selectById(11L)).thenReturn(existing);

        BookDTO dto = new BookDTO();
        dto.setTitle("更新后书名");
        dto.setCategory("考研");
        dto.setConditionLevel(8);
        dto.setPrice(new BigDecimal("25.00"));

        Book updated = bookService.update(3L, 11L, dto);
        assertEquals("更新后书名", updated.getTitle());
        verify(bookMapper).updateById(existing);
    }

    @Test
    void testUpdateThrows403WhenUserMismatch() {
        Book existing = new Book();
        existing.setId(11L);
        existing.setSellerId(3L);
        when(bookMapper.selectById(11L)).thenReturn(existing);

        BookDTO dto = new BookDTO();
        dto.setTitle("恶意修改");

        BizException ex = assertThrows(BizException.class, () -> bookService.update(99L, 11L, dto));
        assertEquals(403, ex.getCode());
        verify(bookMapper, never()).updateById(any(Book.class));
    }

    @Test
    void testOffShelfRecordsReasonAndRestoreClearsIt() {
        Book existing = new Book();
        existing.setId(12L);
        existing.setSellerId(1L);
        existing.setStatus(1);
        when(bookMapper.selectById(12L)).thenReturn(existing);

        Book off = bookService.changeStatus(1L, 12L, 0, " 已线下交易 ");
        assertEquals(0, off.getStatus());
        assertEquals("已线下交易", off.getOffShelfReason());

        Book restored = bookService.changeStatus(1L, 12L, 1, null);
        assertEquals(1, restored.getStatus());
        assertNull(restored.getOffShelfReason());
    }

    @Test
    void testChangeStatusRejectsInvalidStatusWith400() {
        BizException ex = assertThrows(BizException.class,
                () -> bookService.changeStatus(1L, 12L, 9, null));
        assertEquals(400, ex.getCode());
    }

    @Test
    void testDeleteChecksOwnership() {
        Book existing = new Book();
        existing.setId(20L);
        existing.setSellerId(5L);
        when(bookMapper.selectById(20L)).thenReturn(existing);

        BizException ex = assertThrows(BizException.class, () -> bookService.delete(6L, 20L));
        assertEquals(403, ex.getCode());
        verify(bookMapper, never()).deleteById(20L);

        bookService.delete(5L, 20L);
        verify(bookMapper).deleteById(20L);
    }

    @Test
    void testMineReturnsSellerBooks() {
        Book b = new Book();
        b.setId(1L);
        b.setSellerId(5L);
        User seller = new User();
        seller.setId(5L);
        seller.setNickname("小王");

        when(bookMapper.selectList(any())).thenReturn(List.of(b));
        when(userMapper.selectBatchIds(any())).thenReturn(List.of(seller));

        List<Book> mine = bookService.mine(5L);
        assertEquals(1, mine.size());
        assertEquals("小王", mine.get(0).getSellerNickname());
    }
}