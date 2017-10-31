/*
 * 功  能：简单说明该类的功能
 * 
 * 文件名：PaymentServiceImpl.java
 * 
 * 描  述：
 * 
 * [变更履历]
 * Version   变更日         部署              作者           变更内容
 * -----------------------------------------------------------------------------
 * V1.00     2015年12月23日   Haiercash    suyang      CREATE
 * -----------------------------------------------------------------------------
 *
 *
 * Copyright (c) 2015 Haiercash All Rights Reserved.
 *┌─────────────────────────────────────────────────—────┐
 *│ 版权声明                               Haiercash       │
 *└──────────────────────────────—————————————————————───┘
 */

package com.haiercash.payplatform.service.impl;

import com.haiercash.payplatform.common.entity.Corehead;
import com.haiercash.payplatform.common.entity.QueryLimitMessage;
import com.haiercash.payplatform.common.entity.ReturnMessage;
import com.haiercash.payplatform.config.CmisConfig;
import com.haiercash.payplatform.rest.client.XmlClientUtils;
import com.haiercash.payplatform.service.PaymentServiceInterface;
import com.haiercash.payplatform.utils.XstreamBuild;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * DESCRIPTION:
 * <p>
 * <p>
 * <a href="PaymentServiceImpl.java"><i>View Source</i></a>
 * </p>
 *
 * @author <a href="mailto:suyang@haiercash.com">suyang</a>
 * @version Revision: 1.0 Date: 2015年12月23日 下午2:35:14
 */
@Service
public class PaymentServiceImpl implements PaymentServiceInterface {
    private Log logger = LogFactory.getLog(PaymentServiceImpl.class);

    @Autowired
    private CmisConfig cmisConfig;

    @Override
    public ReturnMessage queryLimitMessage(QueryLimitMessage queryLimitMessage) {
        Corehead corehead = new Corehead();
        corehead.setTradeCode("100012");

        XstreamBuild xstreamBuild = new XstreamBuild();
        String requestXml = xstreamBuild.bulidCorerequestXml(queryLimitMessage,
                corehead);

        String response = XmlClientUtils.postForString(cmisConfig.getUrl(), requestXml);
        logger.info(response);
        Map map = (Map) xstreamBuild.bulidCoreresponseBean(response);
        List head = (List) map.get("head");
        Map headMap = (Map) head.get(0);
        ReturnMessage cmisResponse = new ReturnMessage();
        cmisResponse.setCode((String) ((List) headMap.get("retFlag")).get(0));
        cmisResponse.setMessage((String) ((List) headMap.get("retMsg")).get(0));
        if ("00000".equals(((List) headMap.get("retFlag")).get(0))) {
            List body = (List) map.get("body");
            Map bodyMap = (Map) body.get(0);
            List data = new ArrayList();
            data.add(bodyMap);
            cmisResponse.setData(data);
        }
        return cmisResponse;
    }
}
