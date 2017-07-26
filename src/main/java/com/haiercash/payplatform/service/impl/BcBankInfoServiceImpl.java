package com.haiercash.payplatform.service.impl;

import com.haiercash.payplatform.dao.BcBankInfoDao;
import com.haiercash.payplatform.data.BcBankInfo;
import com.haiercash.payplatform.service.BcBankInfoService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Override
    public BcBankInfo selectById(String id) {

        return bcBankInfoDao.selectById("123");
    }
}
