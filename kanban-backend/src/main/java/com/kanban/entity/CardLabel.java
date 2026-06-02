package com.kanban.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("card_label")
public class CardLabel {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long cardId;
    private String name;
    private String color;  // Hex color like #FF5733
}
