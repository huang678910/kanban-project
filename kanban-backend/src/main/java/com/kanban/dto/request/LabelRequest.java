package com.kanban.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LabelRequest {
    @NotBlank(message = "标签名称不能为空")
    private String name;

    @NotBlank(message = "标签颜色不能为空")
    private String color;
}
