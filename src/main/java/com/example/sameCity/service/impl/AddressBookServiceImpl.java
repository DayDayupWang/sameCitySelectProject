package com.example.sameCity.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sameCity.entity.AddressBook;
import com.example.sameCity.mapper.AddressBookMapper;
import com.example.sameCity.service.AddressBookService;
import org.springframework.stereotype.Service;

@Service
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements AddressBookService {

}
