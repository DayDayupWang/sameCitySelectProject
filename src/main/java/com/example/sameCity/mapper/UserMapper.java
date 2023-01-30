package com.example.sameCity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.sameCity.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
