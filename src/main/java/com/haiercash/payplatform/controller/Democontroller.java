package com.haiercash.payplatform.controller;

import com.haiercash.commons.rest.inner.InnerResponse;
import com.haiercash.commons.rest.inner.InnerRestUtil;
import com.haiercash.payplatform.common.annotation.RequestCheck;
import com.haiercash.payplatform.dao.BcBankInfoDao;
import com.haiercash.payplatform.data.BcBankInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by Liu qingxiang on 2017/7/25.
 */
@Controller
public class Democontroller extends BaseInnerController{

    public Democontroller(String moduleNo) {
        super(moduleNo);
    }

    @Autowired
    private InnerRestUtil innerRestUtil;

    @Autowired
    private BcBankInfoDao bcBankInfoDao;


    @RequestCheck
    @RequestMapping(value = "/app/payplatform/demo", method = RequestMethod.GET)
    public InnerResponse demo(String name, Model model) {
        BcBankInfo bcBankInfo = bcBankInfoDao.selectById("234");

        return success();
    }


}
