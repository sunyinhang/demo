package com.haiercash.spring.rest;

import com.alibaba.fastjson.annotation.JSONField;
import com.haiercash.spring.utils.BusinessException;

/**
 * Created by 许崇雷 on 2017-10-08.
 */
public interface IResponse<TBody> {
    String getRetFlag();

    String getRetMsg();

    String getSerNo();

    Object getHead();

    TBody getBody();

    @JSONField(serialize = false, deserialize = false)
    boolean isSuccess(boolean needBody);

    @JSONField(serialize = false, deserialize = false)
    default boolean isSuccess() {
        return this.isSuccess(false);
    }

    @JSONField(serialize = false, deserialize = false)
    default boolean isSuccessNeedBody() {
        return this.isSuccess(true);
    }

    @JSONField(serialize = false, deserialize = false)
    default void assertSuccess(boolean needBody) {
        if (this.isSuccess(needBody))
            return;
        throw new BusinessException(this.getRetFlag(), this.getRetMsg());
    }

    @JSONField(serialize = false, deserialize = false)
    default void assertSuccess() {
        this.assertSuccess(false);
    }

    @JSONField(serialize = false, deserialize = false)
    default void assertSuccessNeedBody() {
        this.assertSuccess(true);
    }
}
