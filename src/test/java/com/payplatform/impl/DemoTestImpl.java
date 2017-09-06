package com.payplatform.impl;

import com.haiercash.payplatform.Application;
import com.haiercash.payplatform.common.service.CrmService;
import com.payplatform.DemoTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * demo for unit test.
 * @author Liu qingxiang
 * @since v1.0.1
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class DemoTestImpl implements DemoTest {

    @Autowired
    private CrmService crmService;

    @Test
    public void test(){
        Map<String, Object> map = new HashMap<>();

        System.out.println(crmService.getCustIsPass(map));
    }

}
