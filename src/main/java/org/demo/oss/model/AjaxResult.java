package org.demo.oss.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用请求消息结果封装类
 */
@Data
public class AjaxResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 返回消息
     */
    private String msg;

    /**
     * 返回数据
     */
    private T data;

    public AjaxResult(Integer code,String msg){
        this.code = code;
        this.msg = msg;
    }

    public AjaxResult(Integer code,String msg,  T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    /**
     * 返回成功消息
     * @param msg 返回内容
     * @return 成功消息
     */
    public static <T> AjaxResult<T> success(String msg){
        return new  AjaxResult<>(200,msg);
    }

    /**
     * 返回成功消息
     *
     * @param data 数据对象
     * @return 成功消息
     */
    public static <T> AjaxResult<T> data(T data){
        return AjaxResult.data("操作成功",data);
    }

    /**
     * 返回成功消息
     *
     * @param msg 返回内容
     * @param data 数据对象
     * @return 成功消息
     */
    public static <T> AjaxResult<T> data(String msg, T data){
        return AjaxResult.data(200,msg,data);
    }

    /**
     * 返回成功消息
     *
     * @param msg 返回内容
     * @param data 数据对象
     * @return 成功消息
     */
    public static <T> AjaxResult<T> data(Integer code,String msg, T data){
        return new AjaxResult<>(code,data == null ? "暂无承载数据" : msg,data);
    }

    /**
     * 返回错误消息
     *
     * @param msg 返回内容
     * @return 错误消息
     */
    public static <T> AjaxResult<T> error(Integer code,String msg){
        return new AjaxResult<>(code,msg);
    }

    /**
     * 返回错误消息
     *
     * @param msg 返回内容
     * @return 错误消息
     */
    public static <T> AjaxResult<T> error(String msg){
        return AjaxResult.error(400,msg);
    }

    /**
     * 返回成功或者失败消息的通用方法
     *
     * @param flag 返回内容
     * @return 成功与失败状态消息
     */
    public static <T> AjaxResult<T> status(Boolean flag,String success,String error){
        return flag? AjaxResult.success(success) : AjaxResult.error(error);
    }
}
