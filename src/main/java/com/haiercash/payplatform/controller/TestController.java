package com.haiercash.payplatform.controller;

import com.haiercash.commons.redis.Cache;
import com.haiercash.payplatform.common.utils.HttpUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static com.haiercash.payplatform.common.utils.RestUtil.success;

/**
 * Created by yuanli on 2017/7/27.
 */
@RestController
public class TestController extends BasePageController {
    public Log logger = LogFactory.getLog(getClass());
    //模块编码  01
    private static String MODULE_NO = "01";

    public TestController() {
        super(MODULE_NO);
    }

    @Autowired
    private Cache cache;

    @Value("${app.file.uploadPath}")
    protected String uploadPath;

    @Value("${app.rest.CRM}")
    protected String crmurl;

    @Value("${app.rest.APPSERVER}")
    protected String appseverurl;

    @Value("${app.other.haierDataImg_url}")
    protected String haierDataImg_url;

    /**
     * get请求测试
     * @param userId
     * @return
     */
    @RequestMapping(value = "/api/shunguang/gettest", method = RequestMethod.GET)
    public Map<String, Object> gettest(@RequestParam String userId) {
        String url = crmurl + "/app/crm/cust/getStores?userId=" + userId;
        //方法1
        String json = HttpUtil.restGet(url);
        System.out.println("json结果输出："+json);
        //return HttpUtil.json2DeepMap(json);

        //方法2
        Map<String, Object> map = HttpUtil.restGetMap(url);
        System.out.println("map结果输出："+map);
        //return success(map);


        //return HttpUtil.json2DeepMap(resultmap);

        return map;
    }

    @RequestMapping(value = "/api/shunguang/test", method = RequestMethod.GET)
    public Map<String, Object> validateGesture(@RequestParam Map<String, Object> params) {
        //方法3
        String url = appseverurl + "/app/appserver/getEdCheck";
//        String url = EurekaServer.APPSERVER + "/app/appserver/apporder/getWtjAppOrder";
        String token = "b98468da-997f-4b25-b466-26ede7c15653";
//        Map<String, Object> map1= new HashMap<String, Object>();
//        map1.put("idTyp", "20");
//        map1.put("idNo", "370305199201031528");
//        map1.put("channel", "11");
//        map1.put("channelNo", "43");
        Map<String, Object> resultmap = HttpUtil.restGetMap(url, token, params);
        System.out.println("map3结果输出："+resultmap);
        return resultmap;
    }

    /**
     * put请求
     * @param params
     * @return
     */
    public Map<String, Object> puttest(Map<String, Object> params) {
        String token = "b98468da-997f-4b25-b466-26ede7c15653";
        String url = appseverurl + "/app/uauth/payPasswd";
        logger.info("url:" + url + "  params:" + params);

        //带token
        Map<String, Object> resultmap0 = HttpUtil.restPutMap(url, token, params);
        logger.info("==result:" + resultmap0);


        //不带token
        Map<String, Object> resultmap1 = HttpUtil.restPutMap(url, params);

        return success();
    }

    /**
     * post请求
     * @param params
     * @return
     */
    @RequestMapping(value = "/api/shunguang/posttest", method = RequestMethod.POST)
    public Map<String, Object> posttest(@RequestBody Map<String, Object> params) {
//
//        {
//            "typCde": "16097a",
//                "apprvAmt": "3000",
//                "applyTnrTyp": "6",
//                "applyTnr":"6",
//                "channel":"11",
//                "channelNo":"43"
//        }
        String url = appseverurl + "/app/appserver/customer/getPaySs";
        logger.info("url:" + url + "  params:" + params);

        String token = "b98468da-997f-4b25-b466-26ede7c15653";

        //带token
        Map<String, Object> result0 = HttpUtil.restPostMap(url, token, params);
        logger.info("带token输出：" + result0);

        //不带token
        Map<String, Object> result1 = HttpUtil.restPostMap(url, params);
        logger.info("不带token输出：" + result1);

        return success();
    }


    @RequestMapping(value = "/api/shunguang/redistest", method = RequestMethod.GET)
    public Map<String, Object> redistest() {
        cache.set("XYZ", "12000");
        String value = cache.get("XYZ");
        logger.info("value:" + value);

        return success();

    }

    @RequestMapping(value = "/api/shunguang/redistest1", method = RequestMethod.GET)
    public Map<String, Object> redistest1() {
        //cache.set("XYZ", "12000");
        String value = cache.get("XYZ");
        logger.info("value00:" + value);


        cache.set("XYZ", "13000");
        String value0 = cache.get("XYZ");
        logger.info("value:" + value0);

        Map map = new HashMap<>();
        map.put("id", "1001");
        map.put("name", "abc");
        cache.set("token", map);

        Map m = cache.get("token");
        String id = (String) m.get("id");
        String name = (String) m.get("name");

        map.put("name", "abcde");
        map.put("sex", "x");
        cache.set("token", map);

        Map mm = cache.get("token");
        String id1 = (String) mm.get("id");
        String name1 = (String) mm.get("name");
        String sex = (String) mm.get("sex");


        return success();

    }


    @RequestMapping(value = "/api/shunguang/datatest", method = RequestMethod.GET)
    public Map<String, Object> datatest() {
        logger.info("uploadPath:" + uploadPath);
        logger.info("crmurl:" + crmurl);
        logger.info("hai:" + haierDataImg_url);
        return success();
    }

}
