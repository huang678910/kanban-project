package com.kanban.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("board_member")
public class BoardMember {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long boardId;
    private Long userId;
    private String role;  // OWNER, EDITOR, VIEWER

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime joinedAt;
}
