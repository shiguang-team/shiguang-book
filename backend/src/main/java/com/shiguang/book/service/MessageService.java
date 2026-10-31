package com.shiguang.book.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shiguang.book.common.BizException;
import com.shiguang.book.dto.MessageDTO;
import com.shiguang.book.entity.Book;
import com.shiguang.book.entity.Message;
import com.shiguang.book.entity.User;
import com.shiguang.book.mapper.BookMapper;
import com.shiguang.book.mapper.MessageMapper;
import com.shiguang.book.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
public class MessageService {

    private final MessageMapper messageMapper;
    private final BookMapper bookMapper;
    private final UserMapper userMapper;

    /** 某本书下的留言（公开，未登录也能看） */
    public List<Message> listByBook(Long bookId) {
        List<Message> list = messageMapper.selectList(new LambdaQueryWrapper<Message>()
                .eq(Message::getBookId, bookId)
                .orderByDesc(Message::getCreatedAt));
        fillSender(list);
        return list;
    }

    public Message create(Long uid, MessageDTO dto) {
        Book book = bookMapper.selectById(dto.getBookId());
        if (book == null) {
            throw new BizException(404, "书籍不存在");
        }
        // 场景测试三定的规则：已下架的书可以看，但不能再新增留言
        if (book.getStatus() != null && book.getStatus() == 0) {
            throw new BizException(409, "该书籍已下架，无法留言");
        }
        if (Objects.equals(book.getSellerId(), uid)) {
            throw new BizException(400, "不能给自己的书留言");
        }

        Message m = new Message();
        m.setBookId(dto.getBookId());
        m.setSenderId(uid);
        m.setReceiverId(book.getSellerId());
        m.setContent(dto.getContent().trim());
        m.setIsRead(0);
        m.setDeleted(0);
        m.setCreatedAt(LocalDateTime.now());
        messageMapper.insert(m);

        fillSender(Collections.singletonList(m));
        return m;
    }

    public void delete(Long uid, Long id) {
        Message m = messageMapper.selectById(id);
        if (m == null) {
            throw new BizException(404, "留言不存在");
        }
        if (!Objects.equals(m.getSenderId(), uid)) {
            throw new BizException(403, "只能删除自己发的留言");
        }
        messageMapper.deleteById(id);
    }

    /** 我收到的留言（个人中心） */
    public List<Message> received(Long uid) {
        List<Message> list = messageMapper.selectList(new LambdaQueryWrapper<Message>()
                .eq(Message::getReceiverId, uid)
                .orderByDesc(Message::getCreatedAt));
        fillSender(list);
        return list;
    }

    private void fillSender(List<Message> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        Set<Long> ids = list.stream().map(Message::getSenderId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return;
        }
        Map<Long, User> map = new HashMap<>();
        for (User u : userMapper.selectBatchIds(ids)) {
            map.put(u.getId(), u);
        }
        for (Message m : list) {
            User u = map.get(m.getSenderId());
            if (u != null) {
                m.setSenderNickname(u.getNickname());
            }
        }
    }
}
