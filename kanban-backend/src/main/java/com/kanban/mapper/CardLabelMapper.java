package com.kanban.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kanban.entity.CardLabel;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CardLabelMapper extends BaseMapper<CardLabel> {
}
