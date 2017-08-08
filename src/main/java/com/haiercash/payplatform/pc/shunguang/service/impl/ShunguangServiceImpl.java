package com.haiercash.payplatform.pc.shunguang.service.impl;

import com.haiercash.payplatform.pc.shunguang.service.ShunguangService;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.haiercash.payplatform.common.utils.RestUtil.success;

/**
 * Created by yuanli on 2017/8/7.
 */
@Service
public class ShunguangServiceImpl implements ShunguangService{

    @Override
    public Map<String, Object> edApply(Map<String, Object> map) {
        return success();
    }
}
