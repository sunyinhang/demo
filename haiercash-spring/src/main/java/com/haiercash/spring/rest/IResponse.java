package com.haiercash.spring.rest;

import com.haiercash.spring.rest.common.CommonResponse;
import com.haiercash.spring.rest.common.CommonResponseHead;
import com.haiercash.spring.util.BusinessException;
import com.haiercash.spring.util.ConstUtil;

import java.lang.reflect.Type;
import java.util.Objects;

/**
 * Created by 许崇雷 on 2017-10-08.
 */
public interface IResponse<TBody> {
    String getSerNo();

    String getRetFlag();

    String getRetMsg();

    Object getHead();

    TBody getBody();

    default IResponse<TBody> afterPropertiesSet(Type bodyType) {
        return this;
    }

    default IResponse<TBody> toCommonResponse() {
        if (this instanceof CommonResponse)
            return this;
        CommonResponse<TBody> response = CommonResponse.fail(this.getRetFlag(), this.getRetMsg());
        CommonResponseHead head = response.getHead();
        head.setSerno(this.getSerNo());
        response.setBody(this.getBody());
        return response;
    }

    default boolean isSuccess(boolean needBody) {
        String retFlag = this.getRetFlag();
        boolean retFlagOK = Objects.equals(retFlag, ConstUtil.SUCCESS_CODE) || Objects.equals(retFlag, ConstUtil.SUCCESS_CODE2);
        return needBody ? retFlagOK && this.getBody() != null : retFlagOK;
    }

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
