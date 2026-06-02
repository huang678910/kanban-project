package com.kanban.controller;

import com.kanban.dto.ApiResponse;
import com.kanban.dto.request.BoardRequest;
import com.kanban.service.BoardService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/boards")
public class BoardController {

    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @GetMapping
    public ApiResponse<?> getMyBoards() {
        return ApiResponse.success(boardService.getMyBoards());
    }

    @GetMapping("/{boardId}")
    public ApiResponse<?> getBoard(@PathVariable Long boardId) {
        return ApiResponse.success(boardService.getBoard(boardId));
    }

    @PostMapping
    public ApiResponse<?> createBoard(@Valid @RequestBody BoardRequest request) {
        return ApiResponse.success("创建成功", boardService.createBoard(request));
    }

    @PutMapping("/{boardId}")
    public ApiResponse<?> updateBoard(@PathVariable Long boardId, @Valid @RequestBody BoardRequest request) {
        return ApiResponse.success("更新成功", boardService.updateBoard(boardId, request));
    }

    @PutMapping("/{boardId}/status")
    public ApiResponse<?> updateBoardStatus(@PathVariable Long boardId, @RequestBody java.util.Map<String, String> body) {
        return ApiResponse.success("状态已更新", boardService.updateBoardStatus(boardId, body.get("status")));
    }

    @DeleteMapping("/{boardId}")
    public ApiResponse<?> deleteBoard(@PathVariable Long boardId) {
        boardService.deleteBoard(boardId);
        return ApiResponse.success("删除成功", null);
    }
}
