package com.kanban.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kanban.dto.ApiResponse;
import com.kanban.dto.request.LabelRequest;
import com.kanban.entity.BoardList;
import com.kanban.entity.Card;
import com.kanban.entity.CardLabel;
import com.kanban.exception.BusinessException;
import com.kanban.mapper.BoardListMapper;
import com.kanban.mapper.CardLabelMapper;
import com.kanban.mapper.CardMapper;
import com.kanban.security.SecurityUtils;
import com.kanban.service.BoardService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class LabelController {

    private final CardLabelMapper cardLabelMapper;
    private final CardMapper cardMapper;
    private final BoardListMapper boardListMapper;
    private final BoardService boardService;

    public LabelController(CardLabelMapper cardLabelMapper, CardMapper cardMapper,
                           BoardListMapper boardListMapper, BoardService boardService) {
        this.cardLabelMapper = cardLabelMapper;
        this.cardMapper = cardMapper;
        this.boardListMapper = boardListMapper;
        this.boardService = boardService;
    }

    @GetMapping("/api/cards/{cardId}/labels")
    public ApiResponse<?> getLabels(@PathVariable Long cardId) {
        Card card = cardMapper.selectById(cardId);
        if (card == null) throw new BusinessException(404, "卡片不存在");

        List<CardLabel> labels = cardLabelMapper.selectList(
                new LambdaQueryWrapper<CardLabel>().eq(CardLabel::getCardId, cardId));
        return ApiResponse.success(labels);
    }

    @PostMapping("/api/cards/{cardId}/labels")
    public ApiResponse<?> addLabel(@PathVariable Long cardId, @Valid @RequestBody LabelRequest request) {
        Card card = cardMapper.selectById(cardId);
        if (card == null) throw new BusinessException(404, "卡片不存在");
        BoardList boardList = boardListMapper.selectById(card.getListId());
        boardService.checkPermission(boardList.getBoardId(), "ADMIN", "MEMBER");

        CardLabel label = new CardLabel();
        label.setCardId(cardId);
        label.setName(request.getName());
        label.setColor(request.getColor());
        cardLabelMapper.insert(label);
        return ApiResponse.success("标签已添加", label);
    }

    @DeleteMapping("/api/card-labels/{labelId}")
    public ApiResponse<?> removeLabel(@PathVariable Long labelId) {
        CardLabel label = cardLabelMapper.selectById(labelId);
        if (label == null) throw new BusinessException(404, "标签不存在");
        // Verify permission via the card's board
        Card card = cardMapper.selectById(label.getCardId());
        if (card != null) {
            BoardList boardList = boardListMapper.selectById(card.getListId());
            boardService.checkPermission(boardList.getBoardId(), "ADMIN", "MEMBER");
        }
        cardLabelMapper.deleteById(labelId);
        return ApiResponse.success("标签已删除", null);
    }
}
