package com.haiercash.payplatform.pc.qidai.service;

import com.haiercash.payplatform.common.entity.Corehead;
import com.haiercash.payplatform.common.entity.QueryLoanDetails;
import com.haiercash.payplatform.common.entity.ReturnMessage;
import com.haiercash.payplatform.config.CmisConfig;
import com.haiercash.payplatform.utils.XstreamBuild;
import com.haiercash.spring.config.EurekaServer;
import com.haiercash.spring.rest.IResponse;
import com.haiercash.spring.rest.client.XmlClientUtils;
import com.haiercash.spring.rest.cmisacq.CmisAcqRequest;
import com.haiercash.spring.rest.cmisacq.CmisAcqRequestBuilder;
import com.haiercash.spring.rest.cmisacq.CmisAcqUtils;
import com.haiercash.spring.service.BaseService;
import com.haiercash.spring.util.ConstUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 许崇雷 on 2018-01-04.
 */
@Service
public class PaymentService extends BaseService {

    @Autowired
    private CmisConfig cmisConfig;


    /**
     * 查询贷款信息（收单系统）
     *
     * @param applSeq
     * @param channelNo
     * @return
     */
    public IResponse<Map> getLoanMessage(String applSeq, String channelNo) {
        String url = EurekaServer.ACQUIRER + "/api/appl/selectApplInfoApp";
        Map<String, Object> body = new HashMap<>();
        body.put("applSeq", applSeq);
        body.put("channelNo", channelNo);

        CmisAcqRequest request = CmisAcqRequestBuilder.newBuilder("ACQ-1145")
                .sysFlag(ConstUtil.CHANNEL)
                .channelNo(channelNo)
                .body(body)
                .build();
        return CmisAcqUtils.postForMap(url, request);
    }


    /**
     * DESCRIPTION:查询详细贷款
     *
     * @param queryLoanDetails
     * @param @param           queryLoanDetails
     * @param @return
     * @return ReturnMessage
     * @author xuchao
     * @date 2016年4月3日
     * queryLoanMessage 方法
     */
    public ReturnMessage queryLoanMessage(QueryLoanDetails queryLoanDetails) {
        Corehead corehead = new Corehead();
        corehead.setTradeCode("100035");
        ReturnMessage returnMessage = new ReturnMessage();
        XstreamBuild xstreamBuild = new XstreamBuild();
        String requestXml = xstreamBuild.bulidCorerequestXml(queryLoanDetails, corehead);
        logger.info(requestXml);
        // 打印请求报文
        String response = XmlClientUtils.postForString(cmisConfig.getUrl(), requestXml);
        logger.info(response);
        Map map = (Map) xstreamBuild.bulidCoreresponseBean(response);
        List head = (List) map.get("head");
        Map headMap = (Map) head.get(0);
        List retFlag = (List) headMap.get("retFlag");
        List body = (List) map.get("body");
        if ("00000".equals(retFlag.get(0))) {
            Map bodyMap = (Map) body.get(0);
            List list = new ArrayList();
            list.add(bodyMap);
            returnMessage.setCode(ConstUtil.SUCCESS_CODE);
            returnMessage.setData(list);
        }
        return returnMessage;
    }
}
