package com.kanban.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("attachment")
public class Attachment {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long cardId;
    private Long userId;
    private String filename;
    private String filePath;
    private Long fileSize;
    private String mimeType;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime uploadedAt;
}
