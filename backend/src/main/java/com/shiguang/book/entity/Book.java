package com.shiguang.book.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("book")
public class Book {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    /** 教材 / 考研 / 课外 / 其他 */
    private String category;

    /** 成色 1-10（10 成新） */
    private Integer conditionLevel;

    private BigDecimal price;

    private String description;

    /** 封面图相对地址，如 /files/xxx.jpg */
    private String cover;

    private Long sellerId;

    /** 1 在售 0 已下架 */
    private Integer status;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /** 非表字段：详情/列表里回填卖家昵称 */
    @TableField(exist = false)
    private String sellerNickname;

    @TableField(exist = false)
    private String sellerDept;
}
