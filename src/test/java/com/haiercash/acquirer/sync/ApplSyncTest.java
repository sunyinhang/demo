package com.haiercash.acquirer.sync;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.haiercash.acquirer.TestApplication;
import com.haiercash.commons.support.ServiceException;

/**
 * Created by Administrator on 2017/3/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@EnableAutoConfiguration
@SpringApplicationConfiguration(classes = TestApplication.class)
@WebAppConfiguration
public class ApplSyncTest {
    @Autowired
    ApplSyncService applSyncService;
    @Autowired
    ApplCompareService applCompareService;

    @Autowired
    private CmisLcApplService cmisLcApplService;

    @Test
    public void syncTest() {
        Long applSeq = 1326708L;
        try {
            applSyncService.applSync(applSeq);
            System.out.println("贷款申请数据同步验证通过：" + applSeq);

            compare(applSeq);
        } catch (ServiceException se) {
            System.out.println(se.getMessage());
        }
    }

    @Test
    public void compareTest() {
        Long applSeq = 1190906L;
        try {
            compare(applSeq);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void compareAllTest() {
        String minHisDate = "2015-07-18"; //cmisLcApplService.getMinApplyDt("0000")
        String maxHisDate = "2099-01-01"; //new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        String hisApplyDt = minHisDate;
        List<Long> errorList = new ArrayList<>();
        while (hisApplyDt != null && hisApplyDt.compareTo(maxHisDate) < 0) {
            // 从核心库查询指定日期的全部流水号，写入消息队列
            List<Long> seqList = cmisLcApplService.getApplSeqByApplyDt(hisApplyDt);
            System.out.println("====> " + hisApplyDt + " 共" + seqList.size() + "笔");
            int i = 0;
            int total = seqList.size();
            for (Long applSeq : seqList) {
                try {
                    i++;
//                    System.out.print(hisApplyDt + " (" + i + "/" + total + ")  ");
                    if (!compare(applSeq)) {
                        errorList.add(applSeq);
//                        System.out.println("----------------------------------------- 不一致：" + applSeq);
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
            hisApplyDt = cmisLcApplService.getMinApplyDt(hisApplyDt);
        }
        System.out.println("[" + minHisDate + "] - [" + maxHisDate + "] 比对完成，共" +
                errorList.size() + "笔贷款申请不一致：\r\n");
        for (Long applSeq : errorList) {
            System.out.println(applSeq + "\r\n");
        }
    }

    private Boolean compare(Long applSeq) {
        List<String> errorList = applCompareService.applCompare(applSeq);
        if (errorList.size() > 0) {
            StringBuffer buffer = new StringBuffer();
            for (String msg : errorList) {
                buffer.append(msg + "\r\n");
            }
            System.out.println(applSeq + "比对不一致：\r\n" + buffer.toString());
            return false;
        } else {
//            System.out.println("贷款申请数据同步校验一致：" + applSeq);
            return true;
        }
    }

    @Autowired
    CmisSqlQueryService cmisSqlQueryService;
    @Test
    public void testFindTypInfo(){
        System.out.println(cmisSqlQueryService.findTypInfo("15093a"));
    }
}
