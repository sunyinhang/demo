package com.haiercash.payplatform.pc.vipabc.service.impl;

import com.haiercash.payplatform.common.dao.VipAbcDao;
import com.haiercash.payplatform.pc.vipabc.service.VipAbcService;
import com.haiercash.spring.service.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/12/25.
 */
@Service
public class VipAbcServiceImpl extends BaseService implements VipAbcService {
    @Autowired
    private VipAbcDao vipAbcDao;

    public Map<String, Object> getIdCardInfo(Map<String, Object> map) {
        HashMap<String, Object> mapIdCard = new HashMap<>();
        String ordersn = map.get("ordersn") + "";//vipabc方订单号
        String idCard = vipAbcDao.selectIdCard(ordersn);
        mapIdCard.put("idCard", idCard);
        return success(mapIdCard);
    }
}
