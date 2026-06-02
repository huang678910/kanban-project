package com.kanban.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kanban.entity.Card;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.List;

@Mapper
public interface CardMapper extends BaseMapper<Card> {

    @Select("SELECT * FROM card WHERE list_id = #{listId} ORDER BY position ASC")
    List<Card> findByListIdOrderByPosition(Long listId);

    @Update("SET @pos := 0")
    void initPositionVariable();

    @Update("UPDATE card SET position = (@pos := @pos + 65536) " +
            "WHERE list_id = #{listId} ORDER BY position ASC, id ASC")
    void rebalancePositions(@Param("listId") Long listId);
}
