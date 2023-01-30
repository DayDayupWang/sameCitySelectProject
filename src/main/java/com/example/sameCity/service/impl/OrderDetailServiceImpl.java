package com.example.sameCity.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sameCity.entity.OrderDetail;
import com.example.sameCity.mapper.OrderDetailMapper;
import com.example.sameCity.service.OrderDetailService;
import org.springframework.stereotype.Service;

@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements OrderDetailService {

}