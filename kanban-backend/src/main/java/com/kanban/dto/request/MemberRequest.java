package com.kanban.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MemberRequest {
    @NotBlank(message = "邮箱不能为空")
    private String email;  // Add member by email, not userId

    @NotBlank(message = "角色不能为空")
    private String role;  // ADMIN, MEMBER, or VIEWER
}
