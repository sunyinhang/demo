package com.haiercash.payplatform.rest;

import com.haiercash.payplatform.utils.BusinessException;

/**
 * Created by 许崇雷 on 2017-10-08.
 */
public interface IResponse<TBody> {
    boolean isSuccess(boolean needBody);

    String getRetFlag();

    String getRetMsg();

    String getSerNo();

    Object getHead();

    TBody getBody();

    default boolean isSuccess() {
        return this.isSuccess(false);
    }

    default boolean isSuccessNeedBody() {
        return this.isSuccess(true);
    }

    default void assertSuccess(boolean needBody) {
        if (this.isSuccess(needBody))
            return;
        throw new BusinessException(this.getRetFlag(), this.getRetMsg());
    }

    default void assertSuccess() {
        this.assertSuccess(false);
    }

    default void assertSuccessNeedBody() {
        this.assertSuccess(true);
    }
}
