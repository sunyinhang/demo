package com.haiercash.payplatform.pc.vipabc.service;

import java.util.Map;

/**
 * Created by Administrator on 2017/12/25.
 */
public interface VipAbcService {
    /**
     * 根据第三方订单号查询身份证号
     *
     * @param map
     * @return
     */

    Map<String, Object> getIdCardInfo(Map<String, Object> map);
}
