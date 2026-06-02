package com.kanban.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CardRequest {
    @NotBlank(message = "卡片标题不能为空")
    @Size(max = 255, message = "标题最长255字符")
    private String title;

    private String descriptionMd;
    private String priority;
    private LocalDateTime dueDate;
    private Long assigneeId;
}
