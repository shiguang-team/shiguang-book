package com.shiguang.book.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.shiguang.book.common.BizException;
import com.shiguang.book.dto.MessageDTO;
import com.shiguang.book.entity.Book;
import com.shiguang.book.entity.Message;
import com.shiguang.book.entity.User;
import com.shiguang.book.mapper.BookMapper;
import com.shiguang.book.mapper.MessageMapper;
import com.shiguang.book.mapper.UserMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MessageServiceTest {

    @Mock
    private MessageMapper messageMapper;

    @Mock
    private BookMapper bookMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private MessageService messageService;

    @BeforeAll
    static void initMybatisCache() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfoHelper.initTableInfo(assistant, Message.class);
        TableInfoHelper.initTableInfo(assistant, Book.class);
        TableInfoHelper.initTableInfo(assistant, User.class);
    }

    @Test
    void testCreateSuccessSetsReceiverAndTrimsContent() {
        Book book = new Book();
        book.setId(5L);
        book.setSellerId(1L);
        book.setStatus(1);

        User buyer = new User();
        buyer.setId(2L);
        buyer.setNickname("买家小李");

        when(bookMapper.selectById(5L)).thenReturn(book);
        when(userMapper.selectBatchIds(any())).thenReturn(List.of(buyer));

        MessageDTO dto = new MessageDTO();
        dto.setBookId(5L);
        dto.setContent("  请问可以图书馆面交吗？ ");

        Message msg = messageService.create(2L, dto);

        assertEquals(1L, msg.getReceiverId());
        assertEquals(0, msg.getIsRead());
        assertEquals("请问可以图书馆面交吗？", msg.getContent());
        assertEquals("买家小李", msg.getSenderNickname());
        verify(messageMapper).insert(any(Message.class));
    }

    @Test
    void testCreateThrows404WhenBookMissing() {
        when(bookMapper.selectById(99L)).thenReturn(null);
        MessageDTO dto = new MessageDTO();
        dto.setBookId(99L);
        dto.setContent("你好");

        BizException ex = assertThrows(BizException.class, () -> messageService.create(2L, dto));
        assertEquals(404, ex.getCode());
    }

    @Test
    void testCreateThrows409WhenBookOffShelf() {
        Book book = new Book();
        book.setId(5L);
        book.setSellerId(1L);
        book.setStatus(0);
        when(bookMapper.selectById(5L)).thenReturn(book);

        MessageDTO dto = new MessageDTO();
        dto.setBookId(5L);
        dto.setContent("还在吗");

        BizException ex = assertThrows(BizException.class, () -> messageService.create(2L, dto));
        assertEquals(409, ex.getCode());
    }

    @Test
    void testCreateThrows400WhenMessagingOwnBook() {
        Book book = new Book();
        book.setId(5L);
        book.setSellerId(1L);
        book.setStatus(1);
        when(bookMapper.selectById(5L)).thenReturn(book);

        MessageDTO dto = new MessageDTO();
        dto.setBookId(5L);
        dto.setContent("自顶");

        BizException ex = assertThrows(BizException.class, () -> messageService.create(1L, dto));
        assertEquals(400, ex.getCode());
    }

    @Test
    void testDeleteValidatesSenderOwnership() {
        Message msg = new Message();
        msg.setId(8L);
        msg.setSenderId(2L);
        when(messageMapper.selectById(8L)).thenReturn(msg);
        when(messageMapper.selectById(99L)).thenReturn(null);

        assertEquals(404, assertThrows(BizException.class, () -> messageService.delete(2L, 99L)).getCode());
        assertEquals(403, assertThrows(BizException.class, () -> messageService.delete(3L, 8L)).getCode());
        verify(messageMapper, never()).deleteById(8L);

        messageService.delete(2L, 8L);
        verify(messageMapper).deleteById(8L);
    }

    @Test
    void testListByBookAndReceivedFillSenderNickname() {
        Message m = new Message();
        m.setId(1L);
        m.setSenderId(3L);
        User sender = new User();
        sender.setId(3L);
        sender.setNickname("陈同学");

        when(messageMapper.selectList(any())).thenReturn(List.of(m));
        when(userMapper.selectBatchIds(any())).thenReturn(List.of(sender));

        assertEquals("陈同学", messageService.listByBook(5L).get(0).getSenderNickname());
        assertEquals("陈同学", messageService.received(1L).get(0).getSenderNickname());
    }

    @Test
    void testUnreadCountAndMarkAllRead() {
        when(messageMapper.selectCount(any())).thenReturn(3L);
        assertEquals(3L, messageService.unreadCount(1L));

        messageService.markAllRead(1L);
        verify(messageMapper).update(isNull(), any());
    }
}