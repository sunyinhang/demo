package com.haiercash.payplatform.tasks.rabbitmq;

import com.haiercash.payplatform.service.ShunGuangMessageService;
import com.haiercash.spring.controller.BaseController;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Created by Administrator on 2017/12/21.
 */
@RestController
public class ShunGuangMessageHandler extends BaseController {
    private static String MODULE_NO = "03";
    public Log logger = LogFactory.getLog(getClass());
    @Autowired
    private ShunGuangMessageService shunGuangMessageService;

    public ShunGuangMessageHandler() {
        super(MODULE_NO);
    }

    /**
     * 顺逛退货
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "/api/payment/shunguang/shunguangth", method = RequestMethod.POST)
    public Map<String, Object> shunguangth(@RequestParam Map<String, Object> map) {
//        HashMap<Object, Object> map1 = new HashMap<>();
//        HashMap<Object, Object> map2 = new HashMap<>();
//        HashMap<String, Object> map3 = new HashMap<>();
//        map1.put("cooprCode","");
//        map1.put("tradeTime","10:18:52");
//        map1.put("sysFlag","04");
//        map1.put("channelNo","46");
//        map1.put("serno","14894579327991");
//        map1.put("tradeCode","100026");
//        map1.put("tradeDate","2017-03-14");
//        map1.put("tradeType","");
//        map2.put("msgTyp","03");
//        map2.put("applSeq","12321");
//        map2.put("mallOrderNo","456765444");
//        map2.put("loanNo","32432");
//        map2.put("idNo","14894579327991");
//        map2.put("custName","测试");
//        map2.put("businessId","20323");
//        map2.put("businessType","RETURN_GOODS");
//        map2.put("status","05");
//        map2.put("content","成功");
//        map3.put("head",map1);
//        map3.put("body",map2);
        return shunGuangMessageService.ShunGuangth(map);
    }
}
