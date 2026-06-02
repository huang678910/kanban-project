package com.kanban.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kanban.dto.ApiResponse;
import com.kanban.entity.Board;
import com.kanban.entity.BoardMember;
import com.kanban.entity.Card;
import com.kanban.mapper.BoardListMapper;
import com.kanban.mapper.BoardMapper;
import com.kanban.mapper.BoardMemberMapper;
import com.kanban.mapper.CardMapper;
import com.kanban.security.SecurityUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class SearchController {

    private final CardMapper cardMapper;
    private final BoardListMapper boardListMapper;
    private final BoardMemberMapper boardMemberMapper;
    private final BoardMapper boardMapper;

    public SearchController(CardMapper cardMapper, BoardListMapper boardListMapper,
                            BoardMemberMapper boardMemberMapper, BoardMapper boardMapper) {
        this.cardMapper = cardMapper;
        this.boardListMapper = boardListMapper;
        this.boardMemberMapper = boardMemberMapper;
        this.boardMapper = boardMapper;
    }

    @GetMapping("/api/search")
    public ApiResponse<?> search(@RequestParam String q) {
        if (q == null || q.trim().isEmpty()) {
            return ApiResponse.success(Collections.emptyList());
        }

        Long userId = SecurityUtils.getCurrentUserId();
        String keyword = q.trim();

        // Get all boards the current user is a member of
        List<BoardMember> myMemberships = boardMemberMapper.selectList(
                new LambdaQueryWrapper<BoardMember>().eq(BoardMember::getUserId, userId));
        Set<Long> myBoardIds = new HashSet<>();
        for (BoardMember bm : myMemberships) {
            myBoardIds.add(bm.getBoardId());
        }

        List<Map<String, Object>> results = new ArrayList<>();

        // 1. Search boards by name
        List<Board> matchingBoards = boardMapper.selectList(
                new LambdaQueryWrapper<Board>().like(Board::getName, keyword));
        for (Board board : matchingBoards) {
            if (myBoardIds.contains(board.getId())) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("type", "board");
                item.put("id", board.getId());
                item.put("title", board.getName());
                item.put("descriptionMd", board.getDescription());
                item.put("boardId", board.getId());
                results.add(item);
            }
        }

        // 2. Search cards by title or description
        List<Card> allCards = cardMapper.selectList(
                new LambdaQueryWrapper<Card>()
                        .like(Card::getTitle, keyword)
                        .or()
                        .like(Card::getDescriptionMd, keyword));

        for (Card card : allCards) {
            var boardList = boardListMapper.selectById(card.getListId());
            if (boardList == null) continue;

            Long boardId = boardList.getBoardId();
            if (!myBoardIds.contains(boardId)) continue;

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("type", "card");
            item.put("id", card.getId());
            item.put("title", card.getTitle());
            item.put("descriptionMd", card.getDescriptionMd());
            item.put("listId", card.getListId());
            item.put("boardId", boardId);
            item.put("listName", boardList.getName());
            results.add(item);
        }

        return ApiResponse.success(results);
    }
}
