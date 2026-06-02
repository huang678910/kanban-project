package com.kanban.service;

import com.kanban.dto.request.BoardRequest;
import com.kanban.entity.Board;
import com.kanban.entity.BoardMember;
import com.kanban.exception.BusinessException;
import com.kanban.mapper.BoardMapper;
import com.kanban.mapper.BoardMemberMapper;
import com.kanban.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BoardService {

    private final BoardMapper boardMapper;
    private final BoardMemberMapper boardMemberMapper;
    private final ActivityLogService activityLogService;

    public BoardService(BoardMapper boardMapper, BoardMemberMapper boardMemberMapper,
                        ActivityLogService activityLogService) {
        this.boardMapper = boardMapper;
        this.boardMemberMapper = boardMemberMapper;
        this.activityLogService = activityLogService;
    }

    public List<Board> getMyBoards() {
        Long userId = SecurityUtils.getCurrentUserId();
        return boardMapper.findBoardsByUserId(userId);
    }

    public Board getBoard(Long boardId) {
        Board board = boardMapper.selectById(boardId);
        if (board == null) {
            throw new BusinessException(404, "看板不存在");
        }
        return board;
    }

    @Transactional
    public Board createBoard(BoardRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        Board board = new Board();
        board.setName(request.getName());
        board.setDescription(request.getDescription());
        board.setStatus("TODO");
        board.setOwnerId(userId);
        boardMapper.insert(board);

        // Add creator as ADMIN
        BoardMember member = new BoardMember();
        member.setBoardId(board.getId());
        member.setUserId(userId);
        member.setRole("ADMIN");
        member.setJoinedAt(LocalDateTime.now());
        boardMemberMapper.insert(member);

        activityLogService.log(board.getId(), userId, "CREATE_BOARD",
                "BOARD", board.getId(), "创建了看板 \"" + board.getName() + "\"");

        return board;
    }

    @Transactional
    public Board updateBoard(Long boardId, BoardRequest request) {
        Board board = getBoard(boardId);
        checkPermission(boardId, "ADMIN");
        board.setName(request.getName());
        board.setDescription(request.getDescription());
        boardMapper.updateById(board);

        activityLogService.log(boardId, SecurityUtils.getCurrentUserId(),
                "UPDATE_BOARD", "BOARD", boardId, "更新了看板信息");
        return board;
    }

    @Transactional
    public Board updateBoardStatus(Long boardId, String status) {
        Board board = getBoard(boardId);
        checkPermission(boardId, "ADMIN");
        board.setStatus(status);
        boardMapper.updateById(board);
        return board;
    }

    @Transactional
    public void deleteBoard(Long boardId) {
        Board board = getBoard(boardId);
        checkAdmin(board);
        boardMapper.deleteById(boardId);
    }

    public void checkAdmin(Board board) {
        Long userId = SecurityUtils.getCurrentUserId();
        BoardMember member = boardMemberMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<BoardMember>()
                        .eq(BoardMember::getBoardId, board.getId())
                        .eq(BoardMember::getUserId, userId));
        if (member == null || !"ADMIN".equals(member.getRole())) {
            throw new BusinessException(403, "只有管理员才能执行此操作");
        }
    }

    public void checkPermission(Long boardId, String... roles) {
        Long userId = SecurityUtils.getCurrentUserId();
        BoardMember member = boardMemberMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<BoardMember>()
                        .eq(BoardMember::getBoardId, boardId)
                        .eq(BoardMember::getUserId, userId));
        if (member == null) {
            throw new BusinessException(403, "你不是该看板的成员");
        }
        String memberRole = member.getRole();
        for (String role : roles) {
            if (memberRole.equals(role)) return;
            // ADMIN inherits all permissions
            if ("ADMIN".equals(memberRole)) return;
        }
        throw new BusinessException(403, "权限不足");
    }
}
