package com.kanban.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CardMoveRequest {
    @NotNull(message = "目标列表ID不能为空")
    private Long targetListId;

    @NotNull(message = "目标位置不能为空")
    private Double position;
}
