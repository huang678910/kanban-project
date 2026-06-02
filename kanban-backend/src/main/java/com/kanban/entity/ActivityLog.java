package com.kanban.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("activity_log")
public class ActivityLog {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long boardId;
    private Long userId;
    private String action;
    private String targetType;
    private Long targetId;
    private String detail;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
