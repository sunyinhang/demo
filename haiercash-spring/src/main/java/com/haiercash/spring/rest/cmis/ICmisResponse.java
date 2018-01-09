package com.haiercash.spring.rest.cmis;

import com.haiercash.spring.rest.IResponse;

import java.lang.reflect.Type;

/**
 * Created by 许崇雷 on 2018-01-09.
 */
public interface ICmisResponse<TBody> extends IResponse<TBody> {
    ICmisResponse<TBody> init(Type bodyType);
}
