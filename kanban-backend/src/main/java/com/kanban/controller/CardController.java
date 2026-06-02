package com.kanban.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kanban.dto.ApiResponse;
import com.kanban.dto.request.CardMoveRequest;
import com.kanban.dto.request.CardRequest;
import com.kanban.entity.Card;
import com.kanban.entity.CardLabel;
import com.kanban.mapper.CardLabelMapper;
import com.kanban.service.CardService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.*;

@RestController
public class CardController {

    private final CardService cardService;
    private final CardLabelMapper cardLabelMapper;

    public CardController(CardService cardService, CardLabelMapper cardLabelMapper) {
        this.cardService = cardService;
        this.cardLabelMapper = cardLabelMapper;
    }

    @GetMapping("/api/lists/{listId}/cards")
    public ApiResponse<?> getCards(@PathVariable Long listId) {
        List<Card> cards = cardService.getCardsByList(listId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Card card : cards) {
            Map<String, Object> cardMap = new LinkedHashMap<>();
            cardMap.put("id", card.getId());
            cardMap.put("listId", card.getListId());
            cardMap.put("title", card.getTitle());
            cardMap.put("descriptionMd", card.getDescriptionMd());
            cardMap.put("status", card.getStatus());
            cardMap.put("priority", card.getPriority());
            cardMap.put("dueDate", card.getDueDate());
            cardMap.put("position", card.getPosition());
            cardMap.put("assigneeId", card.getAssigneeId());
            cardMap.put("createdAt", card.getCreatedAt());
            cardMap.put("updatedAt", card.getUpdatedAt());
            // Include labels
            List<CardLabel> labels = cardLabelMapper.selectList(
                    new LambdaQueryWrapper<CardLabel>().eq(CardLabel::getCardId, card.getId()));
            cardMap.put("labels", labels);
            result.add(cardMap);
        }
        return ApiResponse.success(result);
    }

    @PostMapping("/api/lists/{listId}/cards")
    public ApiResponse<?> createCard(@PathVariable Long listId, @Valid @RequestBody CardRequest request) {
        return ApiResponse.success("创建成功", cardService.createCard(listId, request));
    }

    @GetMapping("/api/cards/{cardId}")
    public ApiResponse<?> getCard(@PathVariable Long cardId) {
        return ApiResponse.success(cardService.getCard(cardId));
    }

    @PutMapping("/api/cards/{cardId}")
    public ApiResponse<?> updateCard(@PathVariable Long cardId, @Valid @RequestBody CardRequest request) {
        return ApiResponse.success("更新成功", cardService.updateCard(cardId, request));
    }

    @DeleteMapping("/api/cards/{cardId}")
    public ApiResponse<?> deleteCard(@PathVariable Long cardId) {
        cardService.deleteCard(cardId);
        return ApiResponse.success("删除成功", null);
    }

    @PutMapping("/api/cards/{cardId}/status")
    public ApiResponse<?> updateCardStatus(@PathVariable Long cardId, @RequestBody Map<String, String> body) {
        return ApiResponse.success("状态已更新", cardService.updateCardStatus(cardId, body.get("status")));
    }

    @PutMapping("/api/cards/{cardId}/move")
    public ApiResponse<?> moveCard(@PathVariable Long cardId, @Valid @RequestBody CardMoveRequest request) {
        return ApiResponse.success(cardService.moveCard(cardId, request));
    }
}
