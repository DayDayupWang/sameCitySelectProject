package com.example.sameCity.dto;

import com.example.sameCity.entity.Setmeal;
import com.example.sameCity.entity.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
