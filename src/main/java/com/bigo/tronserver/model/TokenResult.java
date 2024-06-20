package com.bigo.tronserver.model;

import lombok.Data;

@Data
public class TokenResult<T> {
    int code;
    String msg;
    String time;
    T data;
    String origin;
}
