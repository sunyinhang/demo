package com.haiercash.spring.rest.cmis.v2;

import com.haiercash.core.lang.BeanUtils;
import com.haiercash.core.lang.Convert;
import com.haiercash.spring.rest.cmis.ICmisResponse;
import com.haiercash.spring.trace.rest.ErrorHandler;
import com.haiercash.spring.util.ConstUtil;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Objects;

/**
 * Created by 许崇雷 on 2018-01-09.
 */
public final class CmisResponse2<TBody> extends HashMap<String, Object> implements ICmisResponse<TBody> {
    private TBody body;

    @Override
    public String getSerNo() {
        return Convert.toString(this.get("serno"));
    }

    @Override
    public String getRetFlag() {
        return Convert.toString(this.get("retFlag"));
    }

    @Override
    public String getRetMsg() {
        return Convert.toString(this.get("retMsg"));
    }

    @Override
    public Object getHead() {
        return this;
    }

    @Override
    public TBody getBody() {
        return this.body;
    }

    @Override
    public boolean isSuccess(boolean needBody) {
        String retFlag = this.getRetFlag();
        boolean retFlagOK = Objects.equals(retFlag, ConstUtil.SUCCESS_CODE) || Objects.equals(retFlag, ConstUtil.SUCCESS_CODE2);
        return needBody ? retFlagOK && this.getBody() != null : retFlagOK;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ICmisResponse<TBody> init(Type bodyType) {
        if (this.body == null) {
            this.put("retFlag", ErrorHandler.getRetFlag(this.getRetFlag()));
            this.body = (bodyType instanceof Class && ((Class<?>) bodyType).isAssignableFrom(HashMap.class))
                    ? (TBody) this
                    : BeanUtils.mapToBean(this, bodyType);
        }
        return this;
    }
}
