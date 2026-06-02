package com.kanban.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kanban.entity.BoardList;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface BoardListMapper extends BaseMapper<BoardList> {

    @Select("SELECT * FROM board_list WHERE board_id = #{boardId} ORDER BY position ASC")
    List<BoardList> findByBoardIdOrderByPosition(Long boardId);
}
