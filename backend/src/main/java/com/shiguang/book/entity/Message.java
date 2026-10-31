package com.shiguang.book.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("message")
public class Message {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long bookId;

    private Long senderId;

    /** 收件人 = 书籍的主人，冗余存储方便查「我收到的留言」 */
    private Long receiverId;

    private String content;

    private Integer isRead;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createdAt;

    @TableField(exist = false)
    private String senderNickname;
}
