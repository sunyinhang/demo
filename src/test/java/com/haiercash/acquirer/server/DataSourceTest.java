package com.haiercash.acquirer.server;

import com.haiercash.acquirer.Application;
import com.haiercash.acquirer.controller.LcApplController;
import com.haiercash.acquirer.service.LcApplService;
import com.haiercash.cmis.service.CmisLcApplService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.Map;

/**
 * Created by Administrator on 2016/11/28.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@EnableAutoConfiguration
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class DataSourceTest {
    @Autowired
    LcApplService lcAppleService;
    @Autowired
    CmisLcApplService cmisLcApplService;

    @Test
    public void seletctInfoId() {
        Map<String, Object> map = lcAppleService.select(1303845L);
        System.out.println(map);
    }

}
