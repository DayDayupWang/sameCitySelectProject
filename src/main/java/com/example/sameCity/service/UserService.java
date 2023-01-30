package com.example.sameCity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.sameCity.entity.User;

import javax.servlet.http.HttpSession;

public interface UserService extends IService<User> {
    // 移动端用户退出登录
    Boolean logout(HttpSession session);
}
