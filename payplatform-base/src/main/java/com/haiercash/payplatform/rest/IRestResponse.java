package com.haiercash.payplatform.rest;

/**
 * Created by 许崇雷 on 2017-10-08.
 */
public interface IRestResponse<THead, TBody> {
    boolean isSuccess(boolean needBody);

    String getRetFlag();

    String getRetMsg();

    THead getHead();

    TBody getBody();
}
