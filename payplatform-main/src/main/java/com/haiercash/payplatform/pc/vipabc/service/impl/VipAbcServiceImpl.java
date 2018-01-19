package com.haiercash.payplatform.pc.vipabc.service.impl;

import com.haiercash.core.collection.CollectionUtils;
import com.haiercash.core.lang.StringUtils;
import com.haiercash.payplatform.common.dao.VipAbcDao;
import com.haiercash.payplatform.common.data.VipAbcAppOrderGoods;
import com.haiercash.payplatform.pc.vipabc.service.VipAbcService;
import com.haiercash.spring.service.BaseService;
import com.haiercash.spring.util.ConstUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/12/25.
 */
@Service
public class VipAbcServiceImpl extends BaseService implements VipAbcService {
    @Autowired
    private VipAbcDao vipAbcDao;

    /**
     * 根据第三方订单号查询身份证号
     *
     * @param map
     * @return
     */
    @Override
    public Map<String, Object> getIdCardInfo(Map<String, Object> map) {
        logger.info("获取的参数为：" + map);
        HashMap<String, Object> mapIdCard = new HashMap<>();
        String ordersn = (String) map.get("ordersn");//vipabc方订单号
        String idcard = (String) map.get("idcard");
        List<VipAbcAppOrderGoods> idCards = vipAbcDao.selectIdCard(ordersn, idcard);
        if (CollectionUtils.isEmpty(idCards))
            mapIdCard.put("VipAbcAppOrderGoods", StringUtils.EMPTY);
        else if (idCards.size() > 1)
            return fail(ConstUtil.ERROR_CODE, "结果不唯一");
        else
            mapIdCard.put("VipAbcAppOrderGoods", idCards.get(0));
        return success(mapIdCard);
    }
}
