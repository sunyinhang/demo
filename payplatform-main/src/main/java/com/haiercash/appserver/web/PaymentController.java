package com.haiercash.appserver.web;

import com.haiercash.appserver.service.PaymentService;
import com.haiercash.appserver.util.ConstUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Created by Administrator on 2016/12/15.
 */
@RestController
public class PaymentController extends BaseController {
    public static String MODULE_NO = "16";
    private Log logger = LogFactory.getLog(DhkController.class);

    public PaymentController() {
        super(MODULE_NO);
    }

    @Autowired
    PaymentService paymentService;

    /**
     * 付款码申请
     *
     * @param userId
     * @param useType
     * @param merPrivate
     * @param reserveData
     * @return
     */
    @RequestMapping(value = "/app/appserver/xcd/{userId}/paycode", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> payCodeAppl(@PathVariable String userId, @RequestParam String useType, String merPrivate, String reserveData) {
        if (StringUtils.isEmpty(userId)){
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "用户ID不可为空");
        }
        //调用外联平台接口，获取付款码
        //应用场景useType字段固定传04.
        useType = "04";
        return paymentService.payCodeAppl(userId, useType, merPrivate, reserveData);
    }
}
