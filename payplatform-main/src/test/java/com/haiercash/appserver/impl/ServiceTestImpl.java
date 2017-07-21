package com.haiercash.appserver.impl;

import com.haiercash.appserver.Application;
import com.haiercash.appserver.ServiceTest;
import com.haiercash.appserver.service.AppOrderService;
import com.haiercash.common.data.AppCmisInfo;
import com.haiercash.common.data.AppCmisInfoRepository;
import com.haiercash.common.data.AppContract;
import com.haiercash.common.data.AppContractRepository;
import com.haiercash.commons.util.HttpUtil;
import com.haiercash.commons.util.RedisUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.jdbc.SqlScriptsTestExecutionListener;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service TestImpl.
 * @author Liu qingxiang
 * @see ServiceTestImpl
 * @since v1.1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {Application.class})
@WebAppConfiguration
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class,
//        TransactionDbUnitTestExecutionListener.class,
        TransactionalTestExecutionListener.class,
        SqlScriptsTestExecutionListener.class})
@Transactional
public class ServiceTestImpl implements ServiceTest{

    private AppOrderService appOrderService;

    @Autowired
    private AppContractRepository appContractRepository;

    @Autowired
    private AppCmisInfoRepository appCmisInfoRepository;

    @Before
    public void init(){
        this.appOrderService = PowerMockito.mock(AppOrderService.class);
    }

    @Test
    public void test(){
        Iterable<AppContract> list = appContractRepository.findAll();
        //PowerMockito.when(appOrderService.saveOrder(Mockito.any())).thenReturn(null);
    }

    @Test
//    @Sql("/dbfile/test.sql")
    public void test2(){

        List<AppContract> c = (List<AppContract>) appContractRepository.findAll();
        System.out.println(c.size());


    }

    @Test
    public void test3(){
        Map<String, Object> map = new HashMap<>();
        map.put("11", "1111");
        map.put("22", "2222");

        List<Map<String, Object>> info = new ArrayList<>();

        Map<String, Object> promCdeMap = new HashMap<>();
        promCdeMap.put("dataTyp", "A041");
        promCdeMap.put("content", "promCde");
        promCdeMap.put("reserved6", "applseq");
        info.add(promCdeMap);

        Map<String, Object> promNameMap = new HashMap<>();
        promNameMap.put("dataTyp", "A042");
        promNameMap.put("content", "promName");
        promNameMap.put("reserved6", "applseq");
        info.add(promNameMap);

        map.put("info", info);

        JSONObject jsonObject = new JSONObject(map);
        String s = jsonObject.toString();
        System.out.println(s);

        Map<String, Object> responseMap = HttpUtil.json2Map(s);
        ArrayList array = (ArrayList) responseMap.get("info");

    }

    @Test
    public void test4() {
        AppCmisInfo appCmisInfo = new AppCmisInfo();
        appCmisInfo.setId(UUID.randomUUID().toString().replace("-", ""));
        appCmisInfo.setTradeCode("100054");
        appCmisInfo.setFlag("0");
        appCmisInfo.setInsertTime(new Date());
        appCmisInfo.setRequestMap("{\"registTime\":\"2017-02-22\",\"dataTyp\":\"04\",\"signTime\":\"2017-02-22\",\"sysId\":\"04\",\"idTyp\":\"20\",\"mobileNo\":\"15264826872\",\"dataDt\":\"2017-02-22\",\"custName\":\"李甲团\",\"list\":{\"info\":[{\"dataTyp\":\"04\",\"reserved6\":\"\",\"content\":\"经度0.0纬度0.0\"}]},\"idNo\":\"37040319910722561X\"}");
        appCmisInfoRepository.save(appCmisInfo);
    }

    @Test
    public void test5() {
        System.out.println(Arrays.toString(RedisUtil.serialize("__token_app_userStore")));
    }


}
