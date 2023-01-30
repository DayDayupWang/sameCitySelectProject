package com.example.sameCity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.sameCity.dto.SetmealDto;
import com.example.sameCity.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    /**
     * 新增套餐，同时需要保存套餐和菜品的关联关系
     * SetmealDto setmealDto
     */
    public void saveWithDish(SetmealDto setmealDto);

    public SetmealDto getByIdInSetmeal(Long id);

    /**
     * 删除套餐，同时需要删除套餐和菜品的关联数据
     * @param ids
     */
    public void removeWithDish(List<Long> ids);
}
