package com.bigo.tronserver.model;

import lombok.Data;

@Data
public class Result {
    private int code;
    private String msg;
    private Object data;
    public static Result success(Object data){
        Result result = new Result();
        result.setCode(0);
        result.setMsg("success");
        result.setData(data);
        return result;
    }
    public static Result success(){
        return Result.success(null);
    }
    public static Result fail(String msg){
        Result result = new Result();
        result.setCode(999999);
        result.setMsg(msg);
        return result;
    }
    public static Result fail(){
        return  Result.fail(null);
    }
}
