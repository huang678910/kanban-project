package com.kanban.controller;

import com.kanban.dto.ApiResponse;
import com.kanban.dto.request.ListRequest;
import com.kanban.service.BoardListService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
public class BoardListController {

    private final BoardListService boardListService;

    public BoardListController(BoardListService boardListService) {
        this.boardListService = boardListService;
    }

    @GetMapping("/api/boards/{boardId}/lists")
    public ApiResponse<?> getLists(@PathVariable Long boardId) {
        return ApiResponse.success(boardListService.getListsByBoard(boardId));
    }

    @PostMapping("/api/boards/{boardId}/lists")
    public ApiResponse<?> createList(@PathVariable Long boardId, @Valid @RequestBody ListRequest request) {
        return ApiResponse.success("创建成功", boardListService.createList(boardId, request));
    }

    @PutMapping("/api/lists/{listId}")
    public ApiResponse<?> updateList(@PathVariable Long listId, @Valid @RequestBody ListRequest request) {
        return ApiResponse.success("更新成功", boardListService.updateList(listId, request));
    }

    @PutMapping("/api/lists/{listId}/status")
    public ApiResponse<?> updateListStatus(@PathVariable Long listId, @RequestBody java.util.Map<String, String> body) {
        return ApiResponse.success("状态已更新", boardListService.updateListStatus(listId, body.get("status")));
    }

    @PutMapping("/api/lists/{listId}/priority")
    public ApiResponse<?> updateListPriority(@PathVariable Long listId, @RequestBody java.util.Map<String, String> body) {
        return ApiResponse.success("优先级已更新", boardListService.updateListPriority(listId, body.get("priority")));
    }

    @DeleteMapping("/api/lists/{listId}")
    public ApiResponse<?> deleteList(@PathVariable Long listId) {
        boardListService.deleteList(listId);
        return ApiResponse.success("删除成功", null);
    }
}
