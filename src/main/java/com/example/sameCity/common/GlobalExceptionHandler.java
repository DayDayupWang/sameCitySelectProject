package com.example.sameCity.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局的异常处理器
 */
//指明拦截哪些控制器 带有RestController和Controller的都会被拦截
@ControllerAdvice(annotations = {RestController.class, Controller.class})
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 异常处理方法，指定在sql出问题时抛出
     * @param ex
     * @return
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException ex) {
        log.error(ex.getMessage());


        if (ex.getMessage().contains("Duplicate entry")){
            //根据给出的错误信息Duplicate entry ‘zhangsan’ for key...，从中分割字符得到想要拼接的信息
            String[] split=ex.getMessage().split(" ");
            String msg = split[2] +"已存在";
           return R.error(msg);
        }
        return R.error("未知错误");
    }

    /**
     * 异常处理方法，指定在遇到自定义异常时抛出
     * @param ex
     * @return
     */
    @ExceptionHandler(CustomException.class)
    public R<String> exceptionHandler(CustomException ex) {
        log.error(ex.getMessage());

        return R.error("未知错误");
    }
}
