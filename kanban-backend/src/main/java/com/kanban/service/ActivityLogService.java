package com.kanban.service;

import com.kanban.entity.ActivityLog;
import com.kanban.mapper.ActivityLogMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActivityLogService {

    private final ActivityLogMapper activityLogMapper;

    public ActivityLogService(ActivityLogMapper activityLogMapper) {
        this.activityLogMapper = activityLogMapper;
    }

    public void log(Long boardId, Long userId, String action,
                    String targetType, Long targetId, String detail) {
        ActivityLog log = new ActivityLog();
        log.setBoardId(boardId);
        log.setUserId(userId);
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setDetail(detail);
        activityLogMapper.insert(log);
    }

    public List<ActivityLog> getBoardActivities(Long boardId) {
        return activityLogMapper.findByBoardIdOrderByTime(boardId);
    }
}
