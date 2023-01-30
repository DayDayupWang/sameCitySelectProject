package com.example.sameCity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.sameCity.entity.Category;

public interface CategoryService extends IService<Category> {
public  void remove(Long id);//定义了一个独特的remove方法来处理带有特别条件的删除情况
}
