package com.kanban.controller;

import com.kanban.dto.ApiResponse;
import com.kanban.service.ActivityLogService;
import org.springframework.web.bind.annotation.*;

@RestController
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    public ActivityLogController(ActivityLogService activityLogService) {
        this.activityLogService = activityLogService;
    }

    @GetMapping("/api/boards/{boardId}/activities")
    public ApiResponse<?> getActivities(@PathVariable Long boardId) {
        return ApiResponse.success(activityLogService.getBoardActivities(boardId));
    }
}
