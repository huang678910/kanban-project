package com.kanban.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ListRequest {
    @NotBlank(message = "列表名称不能为空")
    @Size(max = 100, message = "列表名称最长100字符")
    private String name;
}
