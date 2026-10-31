package com.shiguang.book.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shiguang.book.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
