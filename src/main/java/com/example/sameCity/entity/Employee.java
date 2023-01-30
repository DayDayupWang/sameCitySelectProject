package com.example.sameCity.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
// @Data注解的主要作用是提高代码的简洁，
// 使用这个注解可以省去实体类中大量的get()、 set()、 toString()等方法。
@Data
public class Employee implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String username;

    private String name;

    private String password;

    private String phone;

    private String sex;

    private String idNumber; //身份证号码

    private Integer status;

    // 通过注解设置公共字段自动填充，
    // 在具体业务中对实体类进行赋值就可以不用对这些公共字段进行赋值，
    // 在执行插入或者更新时就能自动赋值并插入数据库。
    @TableField(fill = FieldFill.INSERT)  //插入时填充字段
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE) //插入和更新时填充字段
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT) //插入时填充字段
    private Long createUser;

    @TableField(fill = FieldFill.INSERT_UPDATE) //插入和更新时填充字段
    private Long updateUser;

}
