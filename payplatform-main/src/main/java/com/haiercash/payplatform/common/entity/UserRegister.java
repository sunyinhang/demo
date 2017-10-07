package com.haiercash.payplatform.common.entity;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class UserRegister {
    private String userName;
    private String passWord;
    private String mobileNo;
    private String registTime;
}