package com.example.sameCity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.sameCity.entity.Orders;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Orders> {

}