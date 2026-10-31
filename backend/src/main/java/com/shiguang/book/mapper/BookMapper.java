package com.shiguang.book.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shiguang.book.entity.Book;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BookMapper extends BaseMapper<Book> {
}
