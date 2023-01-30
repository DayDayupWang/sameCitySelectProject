package com.example.sameCity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.sameCity.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;
//mapper层的作用是对数据库进行数据持久化操作
@Mapper
public interface DishFlavorMapper extends BaseMapper<DishFlavor> {

}
