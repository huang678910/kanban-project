package com.kanban.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("board_list")
public class BoardList {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long boardId;
    private String name;
    private String status;  // TODO, IN_PROGRESS, DONE, CLOSED
    private String priority;  // HIGH, MEDIUM, LOW
    private Double position;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
