package com.shiguang.book.dto;

import lombok.Data;

import java.math.BigDecimal;

/** 首页列表 / 搜索的查询参数，全部可选 */
@Data
public class BookQuery {

    private Integer page = 1;
    private Integer size = 12;
    private String keyword;
    private String category;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    /** newest / priceAsc / priceDesc */
    private String sort = "newest";
    /** 1 只看在售（默认），0 全部 */
    private Integer onlyOnSale = 1;
}
