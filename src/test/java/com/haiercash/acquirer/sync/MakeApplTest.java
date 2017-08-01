package com.haiercash.acquirer.sync;

import java.util.HashMap;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.alibaba.fastjson.JSONObject;
import com.haiercash.acquirer.TestApplication;
import com.haiercash.acquirer.service.MakeApplService;

/**
 * Created by Administrator on 2017/3/23.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@EnableAutoConfiguration
@SpringApplicationConfiguration(classes = TestApplication.class)
@WebAppConfiguration
public class MakeApplTest {

    @Autowired
    MakeApplService makeApplService;
    @Autowired
    ApplCompareService applCompareService;
    @Autowired
    LcApplDao lcApplDao;

    LcApplApptDao lcApplApptDao;

    @Test
    public void singleTest() {
        Long cmisApplSeq = 1326760L;
        //查询状态为
        //LcAppl lcAppl=new LcAppl();
       // lcAppl.setOutSts();
        List<LcAppl>  list=lcApplDao.selectAll();
        int k=0;
        for(LcAppl appl:list){
            Long seq=appl.getApplSeq();
            HashMap<String, Object> responseMap = makeApplService.makeAppl(cmisApplSeq, seq);
            System.out.println("进件完成：" + responseMap);
            JSONObject response= (JSONObject)responseMap.get("response");
            JSONObject headMap = (JSONObject) response.get("head");
            if (!"0000".equals(headMap.get("retFlag"))) {
                System.out.println("进件失败：" + headMap.get("retMsg"));
                return;
            }
            JSONObject bodyMap = (JSONObject) response.get("body");
            Long applSeq = new Long(bodyMap.get("applSeq").toString());
            System.out.println("新生成贷款申请流水号：" + applSeq);
            k++;
        }
        System.out.println("进件："+k+"笔");

      //  Long cmisApptApplSeq = 1326688L;

//        verifyAppl(applSeq, cmisApplSeq);
//        System.out.println(JSONObject.toJSONString(response));

//        makeApplService.commitAppl(applSeq);
    }

    @Test
    public void verifyTest() {
        Long applSeq = 1260916L;
        Long cmisApplSeq = 1307146L;
        verifyAppl(applSeq, cmisApplSeq);
    }

    private void verifyAppl(Long applSeq, Long cmisApplSeq) {
        List<String> errorList = applCompareService.applCompareTo(applSeq, cmisApplSeq);
        if (errorList.size() > 0) {
            System.out.println(applSeq + "比对不一致：");
            for (String msg : errorList) {
                System.out.println("----- " + msg);
            }
        } else {
            System.out.println("贷款申请数据同步校验一致：" + applSeq);
        }
    }

}
