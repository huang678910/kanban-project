package com.kanban.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kanban.entity.BoardMember;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BoardMemberMapper extends BaseMapper<BoardMember> {
}
