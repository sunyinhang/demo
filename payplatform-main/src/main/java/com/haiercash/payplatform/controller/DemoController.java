package com.haiercash.payplatform.controller;

import com.bestvike.reflect.GenericType;
import com.haiercash.payplatform.rest.IResponse;
import com.haiercash.payplatform.rest.common.CommonRestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * demo controller.
 *
 * @author Liu qingxiang
 * @since v1.0.0
 */
@RestController
public class DemoController extends BaseController {
    public DemoController() {
        super("01");
    }

    @GetMapping("/api/test/get")
    public Object testGet() {
        String url = "http://payplatform-develop-tim/api/echo/get?value=哈哈&c=&a=b==";
        IResponse<DemoBean> response = CommonRestUtils.getForObject(url, DemoBean.class);
        response.assertSuccess(true);
        DemoBean bean = response.getBody();
        System.out.println(bean);
        //=====
        Map<String, Object> params = new HashMap<>();
        params.put("value", "哈哈&c=");
        params.put("a", "b==");
        url = "http://payplatform-develop-tim/api/echo/get";
        IResponse<Map> response2 = CommonRestUtils.getForMap(url, params);
        response2.assertSuccess(true);
        Map map = response2.getBody();
        System.out.println(map);
        return response2;
    }

    @GetMapping("/api/test/delete")
    public IResponse<Map> testDelete() {
        String url = "http://payplatform-develop-tim/api/echo/delete";
        Map<String, Object> params = new HashMap<>();
        params.put("value", "世界哈哈66你好==");
        IResponse<DemoBean> response = CommonRestUtils.deleteForObject(url, DemoBean.class, params);
        response.assertSuccess(true);
        DemoBean bean = response.getBody();
        System.out.println(bean);
        //=====
        IResponse<Map> response2 = CommonRestUtils.deleteForMap(url, params);
        response2.assertSuccess(true);
        Map map = response2.getBody();
        System.out.println(map);
        return response2;
    }

    @GetMapping("/api/test/post")
    public IResponse<Map> testPost() {
        String url = "http://payplatform-develop-tim/api/echo/post";
        Map<String, Object> params = new HashMap<>();
        params.put("value", "&a=世界哈哈66你好==");
        IResponse<DemoBean> response = CommonRestUtils.postForObject(url, params, new GenericType<DemoBean>() {
        });
        response.assertSuccess(true);
        DemoBean bean = response.getBody();
        System.out.println(bean);
        //=====
        IResponse<Map> response2 = CommonRestUtils.postForMap(url, params);
        response2.assertSuccess(true);
        Map map = response2.getBody();
        System.out.println(map);
        return response2;
    }

    @GetMapping("/api/test/put")
    public Object testPut() {
        String url = "http://payplatform-develop-tim/api/echo/put";
        Map<String, Object> params = new HashMap<>();
        params.put("value", "&a=世界哈哈66你好==");
        IResponse<DemoBean> response = CommonRestUtils.putForObject(url, params, DemoBean.class);
        response.assertSuccess(true);
        DemoBean bean = response.getBody();
        System.out.println(bean);
        //=====
        IResponse<Map> response2 = CommonRestUtils.putForMap(url, params);
        response2.assertSuccess(true);
        Map map = response2.getBody();
        System.out.println(map);
        return response2;
    }
}
