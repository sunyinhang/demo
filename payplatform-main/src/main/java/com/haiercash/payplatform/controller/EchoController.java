package com.haiercash.payplatform.controller;

import com.haiercash.core.lang.DateUtils;
import com.haiercash.spring.controller.BaseController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Created by 许崇雷 on 2017-09-18.
 */
@RestController
public class EchoController extends BaseController {

    public EchoController() {
        super("00");
    }

    @GetMapping("/api/echo")
    public Map<String, Object> echoGet(String value) {
        return success("hello world!" + value);
    }

    @GetMapping("/api/echo/get")
    public Map<String, Object> echoGet(String value, String a) {
        System.out.println(a);
        DemoBean bean = new DemoBean(DateUtils.fromDateTimeString("2017-01-02 03:04:05"), DateUtils.fromDateString("2017-01-02"));
        bean.setValue(value);
        return success(bean);
    }

    @DeleteMapping("/api/echo/delete")
    public Map<String, Object> echoDelete(String value) {
        DemoBean bean = new DemoBean(DateUtils.fromDateTimeString("2017-01-02 03:04:05"), DateUtils.fromDateString("2017-01-02"));
        bean.setValue(value);
        return success(bean);
    }

    @PostMapping("/api/echo/post")
    public Map<String, Object> echoPost(@RequestBody Map map) {
        DemoBean bean = new DemoBean(DateUtils.fromDateTimeString("2017-01-02 03:04:05"), DateUtils.fromDateString("2017-01-02"));
        bean.setValue(map.get("value").toString());
        return success(bean);
    }

    @PutMapping("/api/echo/put")
    public Map<String, Object> echoPut(@RequestBody Map map) {
        DemoBean bean = new DemoBean(DateUtils.fromDateTimeString("2017-01-02 03:04:05"), DateUtils.fromDateString("2017-01-02"));
        bean.setValue(map.get("value").toString());
        return success(bean);
    }
}
