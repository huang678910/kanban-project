package com.kanban.service;

import com.kanban.dto.request.CommentRequest;
import com.kanban.entity.BoardList;
import com.kanban.entity.Card;
import com.kanban.entity.Comment;
import com.kanban.exception.BusinessException;
import com.kanban.mapper.BoardListMapper;
import com.kanban.mapper.CardMapper;
import com.kanban.mapper.CommentMapper;
import com.kanban.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class CommentService {

    private final CommentMapper commentMapper;
    private final CardMapper cardMapper;
    private final BoardListMapper boardListMapper;
    private final BoardService boardService;
    private final ActivityLogService activityLogService;
    private final WebSocketNotificationService wsNotificationService;

    public CommentService(CommentMapper commentMapper, CardMapper cardMapper,
                          BoardListMapper boardListMapper, BoardService boardService,
                          ActivityLogService activityLogService,
                          WebSocketNotificationService wsNotificationService) {
        this.commentMapper = commentMapper;
        this.cardMapper = cardMapper;
        this.boardListMapper = boardListMapper;
        this.boardService = boardService;
        this.activityLogService = activityLogService;
        this.wsNotificationService = wsNotificationService;
    }

    public List<Comment> getComments(Long cardId) {
        return commentMapper.findByCardIdOrderByTime(cardId);
    }

    @Transactional
    public Comment addComment(Long cardId, CommentRequest request) {
        Card card = cardMapper.selectById(cardId);
        if (card == null) throw new BusinessException(404, "卡片不存在");
        BoardList boardList = boardListMapper.selectById(card.getListId());
        boardService.checkPermission(boardList.getBoardId(), "ADMIN", "MEMBER");

        Comment comment = new Comment();
        comment.setCardId(cardId);
        comment.setUserId(SecurityUtils.getCurrentUserId());
        comment.setContent(request.getContent());
        commentMapper.insert(comment);

        Long boardId = boardList.getBoardId();
        activityLogService.log(boardId, SecurityUtils.getCurrentUserId(),
                "ADD_COMMENT", "COMMENT", comment.getId(), "在卡片上添加了评论");

        wsNotificationService.notifyBoardChange(boardId, Map.of(
                "type", "COMMENT_ADDED", "cardId", cardId, "commentId", comment.getId()));

        return comment;
    }

    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) throw new BusinessException(404, "评论不存在");
        // Only the comment author can delete
        if (!comment.getUserId().equals(SecurityUtils.getCurrentUserId())) {
            throw new BusinessException(403, "只能删除自己的评论");
        }
        commentMapper.deleteById(commentId);
    }
}
