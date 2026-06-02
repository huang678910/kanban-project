package com.kanban.controller;

import com.kanban.dto.ApiResponse;
import com.kanban.dto.request.MemberRequest;
import com.kanban.entity.BoardMember;
import com.kanban.entity.User;
import com.kanban.mapper.UserMapper;
import com.kanban.service.BoardMemberService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class BoardMemberController {

    private final BoardMemberService boardMemberService;
    private final UserMapper userMapper;

    public BoardMemberController(BoardMemberService boardMemberService, UserMapper userMapper) {
        this.boardMemberService = boardMemberService;
        this.userMapper = userMapper;
    }

    @GetMapping("/api/boards/{boardId}/members")
    public ApiResponse<?> getMembers(@PathVariable Long boardId) {
        List<BoardMember> members = boardMemberService.getMembers(boardId);
        List<Map<String, Object>> enriched = new ArrayList<>();
        for (BoardMember m : members) {
            User user = userMapper.selectById(m.getUserId());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", m.getId());
            item.put("boardId", m.getBoardId());
            item.put("userId", m.getUserId());
            item.put("role", m.getRole());
            item.put("joinedAt", m.getJoinedAt());
            item.put("displayName", user != null ? user.getDisplayName() : "未知用户");
            item.put("username", user != null ? user.getUsername() : "unknown");
            enriched.add(item);
        }
        return ApiResponse.success(enriched);
    }

    @PostMapping("/api/boards/{boardId}/members")
    public ApiResponse<?> addMember(@PathVariable Long boardId, @Valid @RequestBody MemberRequest request) {
        return ApiResponse.success("添加成功", boardMemberService.addMember(boardId, request));
    }

    @PutMapping("/api/board-members/{memberId}/role")
    public ApiResponse<?> changeRole(@PathVariable Long memberId, @RequestBody MemberRequest request) {
        boardMemberService.changeRole(memberId, request.getRole());
        return ApiResponse.success("修改成功", null);
    }

    @DeleteMapping("/api/board-members/{memberId}")
    public ApiResponse<?> removeMember(@PathVariable Long memberId) {
        boardMemberService.removeMember(memberId);
        return ApiResponse.success("移除成功", null);
    }
}
