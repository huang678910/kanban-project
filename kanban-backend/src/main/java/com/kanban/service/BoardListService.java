package com.kanban.service;

import com.kanban.dto.request.ListRequest;
import com.kanban.entity.BoardList;
import com.kanban.exception.BusinessException;
import com.kanban.mapper.BoardListMapper;
import com.kanban.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BoardListService {

    private final BoardListMapper boardListMapper;
    private final BoardService boardService;
    private final ActivityLogService activityLogService;

    public BoardListService(BoardListMapper boardListMapper, BoardService boardService,
                            ActivityLogService activityLogService) {
        this.boardListMapper = boardListMapper;
        this.boardService = boardService;
        this.activityLogService = activityLogService;
    }

    public List<BoardList> getListsByBoard(Long boardId) {
        boardService.checkPermission(boardId, "ADMIN", "MEMBER", "VIEWER");
        return boardListMapper.findByBoardIdOrderByPosition(boardId);
    }

    @Transactional
    public BoardList createList(Long boardId, ListRequest request) {
        boardService.checkPermission(boardId, "ADMIN");  // Only admin

        BoardList boardList = new BoardList();
        boardList.setBoardId(boardId);
        boardList.setName(request.getName());
        boardList.setStatus("TODO");
        boardList.setPriority("MEDIUM");

        List<BoardList> existing = boardListMapper.findByBoardIdOrderByPosition(boardId);
        if (existing.isEmpty()) {
            boardList.setPosition(65536.0);
        } else {
            boardList.setPosition(existing.get(existing.size() - 1).getPosition() + 65536.0);
        }

        boardListMapper.insert(boardList);

        activityLogService.log(boardId, SecurityUtils.getCurrentUserId(),
                "CREATE_LIST", "LIST", boardList.getId(),
                "创建了列表 \"" + boardList.getName() + "\"");
        return boardList;
    }

    @Transactional
    public BoardList updateList(Long listId, ListRequest request) {
        BoardList boardList = boardListMapper.selectById(listId);
        if (boardList == null) throw new BusinessException(404, "列表不存在");
        boardService.checkPermission(boardList.getBoardId(), "ADMIN");

        boardList.setName(request.getName());
        boardListMapper.updateById(boardList);
        return boardList;
    }

    @Transactional
    public BoardList updateListStatus(Long listId, String status) {
        BoardList boardList = boardListMapper.selectById(listId);
        if (boardList == null) throw new BusinessException(404, "列表不存在");
        boardService.checkPermission(boardList.getBoardId(), "ADMIN", "MEMBER");

        boardList.setStatus(status);
        boardListMapper.updateById(boardList);
        return boardList;
    }

    @Transactional
    public BoardList updateListPriority(Long listId, String priority) {
        BoardList boardList = boardListMapper.selectById(listId);
        if (boardList == null) throw new BusinessException(404, "列表不存在");
        boardService.checkPermission(boardList.getBoardId(), "ADMIN");
        boardList.setPriority(priority);
        boardListMapper.updateById(boardList);
        return boardList;
    }

    @Transactional
    public void deleteList(Long listId) {
        BoardList boardList = boardListMapper.selectById(listId);
        if (boardList == null) throw new BusinessException(404, "列表不存在");
        boardService.checkPermission(boardList.getBoardId(), "ADMIN");  // Only admin

        activityLogService.log(boardList.getBoardId(), SecurityUtils.getCurrentUserId(),
                "DELETE_LIST", "LIST", listId, "删除了列表 \"" + boardList.getName() + "\"");
        boardListMapper.deleteById(listId);
    }
}
