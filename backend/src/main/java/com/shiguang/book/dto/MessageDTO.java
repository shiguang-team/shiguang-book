package com.shiguang.book.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MessageDTO {

    @NotNull(message = "书籍 id 不能为空")
    private Long bookId;

    @NotBlank(message = "留言内容不能为空")
    @Size(max = 200, message = "留言最多 200 个字")
    private String content;
}
