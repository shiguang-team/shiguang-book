package com.shiguang.book.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shiguang.book.common.BizException;
import com.shiguang.book.dto.BookDTO;
import com.shiguang.book.dto.BookQuery;
import com.shiguang.book.entity.Book;
import com.shiguang.book.entity.User;
import com.shiguang.book.mapper.BookMapper;
import com.shiguang.book.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookMapper bookMapper;
    private final UserMapper userMapper;

    /** 首页 / 搜索：分页 + 可选条件，条件为空绝不能传空串（BUG-04 的根因） */
    public Page<Book> page(BookQuery q) {
        LambdaQueryWrapper<Book> w = new LambdaQueryWrapper<>();
        if (q.getOnlyOnSale() == null || q.getOnlyOnSale() == 1) {
            w.eq(Book::getStatus, 1);
        }
        if (StringUtils.hasText(q.getKeyword())) {
            String kw = q.getKeyword().trim();
            w.and(x -> x.like(Book::getTitle, kw).or().like(Book::getDescription, kw));
        }
        if (StringUtils.hasText(q.getCategory())) {
            w.eq(Book::getCategory, q.getCategory().trim());
        }
        if (q.getMinPrice() != null) {
            w.ge(Book::getPrice, q.getMinPrice());
        }
        if (q.getMaxPrice() != null) {
            w.le(Book::getPrice, q.getMaxPrice());
        }
        switch (q.getSort() == null ? "newest" : q.getSort()) {
            case "priceAsc" -> w.orderByAsc(Book::getPrice);
            case "priceDesc" -> w.orderByDesc(Book::getPrice);
            default -> w.orderByDesc(Book::getCreatedAt);
        }

        int pageNo = q.getPage() == null || q.getPage() < 1 ? 1 : q.getPage();
        int pageSize = q.getSize() == null || q.getSize() < 1 || q.getSize() > 50 ? 12 : q.getSize();
        Page<Book> page = bookMapper.selectPage(new Page<>(pageNo, pageSize), w);
        fillSeller(page.getRecords());
        return page;
    }

    public Book detail(Long id) {
        Book book = bookMapper.selectById(id);
        if (book == null) {
            throw new BizException(404, "书籍不存在或已删除");
        }
        fillSeller(Collections.singletonList(book));
        return book;
    }

    public Book create(Long uid, BookDTO dto) {
        Book book = new Book();
        copy(dto, book);
        book.setSellerId(uid);
        book.setStatus(1);
        book.setDeleted(0);
        book.setCreatedAt(LocalDateTime.now());
        book.setUpdatedAt(LocalDateTime.now());
        bookMapper.insert(book);
        return book;
    }

    public Book update(Long uid, Long id, BookDTO dto) {
        Book book = mustOwn(uid, id);
        copy(dto, book);
        book.setUpdatedAt(LocalDateTime.now());
        bookMapper.updateById(book);
        return book;
    }

    /** 上架 / 下架：0 下架 1 上架 */
    public Book changeStatus(Long uid, Long id, Integer status) {
        Book book = mustOwn(uid, id);
        book.setStatus(status);
        book.setUpdatedAt(LocalDateTime.now());
        bookMapper.updateById(book);
        return book;
    }

    /** 软删除，历史留言保留 */
    public void delete(Long uid, Long id) {
        mustOwn(uid, id);
        bookMapper.deleteById(id);
    }

    public List<Book> mine(Long uid) {
        List<Book> list = bookMapper.selectList(new LambdaQueryWrapper<Book>()
                .eq(Book::getSellerId, uid)
                .orderByDesc(Book::getCreatedAt));
        fillSeller(list);
        return list;
    }

    private Book mustOwn(Long uid, Long id) {
        Book book = bookMapper.selectById(id);
        if (book == null) {
            throw new BizException(404, "书籍不存在或已删除");
        }
        if (!Objects.equals(book.getSellerId(), uid)) {
            throw new BizException(403, "只能操作自己发布的书籍");
        }
        return book;
    }

    private void copy(BookDTO dto, Book book) {
        book.setTitle(dto.getTitle().trim());
        book.setCategory(dto.getCategory());
        book.setConditionLevel(dto.getConditionLevel());
        book.setPrice(dto.getPrice());
        book.setDescription(dto.getDescription());
        book.setCover(dto.getCover());
    }

    /** 一次性把卖家昵称/院系回填，避免前端为每条书再发一次请求 */
    private void fillSeller(List<Book> books) {
        if (books == null || books.isEmpty()) {
            return;
        }
        Set<Long> ids = books.stream().map(Book::getSellerId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return;
        }
        Map<Long, User> map = new HashMap<>();
        for (User u : userMapper.selectBatchIds(ids)) {
            map.put(u.getId(), u);
        }
        for (Book b : books) {
            User u = map.get(b.getSellerId());
            if (u != null) {
                b.setSellerNickname(u.getNickname());
                b.setSellerDept(u.getDept());
            }
        }
    }
}
// feat(#13): 支持按时间 (created_at DESC)、价格升序 (price ASC)、成色降序 (condition_level DESC) 三种安全排序
