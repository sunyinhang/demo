package com.haiercash.payplatform.service;

import java.util.Map;

/**
 * Created by use on 2017/8/29.
 */
public interface CrmManageService {
    //46、(GET)查询客户标签列表
    public Map<String,Object> getCustTag(String token, Map<String, Object> paramMap);
    //(GET)为指定客户增加指定的自定义标签
    public Map<String,Object> setCustTag(String token, Map<String, Object> paramMap);
}
