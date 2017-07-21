package com.haiercash.appserver.web;

import com.haiercash.appserver.service.AppOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * CA Confirm controller.
 * @author liu qingxiang
 * @since v1.3.0
 */
@Controller
public class CAConfirmController extends BaseController {

    private static String MODULE_NO = "11";
    public CAConfirmController() {
        super(MODULE_NO);
    }

    @Autowired
    private AppOrderService appOrderService;

    /**
     * 客户确认跳转接口.
     *
     * @param  code
     * @return
     */
    @RequestMapping(value = "/app/appserver/ca", method = RequestMethod.GET)
    public String getMsgOfLoan(@RequestParam("code") String code) {
        if (StringUtils.isEmpty(code)) {
            return "/agreement/404.html";
        }

        //return "/agreement/xxx.html";
        return "redirect:/static/index.html?code="+code;
    }

    /**
     * 合同确认页面回显订单数据
     * @param signCode 流水号
     * @return
     */
    @RequestMapping(value = "/app/appserver/contractData", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getContractConfirmData(@RequestParam("code") String signCode) {
        if (StringUtils.isEmpty(signCode)) {
            return fail("17", "signCode不能为空");
        }
        Map<String, Object> confirmData = appOrderService.getContractConfirmData(signCode);
        if (confirmData == null || confirmData.isEmpty()) {
            return fail("18", "系统查询错误");
        }
        return success(confirmData);
    }

}
