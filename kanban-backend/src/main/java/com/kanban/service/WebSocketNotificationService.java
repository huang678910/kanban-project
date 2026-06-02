package com.kanban.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class WebSocketNotificationService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void notifyBoardChange(Long boardId, Map<String, Object> payload) {
        messagingTemplate.convertAndSend("/topic/board/" + boardId, payload);
    }

    public void notifyBoardActivity(Long boardId, Map<String, Object> payload) {
        messagingTemplate.convertAndSend("/topic/board/" + boardId + "/activities", payload);
    }

    public void notifyUser(Long userId, Map<String, Object> payload) {
        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/notifications",
                payload
        );
    }
}
