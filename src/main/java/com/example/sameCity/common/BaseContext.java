package com.example.sameCity.common;


/**
 *基于ThreadLocal封装工具类，用户保存和获取当前登录用户id
 * 基于线程来保存对应的副本
 */
public class BaseContext {
    private static ThreadLocal<Long> threadLocal=new ThreadLocal<>();
    public static void setThreadLocal(Long id){
        threadLocal.set(id);
    }
    public static Long getCurrentId(){
        return threadLocal.get();
    }
}
