package com.kanban.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BoardRequest {
    @NotBlank(message = "看板名称不能为空")
    @Size(max = 100, message = "看板名称最长100字符")
    private String name;

    @Size(max = 500, message = "描述最长500字符")
    private String description;
}
