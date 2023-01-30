package com.example.sameCity.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.sameCity.common.R;
import com.example.sameCity.dto.SetmealDto;
import com.example.sameCity.entity.*;
import com.example.sameCity.service.CategoryService;
import com.example.sameCity.service.SetmealDishService;
import com.example.sameCity.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/setmeal")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private CategoryService categoryService;

    /**
     * 新增套餐
     *
     * @return
     * @RequestBody SetmealDto setmealDto
     */
    @PostMapping
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> save(@RequestBody SetmealDto setmealDto) {
        log.info("套餐信息：{}", setmealDto);
        setmealService.saveWithDish(setmealDto);
        return R.success("新增套餐成功");
    }


    /**
     * 根据条件查询对应的套餐数据，追加了名称
     * Dish dish
     * @return
     */
    @GetMapping("/list")
    @Cacheable(value = "setmealCache",key = "#setmeal.categoryId+'_'+#setmeal.status")
    public R<List<Setmeal>> list(Setmeal setmeal){//Long categoryId也可以但为了增加复用性这里用dish
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(setmeal.getCategoryId()!=null,Setmeal::getCategoryId,setmeal.getCategoryId());
        //添加条件，查询状态为1起售的
        queryWrapper.eq(Setmeal::getStatus,1);
        //添加排序条件
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> list = setmealService.list(queryWrapper);


        return R.success(list);
    }



    /**
     * 套餐信息分页查询
     * <p>
     * int page
     * int pageSize
     * String name
     *
     * @return R<Page>
     */
    @GetMapping("/page")  //GET方法映射到/employee/page
    public R<Page> page(int page, int pageSize, String name) {
        //log.info("page={},pageSize={},name={}", page, pageSize, name);
        //构造分页构造器
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);
        Page<SetmealDto> dtoPage = new Page<>();
        //构造条件构造器
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper();
//        //添加过滤条件（name不为空）
        queryWrapper.like(StringUtils.isNotEmpty(name), Setmeal::getName, name);
        //添加排序条件，根据更新时间降序排列
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        //执行查询
        setmealService.page(pageInfo, queryWrapper);

        //对象拷贝
        BeanUtils.copyProperties(pageInfo, dtoPage, "records");
        List<Setmeal> records = pageInfo.getRecords();
        List<SetmealDto> list = records.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item, setmealDto);
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }
            return setmealDto;
        }).collect(Collectors.toList());

        dtoPage.setRecords(list);
        return R.success(dtoPage);
    }

    /**
     * 回显操作，点击行中修改根据id查询套餐信息
     * Long id
     *
     * @return
     */
    //由于页面携带name，所以为了展示名称用dto
    @GetMapping("/{id}")
    public R<SetmealDto> getData(@PathVariable Long id) {
        SetmealDto setmealDto = setmealService.getByIdInSetmeal(id);
        return R.success(setmealDto);
    }


    /**
     * 修改套餐页面
     *
     * @param setmealDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto) {
        if (setmealDto == null) {
            return R.error("请求异常");
        }
        if (setmealDto.getSetmealDishes() == null) {
            return R.error("套餐没有菜品，请添加");
        }
        //套餐一般是一对多的状态
        //获取到关联的菜品列表，注意关联菜品的列表是我们提交过来的
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        //获取到套餐的id
        Long setmealId = setmealDto.getId();
        //构造关联菜品的条件查询
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        //根据套餐id在关联菜品中查询数据，注意这里所做的查询是在数据库中进行查询的
        queryWrapper.eq(SetmealDish::getSetmealId, setmealId);
        //关联菜品先移除掉原始数据库中的数据
        log.info("queryWrapper为：", queryWrapper);
        setmealDishService.remove(queryWrapper);
        //为setmeal_dish表填充相关的属性
        //这里我们需要为关联菜品的表前面的字段填充套餐的id
        for (SetmealDish setmealDish : setmealDishes) {
            setmealDish.setSetmealId(setmealId);//填充属性值
        }
        //批量把setmealDish保存到setmeal_dish表
        //这里我们保存了我们提交过来的关联菜品数据
        setmealDishService.saveBatch(setmealDishes);//保存套餐关联菜品
        //这里我们正常更新套餐
        setmealService.updateById(setmealDto);//保存套餐
        return R.success("套餐修改成功");

    }

    /**
     * 删除套餐
     * 清缓存时清理setmealCache缓存下的所有数据
     * @param ids
     * @return
     */
    @DeleteMapping
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> delete(@RequestParam List<Long> ids) {

        setmealService.removeWithDish(ids);
        return R.success("套餐数据删除成功");
    }

    /**
     * 停售套餐
     *
     * @param status
     * @param ids
     */
    @PostMapping("/status/{status}")
    public R<String> changeSellStatus(@PathVariable int status, @RequestParam List<Long> ids) {
        //前端是根据status的0或1来决定状态，改变status后前端会自动更改字段
//        方法1
//        log.info(status + "-----------" + ids);
//        LambdaUpdateWrapper<Setmeal> updateWrapper = new LambdaUpdateWrapper<>();
//        //直接使用update的查询方法设置所有状态值
//        updateWrapper.set(Setmeal::getStatus, status).in(ids != null，Setmeal::getId, ids);
//        setmealService.update(updateWrapper);
//        return R.success("更改状态成功");

//        方法2
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ids != null,Setmeal::getId, ids);
//        list 对应sql:SELECT * FROM setmeal WHERE (id IN (ids) )
        List<Setmeal> setmeals = setmealService.list(queryWrapper);
        log.info("setmeals目前是："+setmeals);
        setmeals= setmeals.stream().map((item) -> {
            item.setStatus(status);

            return item;
        }).collect(Collectors.toList());
        log.info("setmeals修改状态后是："+setmeals);
        setmealService.saveOrUpdateBatch(setmeals);//saveBatch是插入方法插入时存在
        return R.success("更改状态成功");

    }
}
