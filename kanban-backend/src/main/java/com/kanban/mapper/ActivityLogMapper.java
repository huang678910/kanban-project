package com.kanban.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kanban.entity.ActivityLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface ActivityLogMapper extends BaseMapper<ActivityLog> {

    @Select("SELECT * FROM activity_log WHERE board_id = #{boardId} ORDER BY created_at DESC")
    List<ActivityLog> findByBoardIdOrderByTime(Long boardId);
}
