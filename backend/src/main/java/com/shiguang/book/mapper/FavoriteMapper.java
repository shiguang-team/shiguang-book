package com.shiguang.book.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shiguang.book.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {
}
