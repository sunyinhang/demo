package com.haiercash.payplatform.pc.cashloan.controller;

import com.bestvike.lang.StringUtils;
import com.haiercash.payplatform.controller.BaseController;
import com.haiercash.payplatform.pc.cashloan.service.CashLoanService;
import com.haiercash.payplatform.utils.ConstUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletException;
import java.io.IOException;
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

    @RequestMapping(value = "/api/payment/activity", method = RequestMethod.GET)
    public ModelAndView activity() throws ServletException, IOException {
        String channelNo = this.getChannelNo();
        if (StringUtils.isEmpty(channelNo))
            return new ModelAndView("forward:/error");
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
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/payment/cashLoan/commitOrder", method = RequestMethod.POST)
    public Map<String, Object> commitOrder(@RequestBody Map<String, Object> map) throws Exception {
        return cashLoanService.commitOrder(super.initParam(map));
    }


}
