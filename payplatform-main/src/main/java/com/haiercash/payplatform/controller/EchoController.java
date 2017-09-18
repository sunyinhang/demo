package com.haiercash.payplatform.controller;

import org.springframework.web.bind.annotation.GetMapping;
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
    public Map<String, Object> echo(String value) {
        return success(value);
    }
}
