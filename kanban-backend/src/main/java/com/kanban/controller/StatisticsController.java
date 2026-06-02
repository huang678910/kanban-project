package com.kanban.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kanban.dto.ApiResponse;
import com.kanban.entity.BoardList;
import com.kanban.entity.Card;
import com.kanban.mapper.BoardListMapper;
import com.kanban.mapper.CardMapper;
import com.kanban.service.BoardService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
public class StatisticsController {

    private final CardMapper cardMapper;
    private final BoardListMapper boardListMapper;
    private final BoardService boardService;

    public StatisticsController(CardMapper cardMapper, BoardListMapper boardListMapper,
                                BoardService boardService) {
        this.cardMapper = cardMapper;
        this.boardListMapper = boardListMapper;
        this.boardService = boardService;
    }

    @GetMapping("/api/boards/{boardId}/statistics")
    public ApiResponse<?> getStatistics(@PathVariable Long boardId) {
        boardService.checkPermission(boardId, "ADMIN", "MEMBER", "VIEWER");

        List<BoardList> lists = boardListMapper.findByBoardIdOrderByPosition(boardId);
        List<Map<String, Object>> listStats = new ArrayList<>();

        int totalCards = 0;
        int highPriority = 0;
        int overdue = 0;
        int done = 0;
        LocalDateTime now = LocalDateTime.now();

        for (BoardList list : lists) {
            List<Card> cards = cardMapper.findByListIdOrderByPosition(list.getId());
            int cardCount = cards.size();
            totalCards += cardCount;

            for (Card card : cards) {
                if ("HIGH".equals(card.getPriority())) highPriority++;
                if (card.getDueDate() != null && card.getDueDate().isBefore(now)) overdue++;
                if ("DONE".equals(card.getStatus())) done++;
            }

            Map<String, Object> stat = new HashMap<>();
            stat.put("listId", list.getId());
            stat.put("listName", list.getName());
            stat.put("cardCount", cardCount);
            listStats.add(stat);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalCards", totalCards);
        result.put("highPriority", highPriority);
        result.put("overdue", overdue);
        result.put("done", done);
        result.put("listStats", listStats);

        return ApiResponse.success(result);
    }
}
