package com.haiercash.payplatform.service.impl;

import com.haiercash.commons.rest.EurekaRest;
import com.haiercash.payplatform.dao.BcBankInfoDao;
import com.haiercash.payplatform.data.AppOrder;
import com.haiercash.payplatform.data.BcBankInfo;
import com.haiercash.payplatform.service.BcBankInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * bc bank info service impl.
 * @author Liu qingxiang
 * @since v1.0.0
 */
@Service
public class BcBankInfoServiceImpl implements BcBankInfoService{

    @Autowired
    private BcBankInfoDao bcBankInfoDao;

    @Autowired
    private EurekaRest eurekaRest;

    @Override
    public BcBankInfo selectById(String id) {

        ResponseEntity<AppOrder> responseEntity = eurekaRest.exchange("CRM", "/app/crm/demo", HttpMethod.GET, null, AppOrder.class);
        AppOrder body = responseEntity.getBody();

        return bcBankInfoDao.selectById("123");
    }
}
