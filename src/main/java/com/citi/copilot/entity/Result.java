package com.citi.copilot.entity;

import lombok.Data;

@Data
public class Result<T> {
    private T data;
    private String message;
    private boolean status;
}
