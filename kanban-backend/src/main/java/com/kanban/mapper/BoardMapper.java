package com.kanban.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kanban.entity.Board;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface BoardMapper extends BaseMapper<Board> {

    @Select("SELECT DISTINCT b.* FROM board b " +
            "LEFT JOIN board_member bm ON b.id = bm.board_id " +
            "WHERE b.owner_id = #{userId} OR bm.user_id = #{userId} " +
            "ORDER BY b.updated_at DESC")
    List<Board> findBoardsByUserId(Long userId);
}
