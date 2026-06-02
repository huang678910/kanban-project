package com.kanban.service;

import com.kanban.dto.request.CardMoveRequest;
import com.kanban.dto.request.CardRequest;
import com.kanban.entity.BoardList;
import com.kanban.entity.Card;
import com.kanban.exception.BusinessException;
import com.kanban.mapper.BoardListMapper;
import com.kanban.mapper.CardMapper;
import com.kanban.security.SecurityUtils;
import com.kanban.util.PositionUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class CardService {

    private final CardMapper cardMapper;
    private final BoardListMapper boardListMapper;
    private final BoardService boardService;
    private final ActivityLogService activityLogService;
    private final WebSocketNotificationService wsNotificationService;

    public CardService(CardMapper cardMapper, BoardListMapper boardListMapper,
                       BoardService boardService, ActivityLogService activityLogService,
                       WebSocketNotificationService wsNotificationService) {
        this.cardMapper = cardMapper;
        this.boardListMapper = boardListMapper;
        this.boardService = boardService;
        this.activityLogService = activityLogService;
        this.wsNotificationService = wsNotificationService;
    }

    public List<Card> getCardsByList(Long listId) {
        return cardMapper.findByListIdOrderByPosition(listId);
    }

    public Card getCard(Long cardId) {
        Card card = cardMapper.selectById(cardId);
        if (card == null) throw new BusinessException(404, "卡片不存在");
        return card;
    }

    @Transactional
    public Card createCard(Long listId, CardRequest request) {
        BoardList boardList = boardListMapper.selectById(listId);
        if (boardList == null) throw new BusinessException(404, "列表不存在");
        boardService.checkPermission(boardList.getBoardId(), "ADMIN", "MEMBER");

        Card card = new Card();
        card.setListId(listId);
        card.setTitle(request.getTitle());
        card.setDescriptionMd(request.getDescriptionMd());
        card.setStatus("TODO");
        card.setPriority(request.getPriority() != null ? request.getPriority() : "MEDIUM");
        card.setDueDate(request.getDueDate());
        card.setAssigneeId(request.getAssigneeId());

        // Calculate position at end of list
        List<Card> cards = cardMapper.findByListIdOrderByPosition(listId);
        if (cards.isEmpty()) {
            card.setPosition(PositionUtil.BASE_POSITION);
        } else {
            card.setPosition(cards.get(cards.size() - 1).getPosition() + PositionUtil.BASE_POSITION);
        }

        cardMapper.insert(card);

        Long boardId = boardList.getBoardId();
        activityLogService.log(boardId, SecurityUtils.getCurrentUserId(),
                "CREATE_CARD", "CARD", card.getId(),
                "创建了卡片 \"" + card.getTitle() + "\"");

        wsNotificationService.notifyBoardChange(boardId, Map.of(
                "type", "CARD_CREATED",
                "cardId", card.getId(),
                "listId", listId
        ));

        return card;
    }

    @Transactional
    public Card updateCard(Long cardId, CardRequest request) {
        Card card = getCard(cardId);
        BoardList boardList = boardListMapper.selectById(card.getListId());
        boardService.checkPermission(boardList.getBoardId(), "ADMIN", "MEMBER");

        card.setTitle(request.getTitle());
        card.setDescriptionMd(request.getDescriptionMd());
        card.setPriority(request.getPriority());
        card.setDueDate(request.getDueDate());
        card.setAssigneeId(request.getAssigneeId());
        cardMapper.updateById(card);

        activityLogService.log(boardList.getBoardId(), SecurityUtils.getCurrentUserId(),
                "UPDATE_CARD", "CARD", cardId, "更新了卡片 \"" + card.getTitle() + "\"");

        wsNotificationService.notifyBoardChange(boardList.getBoardId(), Map.of(
                "type", "CARD_UPDATED",
                "cardId", cardId
        ));

        return card;
    }

    @Transactional
    public Card updateCardStatus(Long cardId, String status) {
        Card card = getCard(cardId);
        BoardList boardList = boardListMapper.selectById(card.getListId());
        boardService.checkPermission(boardList.getBoardId(), "ADMIN", "MEMBER");

        card.setStatus(status);
        cardMapper.updateById(card);

        activityLogService.log(boardList.getBoardId(), SecurityUtils.getCurrentUserId(),
                "UPDATE_CARD", "CARD", cardId,
                "将卡片 \"" + card.getTitle() + "\" 状态改为 " + status);

        wsNotificationService.notifyBoardChange(boardList.getBoardId(), Map.of(
                "type", "CARD_UPDATED", "cardId", cardId));
        return card;
    }

    @Transactional
    public void deleteCard(Long cardId) {
        Card card = getCard(cardId);
        BoardList boardList = boardListMapper.selectById(card.getListId());
        boardService.checkPermission(boardList.getBoardId(), "ADMIN", "MEMBER");

        activityLogService.log(boardList.getBoardId(), SecurityUtils.getCurrentUserId(),
                "DELETE_CARD", "CARD", cardId, "删除了卡片 \"" + card.getTitle() + "\"");

        wsNotificationService.notifyBoardChange(boardList.getBoardId(), Map.of(
                "type", "CARD_DELETED",
                "cardId", cardId,
                "listId", card.getListId()
        ));

        cardMapper.deleteById(cardId);
    }

    @Transactional
    public Card moveCard(Long cardId, CardMoveRequest request) {
        Card card = getCard(cardId);
        Long sourceListId = card.getListId();
        Long targetListId = request.getTargetListId();

        BoardList targetList = boardListMapper.selectById(targetListId);
        if (targetList == null) throw new BusinessException(404, "目标列表不存在");
        boardService.checkPermission(targetList.getBoardId(), "ADMIN", "MEMBER");

        // Calculate new position using midpoint algorithm
        List<Card> targetCards = cardMapper.findByListIdOrderByPosition(targetListId);
        Double requestedPos = request.getPosition();
        double newPosition;

        // Find insertion point
        Card prevCard = null;
        Card nextCard = null;
        for (Card c : targetCards) {
            if (c.getId().equals(cardId)) continue;
            if (c.getPosition() <= requestedPos) {
                prevCard = c;
            }
            if (c.getPosition() > requestedPos && nextCard == null) {
                nextCard = c;
            }
        }

        newPosition = PositionUtil.calculatePosition(
                prevCard != null ? prevCard.getPosition() : null,
                nextCard != null ? nextCard.getPosition() : null
        );

        card.setListId(targetListId);
        card.setPosition(newPosition);
        cardMapper.updateById(card);

        Long boardId = targetList.getBoardId();
        String detail = sourceListId.equals(targetListId)
                ? "移动了卡片 \"" + card.getTitle() + "\" 的位置"
                : "将卡片 \"" + card.getTitle() + "\" 移动到列表 \"" + targetList.getName() + "\"";
        activityLogService.log(boardId, SecurityUtils.getCurrentUserId(),
                "MOVE_CARD", "CARD", cardId, detail);

        wsNotificationService.notifyBoardChange(boardId, Map.of(
                "type", "CARD_MOVED",
                "cardId", cardId,
                "sourceListId", sourceListId,
                "targetListId", targetListId,
                "position", newPosition
        ));

        return card;
    }
}
