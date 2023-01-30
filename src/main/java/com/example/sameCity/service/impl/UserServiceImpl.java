package com.example.sameCity.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sameCity.common.BaseContext;
import com.example.sameCity.entity.User;
import com.example.sameCity.mapper.UserMapper;
import com.example.sameCity.service.UserService;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper,User> implements UserService {

    // 移动端用户退出登录
    @Override
    public Boolean logout(HttpSession session) {
        Long userId = BaseContext.getCurrentId();
        User user = this.getById(userId);
        // 清除Session保存作用域中保存的数据
        session.removeAttribute("user");
        return true;
    }
}
