package com.example.sameCity.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.sameCity.common.R;
import com.example.sameCity.dto.DishDto;
import com.example.sameCity.entity.Category;
import com.example.sameCity.entity.Dish;
import com.example.sameCity.entity.DishFlavor;
import com.example.sameCity.service.CategoryService;
import com.example.sameCity.service.DishFlavorService;
import com.example.sameCity.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 菜品管理
 */
@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 新增菜品
     * DishDto dishDto
     * @return R<String>
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        log.info(dishDto.toString());
        dishService.saveWithFlavor(dishDto);
        return R.success("新增菜品成功");
    }

    /**
     * 菜品信息分页查询
     * int page
     * int pageSize
     * String name
     *
     * @return R<Page>
     */
    @GetMapping("/page")  //GET方法映射到/dish/page
    public R<Page> page(int page, int pageSize, String name) {
        log.info("page={},pageSize={},name={}", page, pageSize, name);

        //构造分页构造器
        Page<Dish> pageInfo = new Page<>(page, pageSize);
        //由于原
        Page<DishDto> dishDtoPage = new Page<>();
        //构造条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper();
//        //添加过滤条件
        queryWrapper.like(name != null, Dish::getName, name);
        //添加排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        //执行查询
        dishService.page(pageInfo, queryWrapper);
        //对象拷贝，拷贝除了records之外的其他属性
        BeanUtils.copyProperties(pageInfo, dishDtoPage, "records");
        List<Dish> records = pageInfo.getRecords();
        //.map把每个元素拿出来
        List<DishDto> list = records.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            //把各个item的普通属性以流的形式拷贝到dishDto上
            BeanUtils.copyProperties(item, dishDto);

            //得到单个的分类id
            Long categoryId = item.getCategoryId();
            //根据id查询数据库的分类对象
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                //得到分类菜品
                String categoryName = category.getName();
                //设置分类菜品
                dishDto.setCategoryName(categoryName);
            }


            return dishDto;
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(list);
        return R.success(pageInfo);
    }

    /**
     * 回显操作，点击行中修改根据id查询菜品信息和口味
     * Long id
     *
     * @return R<DishDto>
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id) {
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    /**
     * 修改菜品
     * @RequestBody DishDto dishDto
     * @return R<String>
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {
        log.info(dishDto.toString());
        dishService.updateWithFlavor(dishDto);
        //清理所有菜品的缓存数据
        //Set keys = redisTemplate.keys("dish_*");
        // redisTemplate.delete(keys);
        //动态清理所有菜品的缓存数据
        String key = "dish_" + dishDto.getCategoryId() + "_1";
        redisTemplate.delete(key);
        return R.success("修改菜品成功");
    }


    /**
     * 根据条件查询对应的菜品数据，追加了名称
     * Dish dish
     *
     * @return
     */
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish) {//Long categoryId也可以但为了增加复用性这里用dish


        List<DishDto> dishDtoList = null;
        //动态构造key
        String key = "dish_" + dish.getCategoryId() + "_" + dish.getStatus();
        //先从redis中获取缓存数据
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);
        if (dishDtoList != null) {
            //如果存在，直接返回，无需查询数据库
            return R.success(dishDtoList);
        }

        //如果不存在，需要查询数据库
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        //添加条件，查询状态为1起售的
        queryWrapper.eq(Dish::getStatus, 1);
        //添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> list = dishService.list(queryWrapper);

         dishDtoList = list.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            //把各个item的普通属性以流的形式拷贝到dishDto上
            BeanUtils.copyProperties(item, dishDto);

            //得到单个的分类id
            Long categoryId = item.getCategoryId();
            //根据id查询数据库的分类对象
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                //得到分类菜品
                String categoryName = category.getName();
                //设置分类菜品
                dishDto.setCategoryName(categoryName);
            }
            //菜品id
            Long dishId = item.getId();
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(DishFlavor::getDishId, dishId);
            //Sql:select * from dish_flavor where dish_id=?
            List<DishFlavor> dishFlavorList = dishFlavorService.list(lambdaQueryWrapper);
            dishDto.setFlavors(dishFlavorList);
            return dishDto;
        }).collect(Collectors.toList());

        //将查询到的菜品数据缓存到Redis
        redisTemplate.opsForValue().set(key, dishDtoList, 60, TimeUnit.MINUTES);
        return R.success(dishDtoList);
    }


    @PostMapping("/status/{status}")
    public R<String> changeSellStatus(@PathVariable int status, @RequestParam List<Long> ids) {
        //前端根据status的0或1来决定状态，改变status后前端会自动更改字段
//        方法1
        log.info(status + "-----------" + ids);
        LambdaUpdateWrapper<Dish> updateWrapper = new LambdaUpdateWrapper<>();
        //直接使用update的查询方法设置所有状态值
        updateWrapper.set(Dish::getStatus, status).in(ids != null, Dish::getId, ids);
        dishService.update(updateWrapper);
        return R.success("更改状态成功");
    }

    /**
     * 删除菜品
     * List<Long> ids
     * @return
     *
     */
    @DeleteMapping
    public R<String> deleteDish(@RequestParam List<Long> ids) {
        //@RequestParam: 用于将请求参数区数据映射到功能处理方法的参数上
        log.info("ids:{}", ids);
        dishService.removeDishWithFlavor(ids);
        return R.success("删除菜品成功");

    }

}
