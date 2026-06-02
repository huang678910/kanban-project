package com.kanban.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("card")
public class Card {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long listId;
    private String title;
    private String descriptionMd;
    private String status;    // TODO, IN_PROGRESS, DONE, CLOSED
    private String priority;  // HIGH, MEDIUM, LOW
    private LocalDateTime dueDate;
    private Double position;
    private Long assigneeId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
