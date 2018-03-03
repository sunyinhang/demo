package com.haiercash.spring.rest.cmis;

import com.bestvike.linq.exception.InvalidOperationException;
import com.haiercash.core.lang.Convert;
import com.haiercash.core.lang.StringUtils;
import com.haiercash.spring.eureka.EurekaServer;
import com.haiercash.spring.rest.cmis.v1.CmisResponse;
import com.haiercash.spring.rest.cmis.v2.CmisResponse2;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Type;

/**
 * Created by 许崇雷 on 2018-01-09.
 */
public enum CmisVersion {
    V1,
    V2;

    private static final int V1_TRADE_CODE_MIN = 100000;
    private static final int V2_TRADE_CODE_MIN = 100200;
    private static final int VN_TRADE_CODE_MAX = 200000;

    public static CmisVersion forTradeCode(String tradeCode) {
        if (StringUtils.isEmpty(tradeCode))
            throw new InvalidOperationException("traceCode can not be empty");
        Integer tradeCodeValue = Convert.nullInteger(tradeCode);
        if (tradeCodeValue == null)
            throw new InvalidOperationException("traceCode must be integer");
        else if (tradeCodeValue < V1_TRADE_CODE_MIN)
            throw new InvalidOperationException("traceCode must greater than " + V1_TRADE_CODE_MIN);
        else if (tradeCodeValue >= V1_TRADE_CODE_MIN && tradeCodeValue < V2_TRADE_CODE_MIN)
            return V1;
        else if (tradeCodeValue >= V2_TRADE_CODE_MIN && tradeCodeValue < VN_TRADE_CODE_MAX)
            return V2;
        else
            throw new InvalidOperationException("traceCode must less than " + VN_TRADE_CODE_MAX);
    }

    public String getUrl(ICmisRequest request) {
        switch (this) {
            case V1:
                return EurekaServer.CMISFRONTSERVER + "/pub/cmisfront";
            case V2:
                return EurekaServer.CMISINTERFACESERVER + "/api/cmis/interfaces/interface/" + request.getTradeCode() + "/";
            default:
                throw new InvalidOperationException("not supported cmis version");
        }
    }

    public Type getResponseType(Type bodyType) {
        switch (this) {
            case V1:
                return ParameterizedTypeImpl.make(CmisResponse.class, new Type[]{bodyType}, null);
            case V2:
                return ParameterizedTypeImpl.make(CmisResponse2.class, new Type[]{bodyType}, null);
            default:
                throw new InvalidOperationException("not supported cmis version");
        }
    }
}
