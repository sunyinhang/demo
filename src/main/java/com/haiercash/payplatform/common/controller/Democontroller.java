package com.haiercash.payplatform.common.controller;

import com.haiercash.commons.redis.Cache;
import com.haiercash.commons.rest.inner.InnerResponse;
import com.haiercash.commons.rest.inner.InnerRestUtil;
import com.haiercash.payplatform.common.annotation.RequestCheck;
import com.haiercash.payplatform.common.dao.BcBankInfoDao;
import com.haiercash.payplatform.common.data.BcBankInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * demo controller.
 * @author Liu qingxiang
 * @since v1.0.0
 */
@RestController
public class Democontroller extends BaseController{

    public Democontroller() {
        super("01");
    }

    @Autowired
    private InnerRestUtil innerRestUtil;

    @Autowired
    private BcBankInfoDao bcBankInfoDao;

    @Autowired
    private Cache cache;


    @RequestCheck
    @RequestMapping(value = "/app/payplatform/demo", method = RequestMethod.GET)
    public InnerResponse demo(String name, Model model) throws Exception {
        BcBankInfo bcBankInfo = bcBankInfoDao.selectById("468203");


        cache.set("XYZ", "12000");
        String value = cache.get("XYZ");

        return null;
    }


}
