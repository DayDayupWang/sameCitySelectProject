package com.example.sameCity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sameCity.common.CustomException;
import com.example.sameCity.dto.DishDto;
import com.example.sameCity.entity.Dish;
import com.example.sameCity.entity.DishFlavor;
import com.example.sameCity.mapper.DishMapper;
import com.example.sameCity.service.DishFlavorService;
import com.example.sameCity.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {


    @Autowired
    private DishFlavorService dishFlavorService;

    /**
     * 新增菜品，同时保存对应的口味数据
     *
     * @param dishDto
     */
    @Override
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        //保存菜品的基本信息到菜品表dish
        this.save(dishDto);

        Long dishId = dishDto.getId();
        List<DishFlavor> flavors = dishDto.getFlavors();
        //通过lambda表达式对
        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());
        //保存菜品口味数据到菜品口味表dish_flavor
        //用saveBatch来保存口味集合
        dishFlavorService.saveBatch(flavors);
    }


    @Override
    public DishDto getByIdWithFlavor(Long id) {
        //1查询菜品基本信息
        Dish dish = this.getById(id);
        //1.5将已经查到的菜品信息拷贝到新创建的dishdto对象中
        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish, dishDto);
        //2根据条件查询菜品对应的口味
//        1. 其中DishFlavor::getDishId的意思就相当于：
//        1.1 实例化一个DishFlavor对象
//        DishFlavor dishFlavor = new DishFlavor;
//        1.2 调用对象DishFlavor的get方法，这里调用的是getDishId:
//        2.eq方法相当于赋值“=”
//        即将DishId的值为参数id，注意此时使用的是get方法而不是set方法
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dish.getId());
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);
        dishDto.setFlavors(flavors);
        return dishDto;
    }

    @Override
    @Transactional //加入事务注解保证事务的一致性
    public void updateWithFlavor(DishDto dishDto) {
        //更新dish表信息
        this.updateById(dishDto);
        //清除当前菜品对应口味数据 delete操作
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        //相当于delete from dish_flavor where dish_id=?
        queryWrapper.eq(DishFlavor::getDishId, dishDto.getId());

        dishFlavorService.remove(queryWrapper);

        //添加提交过来的口味数据 insert操作
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());
        dishFlavorService.saveBatch(flavors);
    }

    /**
     * 删除菜品以及关联口味
     *
     * @param ids
     */
    @Override
    @Transactional
    public void removeDishWithFlavor(List<Long> ids) {
        // 查菜品的状态，是否可以删除（）
//        select count(*) from Dish where id in (1,2,3) and status=1
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Dish::getId, ids);
        queryWrapper.eq(Dish::getStatus, 1);
        int count = this.count(queryWrapper);
        if (count > 0) {
//            如果不能删除，抛出一个业务异常
            throw new CustomException("菜品正在售卖中，不能删除");
        }

//        如果可以删除
        this.removeByIds(ids);
//        删除关系表中的数据---Dish_Flavor
//        delete from Dish_Flavor where Dish_id in (1,2,3)
        // 删除dish_flavor
        LambdaQueryWrapper<DishFlavor> dfQueryWrapper = new LambdaQueryWrapper<>();
        dfQueryWrapper.in(DishFlavor::getDishId, ids);

        dishFlavorService.remove(dfQueryWrapper);
    }


}
