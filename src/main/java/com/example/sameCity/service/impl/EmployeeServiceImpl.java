package com.example.sameCity.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sameCity.entity.Employee;
import com.example.sameCity.mapper.EmployeeMapper;
import com.example.sameCity.service.EmployeeService;
import org.springframework.stereotype.Service;

@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {


}
