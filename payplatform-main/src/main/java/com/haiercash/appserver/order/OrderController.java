package com.haiercash.appserver.order;

import com.haiercash.appserver.service.OrderService;
import com.haiercash.appserver.web.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * order request controller.
 * @author Liu qingxiang
 * @since v2.0.0
 */
@RestController
@RequestMapping("/app/appserver")
public class OrderController extends BaseController{

    @Autowired
    private OrderService orderService;

    public OrderController() {
        super("25");
    }

    @RequestMapping(value = "/order/{orderNo}/sg/logisticsInfo", method = RequestMethod.GET)
    public Map<String, Object> findSgLogisticsInfoByFormId(@PathVariable String orderNo) {
        if (StringUtils.isEmpty(orderNo)) {
            return fail("01", "订单编号不能为空");
        }
        Map<String, Object> result = orderService.findSgLogisticsInfoByFormId(orderNo);
        return result;
    }

}
