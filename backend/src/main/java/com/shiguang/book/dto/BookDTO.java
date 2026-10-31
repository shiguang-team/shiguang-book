package com.shiguang.book.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BookDTO {

    @NotBlank(message = "书名不能为空")
    @Size(max = 60, message = "书名最多 60 个字")
    private String title;

    @NotBlank(message = "请选择分类")
    private String category;

    @NotNull(message = "请选择成色")
    @Min(value = 1, message = "成色最小 1")
    @Max(value = 10, message = "成色最大 10")
    private Integer conditionLevel;

    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格不能低于 0.01 元")
    @DecimalMax(value = "99999", message = "价格不能高于 99999 元")
    private BigDecimal price;

    @Size(max = 500, message = "描述最多 500 个字")
    private String description;

    private String cover;
}
