package com.haiercash.payplatform.rest;

/**
 * Created by 许崇雷 on 2017-10-08.
 */
public interface IRestResponse {
    boolean isSuccess(boolean needBody);

    String getRetFlag();

    String getRetMsg();
}
