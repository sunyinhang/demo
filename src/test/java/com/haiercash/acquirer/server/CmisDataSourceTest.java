package com.haiercash.acquirer.server;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.haiercash.payplatform.Application;

/**
 * Created by Administrator on 2017/2/27.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@EnableAutoConfiguration
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class CmisDataSourceTest {

    @Autowired
    CmisLcApplService lcApplService;

    @Test
    public void getNewApplSeqAndOutStsList() {
        List<Long> list = lcApplService.getApplSeqByApplyDt("2017-03-01");
        System.out.println(list);
    }
}
