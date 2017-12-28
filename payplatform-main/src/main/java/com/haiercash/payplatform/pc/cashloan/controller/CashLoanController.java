package com.haiercash.payplatform.pc.cashloan.controller;

import com.haiercash.core.lang.StringUtils;
import com.haiercash.payplatform.pc.cashloan.service.CashLoanService;
import com.haiercash.spring.controller.BaseController;
import com.haiercash.spring.util.ConstUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Created by 许崇雷 on 2017-10-10.
 */
@RestController
public class CashLoanController extends BaseController {
    @Autowired
    private CashLoanService cashLoanService;

    public CashLoanController() {
        super("21");
    }

    @RequestMapping(value = "/api/payment/activity", method = RequestMethod.POST)
    public String activity() {
        String channelNo = this.getChannelNo();
        if (StringUtils.isEmpty(channelNo))
            return StringUtils.EMPTY;
        return cashLoanService.getActivityUrl();
    }


    @RequestMapping(value = "/api/payment/activityLogin", method = RequestMethod.POST)
    public Map<String, Object> activityLogin() {
        String channelNo = this.getChannelNo();
        if (StringUtils.isEmpty(channelNo))
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "渠道号不能为空");
        return this.cashLoanService.joinActivity();
    }

    /**
     * 现金贷订单提交
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/payment/cashLoan/commitOrder", method = RequestMethod.POST)
    public Map<String, Object> commitOrder(@RequestBody Map<String, Object> map) throws Exception {
        return cashLoanService.commitOrder(map);
    }

    /**
     * 订单保存
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "/api/payment/cashLoan/saveOrder", method = RequestMethod.POST)
    public Map<String, Object> saveOrder(@RequestBody Map<String, Object> map) {
        return cashLoanService.saveOrder(map);
    }
}
