package com.example.sameCity.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sameCity.entity.ShoppingCart;
import com.example.sameCity.mapper.ShoppingCartMapper;
import com.example.sameCity.service.ShoppingCartService;
import org.springframework.stereotype.Service;

@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {

}
