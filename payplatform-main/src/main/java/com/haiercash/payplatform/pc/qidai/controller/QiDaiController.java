package com.haiercash.payplatform.pc.qidai.controller;

import com.haiercash.core.lang.DateUtils;
import com.haiercash.core.lang.StringUtils;
import com.haiercash.payplatform.common.dao.ChannelTradeLogDao;
import com.haiercash.payplatform.common.data.ChannelTradeLog;
import com.haiercash.payplatform.pc.qidai.bean.DownFileBean;
import com.haiercash.payplatform.pc.qidai.bean.HaiercashPayApplyBean;
import com.haiercash.payplatform.pc.qidai.bean.ImageUploadPO;
import com.haiercash.payplatform.pc.qidai.service.QiDaiService;
import com.haiercash.spring.controller.BaseController;
import com.haiercash.spring.rest.IResponse;
import com.haiercash.spring.util.ConstUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Created by 许崇雷 on 2018-01-04.
 */
@RestController
public class QiDaiController extends BaseController {
    @Autowired
    private ChannelTradeLogDao channelTradeLogDao;
    @Autowired
    private QiDaiService qiDaiService;

    public QiDaiController() {
        super("90");
    }

    //文件上传
    @PostMapping(value = "/api/payment/general/HaiercashFileUploadForMd5")
    public IResponse<Map> fileUploadForMd5(@RequestBody ImageUploadPO imagePO) throws Exception {
        return qiDaiService.fileUploadForMd5(imagePO);
    }

    //文件下载
    @PostMapping(value = "/api/payment/general/HaiercashFileDownloadForMd5")
    public IResponse<List> fileDownloadForMd5(@RequestBody DownFileBean downFileBean) throws Exception {
        return qiDaiService.fileDownloadForMd5(downFileBean);
    }

    //支付申请
    @PostMapping(value = "/api/payment/general/HaiercashPayApplyForJson")
    public IResponse<Map> applyForJson(@RequestBody HaiercashPayApplyBean haiercashPayApplyBean) throws Exception {
        IResponse<Map> result = null;
        try {
            return result = qiDaiService.applyForJson(haiercashPayApplyBean);
        } finally {
            // 写入渠道交易日志表
            writeLog(haiercashPayApplyBean, result);
        }
    }

    //实名认证
    @PostMapping(value = "/api/payment/general/HaiercashCrmfCiCustRealThreeInfo")
    public IResponse<Map> crmfCiCustRealThreeInfo(@RequestBody HaiercashPayApplyBean haiercashPayApplyBean) throws Exception {
        IResponse<Map> result = null;
        try {
            return result = qiDaiService.crmfCiCustRealThreeInfo(haiercashPayApplyBean);
        } finally {
            writeLog(haiercashPayApplyBean, result);
        }
    }

    //白名单
    @PostMapping(value = "/api/payment/general/HaiercashCrmAddWhiteListCmis")
    public IResponse<Map> haiercashCrmAddWhiteListCmis(@RequestBody HaiercashPayApplyBean haiercashPayApplyBean) throws Exception {
        IResponse<Map> result = null;
        try {
            return result = qiDaiService.crmAddWhiteListCmis(haiercashPayApplyBean);
        } finally {
            writeLog(haiercashPayApplyBean, result);
        }
    }

    //风险信息
    @PostMapping(value = "/api/payment/general/HaiercashPayRiskInfoApply")
    public IResponse<Map> riskInfoApply(@RequestBody HaiercashPayApplyBean haiercashPayApplyBean) throws Exception {
        IResponse<Map> result = null;
        try {
            return result = qiDaiService.riskInfoApply(haiercashPayApplyBean);
        } finally {
            writeLog(haiercashPayApplyBean, result);
        }
    }

    //还款计划
    @PostMapping(value = "/api/payment/general/HaiercashRepayment")
    public IResponse<Map> haierCashRepayment(@RequestBody HaiercashPayApplyBean haiercashPayApplyBean) throws Exception {
        logger.info("还款计划查询,开始");
        IResponse<Map> result = null;
        try {
            return result = qiDaiService.repayment(haiercashPayApplyBean);
        } finally {
            writeLog(haiercashPayApplyBean, result);
        }
    }

    //支付申请2
    @PostMapping(value = "/api/payment/general/HaiercashPayApply")
    public IResponse<Map> haierCashPayApply(@RequestBody HaiercashPayApplyBean haiercashPayApplyBean) throws Exception {
        IResponse<Map> result = null;
        try {
            return result = qiDaiService.apply(haiercashPayApplyBean);
        } finally {
            writeLog(haiercashPayApplyBean, result);
        }
    }

//    //查询签章消息推送
//    @PostMapping(value = "/api/payment/general/HaiercashQueryCAData")
//    public IResponse<Map> haiercashQueryCAData(@RequestBody HaiercashPayApplyBean haiercashPayApplyBean) throws Exception {
//        IResponse<Map> result = null;
//        try {
//            return result = qiDaiService.apply(haiercashPayApplyBean);
//        } finally {
//            writeLog(haiercashPayApplyBean, result);
//        }
//    }

    private void writeLog(HaiercashPayApplyBean haiercashPayApplyBean, IResponse<Map> result) {
        if (StringUtils.isNotEmpty(haiercashPayApplyBean.getApplyNo())) {
            ChannelTradeLog channelTradeLog = new ChannelTradeLog();
            channelTradeLog.setApplyno(haiercashPayApplyBean.getApplyNo());
            channelTradeLog.setChannelno(haiercashPayApplyBean.getChannelNo());
            channelTradeLog.setTradecode(haiercashPayApplyBean.getTradeCode());
            channelTradeLog.setRetflag(result != null ? result.getRetFlag() : ConstUtil.ERROR_CODE);
            channelTradeLog.setRetmsg(result != null ? result.getRetMsg() : ConstUtil.ERROR_INFO);
            channelTradeLog.setTradetime(DateUtils.nowDateTimeMsString());
            channelTradeLogDao.insert(channelTradeLog);
        }
    }
}
