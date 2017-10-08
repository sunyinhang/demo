package com.haiercash.payplatform.rest.cmis;

import com.haiercash.payplatform.config.EurekaServer;
import com.haiercash.payplatform.rest.AbstractRestUtil;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * Created by 许崇雷 on 2017-10-08.
 */
@Component
public class CmisUtil extends AbstractRestUtil<CmisResponse> {
    private static CmisUtil cmisUtil;

    @PostConstruct
    public void init() {
        cmisUtil = this;
    }

    @Override
    protected String instanceUrl() {
        return EurekaServer.CMISFRONTSERVER;
    }

    @Override
    protected CmisResponse createResponse(String retFlag, String retMsg) {
        return CmisResponse.create(retFlag, retMsg);
    }

    public static CmisResponse get(String api) {
        return cmisUtil.getCore(api, null, null);
    }

    public static CmisResponse get(String api, Map<String, ?> uriVariables) {
        return cmisUtil.getCore(api, uriVariables, null);
    }

    public static CmisResponse get(String api, Map<String, ?> uriVariables, MultiValueMap<String, String> headers) {
        return cmisUtil.getCore(api, uriVariables, headers);
    }

    public static CmisResponse delete(String api) {
        return cmisUtil.deleteCore(api, null, null);
    }

    public static CmisResponse delete(String api, Map<String, ?> uriVariables) {
        return cmisUtil.deleteCore(api, uriVariables, null);
    }

    public static CmisResponse delete(String api, Map<String, ?> uriVariables, MultiValueMap<String, String> headers) {
        return cmisUtil.deleteCore(api, uriVariables, headers);
    }

    public static CmisResponse post(String api, Object request) {
        return cmisUtil.postCore(api, request, null);
    }

    public static CmisResponse post(String api, Object request, MultiValueMap<String, String> headers) {
        return cmisUtil.postCore(api, request, headers);
    }

    public static CmisResponse put(String api, Object request) {
        return cmisUtil.putCore(api, request, null);
    }

    public static CmisResponse put(String api, Object request, MultiValueMap<String, String> headers) {
        return cmisUtil.putCore(api, request, headers);
    }
}
