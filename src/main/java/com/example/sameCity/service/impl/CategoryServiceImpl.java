package com.example.sameCity.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sameCity.common.CustomException;
import com.example.sameCity.entity.Category;
import com.example.sameCity.entity.Dish;
import com.example.sameCity.entity.Setmeal;
import com.example.sameCity.mapper.CategoryMapper;
import com.example.sameCity.service.CategoryService;
import com.example.sameCity.service.DishService;
import com.example.sameCity.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;

    /**
     * 根据id删除分类，删除之前需要进行判断
     * 由于删除需要的条件较多，所以不能直接用框架提供的 removeById
     * Long id
     */
    @Override
    public void remove(Long id) {
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //添加查询条件
        dishLambdaQueryWrapper.eq(Dish::getCategoryId, id);
        int count1 = dishService.count(dishLambdaQueryWrapper);

        //查询当前分类是否关联了菜品，如果已经关联，抛出一个业务异常
        if (count1 > 0) {
//            关联了菜品，抛出一个业务异常
            throw  new CustomException("当前分类下关联了菜品，不能删除");
        }

        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //添加查询条件
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId, id);
        int count2 = setmealService.count();
        if (count2 > 0) {
            //查询当前分类是否关联了套餐，如果已经关联，抛出一个业务异常
            throw  new CustomException("当前分类下关联了套餐，不能删除");
        }


        //既没有关联菜品又没有关联套餐，正常删除分类
        super.removeById(id);
    }
}
