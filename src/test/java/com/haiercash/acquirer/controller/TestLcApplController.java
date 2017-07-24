package com.haiercash.acquirer.controller;

import com.haiercash.acquirer.Application;
import com.haiercash.acquirer.service.MakeApplService;
import com.haiercash.commons.rest.cmis.CmisResponse;
import com.haiercash.commons.rest.cmis.CmisRestUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

/**
 * Created by Administrator on 2017/3/14.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@EnableAutoConfiguration
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@RestController
public class TestLcApplController extends BaseCmisController {
    @Autowired
    private CmisRestUtil cmisRestUtil;
    @Autowired
    MakeApplService makeApplService;

    public TestLcApplController() {
        super("T10");
    }

    @RequestMapping(value = "/api/test/testCmis", method = RequestMethod.GET)
    public CmisResponse testCmis(Long applSeq) throws Exception {
        // logger.info("请求："+cmisRestUtil.buildRequest("100074"));
        // ResponseEntity<CmisResponse> map1=cmisEurekaRest.post(cmisRestUtil.buildRequest("100074"));
        //  CmisResponse res=map1.getBody();
        // logger.info(map1);
        //  Object result = cmisRestUtil.parseResponseBody(map1);
        //  logger.info(result);
        //  logger.info((Map)result);
        //  return  lcApplService.saveLcAppl(map,"2",super.getToken());
        /*logger.info(CommonProperties.get("address.gateUrl"));
        HashMap map=new HashMap();
        Map<String, Object> resMap = CmisUtil.getCmisResponse("100074", super.getToken(), map);
        return resMap;*/
//        String dictType = String.valueOf(map.get("dictType"));
//        String value = String.valueOf(map.get("value"));
//        logger.info("字典List:" + cmisLcApplService.queryDict(dictType));
//        logger.info("字典是否存在：" + cmisLcApplService.queryDictIfExit(dictType, value));
//        applVerifyService.checkAppl(applSeq);
        return cmisRestUtil.buildResponse(new HashMap<>());
    }
    @Test
    public void testOne(){
        makeApplService.makeAppl(new Long(1260883));
    }
}
