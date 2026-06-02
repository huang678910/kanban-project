package com.kanban.controller;

import com.kanban.dto.ApiResponse;
import com.kanban.dto.request.CommentRequest;
import com.kanban.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/api/cards/{cardId}/comments")
    public ApiResponse<?> getComments(@PathVariable Long cardId) {
        return ApiResponse.success(commentService.getComments(cardId));
    }

    @PostMapping("/api/cards/{cardId}/comments")
    public ApiResponse<?> addComment(@PathVariable Long cardId, @Valid @RequestBody CommentRequest request) {
        return ApiResponse.success("添加成功", commentService.addComment(cardId, request));
    }

    @DeleteMapping("/api/comments/{commentId}")
    public ApiResponse<?> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ApiResponse.success("删除成功", null);
    }
}
