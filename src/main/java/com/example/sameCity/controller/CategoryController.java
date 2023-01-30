package com.example.sameCity.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.sameCity.common.R;
import com.example.sameCity.entity.Category;
import com.example.sameCity.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * category
 */
@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;


    /**
     * 新增菜品
     * @RequestBody Category category
     * @return R.success
     */
    @PostMapping
    public R<String> save(@RequestBody Category category) {//为了匹配JSON数据，进行了封装
        log.info("新增菜品，菜品信息：{}", category.toString());

        categoryService.save(category);
        return R.success("新增菜品成功");
    }
    /**
     * 菜品信息分页查询
     *
     * int page
     * int pageSize
     * String name
     * @return R<Page>
     */
    @GetMapping("/page")  //GET方法映射到/employee/page
    public R<Page> page(int page, int pageSize, String name) {
        log.info("page={},pageSize={},name={}", page, pageSize, name);

        //构造分页构造器
        Page<Category> pageInfo = new Page<>(page, pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper();
//        //添加过滤条件
//        queryWrapper.like(StringUtils.isNotEmpty(name), Category::getName, name);
        //添加排序条件
        queryWrapper.orderByAsc(Category::getSort);
        //执行查询
        categoryService.page(pageInfo, queryWrapper);
        return R.success(pageInfo);
    }

    /**
     * 根据id删除分类
     * Long id
     * @return
     */
    @DeleteMapping
    public  R<String> delete(Long id){
        log.info("删除分类，id为{}", id);
        categoryService.removeById(id);//根据框架提供的方法就可以来写了
        return R.success("分类信息删除成功");
    }


    /**
     * 根据id修改分类信息
     * @RequestBody Category category
     * @return
     */
    @PutMapping
    public  R<String> update(@RequestBody Category category){
        //@RequestBody用于接收来自前端的信息
        log.info("修改分类信息:{}", category);
        categoryService.updateById(category);//根据框架提供的方法就可以来写了
        return R.success("修改分类信息成功");
    }

    /**
     * 根据条件查询分类数据
     * Category category
     * @return
     */
    @GetMapping("/list")
    public R<List<Category>> list(Category category){

        //条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        //添加条件
        queryWrapper.eq(category.getType()!=null,Category::getType,category.getType());
        //添加排序条件
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);

        List<Category> list = categoryService.list(queryWrapper);
        return  R.success(list);
    }

}
