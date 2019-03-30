package com.alibaba.otter.manager.biz.check.exception;

/**
 * @Description: 不能使用异常
 * @Author: tangdelong
 * @Date: 2018/6/22 16:04
 */
public class NotUseException extends RuntimeException{

    public NotUseException(String msg){
        super(msg);
    }

}
