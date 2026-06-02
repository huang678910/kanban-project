package com.kanban.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kanban.dto.request.MemberRequest;
import com.kanban.entity.Board;
import com.kanban.entity.BoardMember;
import com.kanban.entity.User;
import com.kanban.exception.BusinessException;
import com.kanban.mapper.BoardMemberMapper;
import com.kanban.mapper.BoardMapper;
import com.kanban.mapper.UserMapper;
import com.kanban.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BoardMemberService {

    private final BoardMemberMapper boardMemberMapper;
    private final BoardMapper boardMapper;
    private final UserMapper userMapper;
    private final BoardService boardService;
    private final ActivityLogService activityLogService;

    public BoardMemberService(BoardMemberMapper boardMemberMapper, BoardMapper boardMapper,
                              UserMapper userMapper, BoardService boardService,
                              ActivityLogService activityLogService) {
        this.boardMemberMapper = boardMemberMapper;
        this.boardMapper = boardMapper;
        this.userMapper = userMapper;
        this.boardService = boardService;
        this.activityLogService = activityLogService;
    }

    public List<BoardMember> getMembers(Long boardId) {
        boardService.checkPermission(boardId, "ADMIN", "MEMBER", "VIEWER");
        return boardMemberMapper.selectList(
                new LambdaQueryWrapper<BoardMember>().eq(BoardMember::getBoardId, boardId));
    }

    @Transactional
    public BoardMember addMember(Long boardId, MemberRequest request) {
        Board board = boardMapper.selectById(boardId);
        if (board == null) throw new BusinessException(404, "看板不存在");
        boardService.checkAdmin(board);  // Only admin can add members

        // Look up user by email
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getEmail, request.getEmail()));
        if (user == null) throw new BusinessException("未找到该邮箱对应的用户，请确认用户已注册");

        // Check not already a member
        Long count = boardMemberMapper.selectCount(
                new LambdaQueryWrapper<BoardMember>()
                        .eq(BoardMember::getBoardId, boardId)
                        .eq(BoardMember::getUserId, user.getId()));
        if (count > 0) throw new BusinessException("该用户已经是看板成员");

        // Cannot set as ADMIN (there can be only one owner)
        if ("ADMIN".equalsIgnoreCase(request.getRole())) {
            throw new BusinessException("不能将成员设置为管理员");
        }

        BoardMember member = new BoardMember();
        member.setBoardId(boardId);
        member.setUserId(user.getId());
        member.setRole(request.getRole().toUpperCase());
        boardMemberMapper.insert(member);

        activityLogService.log(boardId, SecurityUtils.getCurrentUserId(),
                "ADD_MEMBER", "MEMBER", member.getId(),
                "添加了成员 " + user.getUsername());

        return member;
    }

    @Transactional
    public void removeMember(Long memberId) {
        BoardMember member = boardMemberMapper.selectById(memberId);
        if (member == null) throw new BusinessException(404, "成员不存在");
        Board board = boardMapper.selectById(member.getBoardId());
        boardService.checkAdmin(board);

        // Cannot remove the admin
        if ("ADMIN".equals(member.getRole())) {
            throw new BusinessException("不能移除看板管理员");
        }

        activityLogService.log(member.getBoardId(), SecurityUtils.getCurrentUserId(),
                "REMOVE_MEMBER", "MEMBER", memberId, "移除了成员");
        boardMemberMapper.deleteById(memberId);
    }

    @Transactional
    public void changeRole(Long memberId, String newRole) {
        BoardMember member = boardMemberMapper.selectById(memberId);
        if (member == null) throw new BusinessException(404, "成员不存在");
        Board board = boardMapper.selectById(member.getBoardId());
        boardService.checkAdmin(board);

        if ("ADMIN".equalsIgnoreCase(newRole)) {
            throw new BusinessException("不能将其他成员设置为管理员");
        }

        member.setRole(newRole.toUpperCase());
        boardMemberMapper.updateById(member);

        activityLogService.log(member.getBoardId(), SecurityUtils.getCurrentUserId(),
                "CHANGE_ROLE", "MEMBER", memberId, "更改了成员角色为 " + newRole);
    }
}
