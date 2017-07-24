package com.haiercash.acquirer.server;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.haiercash.acquirer.Application;
import com.haiercash.acquirer.controller.AcqSyncLogController;
import com.haiercash.acquirer.controller.LcApplController;
import com.haiercash.acquirer.dao.*;
import com.haiercash.acquirer.data.*;
import com.haiercash.acquirer.mq.AcqPublisher;
import com.haiercash.acquirer.service.ApplCompareService;
import com.haiercash.acquirer.service.ApplSyncService;
import com.haiercash.acquirer.service.LcApplService;
import com.haiercash.cmis.dao.CmisAcqChannelDao;
import com.haiercash.cmis.data.*;
import com.haiercash.cmis.service.CmisAcqChannelService;
import com.haiercash.commons.rest.cmis.CmisResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2017/3/1.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@EnableAutoConfiguration
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class MqTest {
    @Autowired
    private AcqPublisher acqPublisher;
    @Autowired
    ApplSyncService applSyncService;
    @Autowired
    ApplCompareService applCompareService;
    @Autowired
    CmisAcqChannelService cmisAcqChannelService;
    @Autowired
    AcqSyncLogController acqSyncLogController;
    @Autowired
    LcApplDao lcApplDao;
    @Autowired
    LcApplExtDao lcApplExtDao;
    @Autowired
    LcApplApptDao lcApplApptDao;
    @Autowired
    LcApptIndivDao lcApptIndivDao;
    @Autowired
    LcApptAssetDao lcApptAssetDao;
    @Autowired
    LcApptRelDao lcApptRelDao;
    @Autowired
    LcApplAccInfoDao lcApplAccInfoDao;
    @Autowired
    LcApplGoodsDao lcApplGoodsDao;
    @Autowired
    LcApptExtDao lcApptExtDao;
    @Autowired
    LcApplService lcApplService;
    @Autowired
    LcApplController lcApplController;

    @Test
    public void sendSyncMessage() {
        acqPublisher.sendSyncMessage(null, "", 1L);
        acqPublisher.sendSyncMessage(null, "02", 2L);
        acqPublisher.sendSyncMessage(3L, "03", 3L);
        try {
            Thread.sleep(2000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSendMessage() {
//        acqPublisher.sendAppMsgToMy( null,"状态",123L);
    }

    @Test
    public void sendVerifyMessage() {
        acqPublisher.sendVerifyMessage("信息校验", "1");
        acqPublisher.sendVerifyMessage("信息校验", "2");
        acqPublisher.sendVerifyMessage("信息校验", "3");
        try {
            Thread.sleep(2000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void syncLcAppl() {
        applCompareService.applCompare(40271L);
    }

    @Test
    public void getChannelByChannelNo() {
        CmisResponse channelByChannelNo = cmisAcqChannelService.getChannelByChannelNo();
        System.out.print("");
    }

    @Test
    public void insertAcqChannel() {
        cmisAcqChannelService.insertAcqChannel();
    }

    @Test
    public void syncCounts1() {
        LcAppl lcAppl = new LcAppl();
        lcAppl.setIdNo("37040319910722561X");
        List<LcAppl> select = lcApplDao.select(lcAppl);
        for (LcAppl lcApplOne : select) {
            Long applSeq = lcApplOne.getApplSeq();
            LcApptIndiv lcApptIndiv = new LcApptIndiv();
            LcApptExt lcApptExt = new LcApptExt();
            lcApptIndiv.setApplSeq(applSeq);
            lcApptExt.setApplSeq(applSeq);
            lcApplDao.deleteByPrimaryKey(applSeq);
            lcApplExtDao.deleteByPrimaryKey(applSeq);
            lcApplApptDao.deleteByApplSeq(applSeq + "");
            lcApptIndivDao.delete(lcApptIndiv);
            lcApptExtDao.delete(lcApptExt);
            lcApptAssetDao.deleteByApptSeq(applSeq);
            lcApptRelDao.deleteByApptSeq(applSeq);
            lcApplAccInfoDao.deleteByApplSeq(applSeq);
            lcApplGoodsDao.deleteByApplSeq(applSeq + "");

        }
    }
}
