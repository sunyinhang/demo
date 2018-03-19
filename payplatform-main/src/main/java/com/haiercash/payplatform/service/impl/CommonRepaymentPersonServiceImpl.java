package com.haiercash.payplatform.service.impl;

import com.haiercash.payplatform.common.dao.AppOrdernoTypgrpRelationDao;
import com.haiercash.payplatform.common.data.CommonRepaymentPerson;
import com.haiercash.payplatform.common.enums.AcquirerCommonPersonEnum;
import com.haiercash.payplatform.service.CommonRepaymentPersonService;
import com.haiercash.payplatform.service.CrmService;
import com.haiercash.payplatform.utils.AcqTradeCode;
import com.haiercash.payplatform.utils.AcqUtil;
import com.haiercash.spring.eureka.EurekaServer;
import com.haiercash.spring.service.BaseService;
import com.haiercash.spring.util.ConstUtil;
import com.haiercash.spring.util.HttpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Commont repayment person service impl.
 *
 * @author Liu qingxiang
 * @see CommonRepaymentPersonService
 */
@Service
public class CommonRepaymentPersonServiceImpl extends BaseService implements CommonRepaymentPersonService {

    @Autowired
    private AppOrdernoTypgrpRelationDao appOrdernoTypgrpRelationDao;
    @Autowired
    private CmisServiceImpl cmisService;
    @Autowired
    private CrmService crmService;

    public Map<String, Object> getCommonRepaymentPerson(String applSeq) {
        String acqUrl = EurekaServer.ACQUIRER + "/api/appl/seletctApptInfo";
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("channelNo", super.getChannelNo());
        paramMap.put("applSeq", applSeq);
        Map<String, Object> acqResponse = AcqUtil
                .getAcqResponse(acqUrl, AcqTradeCode.SELECT_COMMON_PERSON, super.getChannel(), super.getChannelNo(), "",
                        "", paramMap);
        if (acqResponse == null || acqResponse.isEmpty()) {
            return fail(ConstUtil.ERROR_CODE, "收单系统通信失败");
        }
        if (!AcqUtil.isSuccess(acqResponse)) {
            Map<String, Object> responseMap = (Map<String, Object>) acqResponse.get("response");
            Map<String, Object> headMap = (Map<String, Object>) responseMap.get("head");
            return fail((String) headMap.get("retFlag"), (String) headMap.get("retMsg"));
        }
        Map<String, Object> bodyMap = (Map<String, Object>) ((Map<String, Object>) acqResponse.get("response"))
                .get("body");
        List<Map<String, Object>> apptList = (List<Map<String, Object>>) ((Map<String, Object>) bodyMap.get("apptList"))
                .get("appt");
        List<CommonRepaymentPerson> personList = new ArrayList<>();
        apptList.forEach(apptMap -> personList
                .add(AcquirerCommonPersonEnum.acquirerMap2CommonPersonObject(apptMap, new CommonRepaymentPerson())));
        for (CommonRepaymentPerson person : personList) {
            person.setApplSeq(applSeq);
            person.setId(applSeq);

            // 客户基本信息查询
            Map<String, Object> smrzMap = crmService.queryMerchCustInfo(person.getName(), person.getIdNo());
            if (!HttpUtil.isSuccess(smrzMap) || StringUtils.isEmpty(smrzMap.get("body"))) {
                return fail("96", "实名认证失败！");
            }
            Map<String, Object> smrz = (Map<String, Object>) smrzMap.get("body");
            String mobile = StringUtils.isEmpty(smrz.get("mobile")) ? "" : (String) smrz.get("mobile");
            String name = StringUtils.isEmpty(smrz.get("custName")) ? "" : (String) smrz.get("custName");
            String idNo = StringUtils.isEmpty(smrz.get("certNo")) ? "" : (String) smrz.get("certNo");
            String cardNo = StringUtils.isEmpty(smrz.get("cardNo")) ? "" : (String) smrz.get("cardNo");
            String custNo = StringUtils.isEmpty(smrz.get("custNo")) ? "" : (String) smrz.get("custNo");
            String repayAcProvince = "";
            String repayAcCity = "";
            if (smrz.containsKey("acctProvince")) {
                repayAcProvince = StringUtils.isEmpty(smrz.get("acctProvince")) ?
                        "" :
                        (String) smrz.get("acctProvince");
            }
            if (smrz.containsKey("acctCity")) {
                repayAcCity = StringUtils.isEmpty(smrz.get("acctCity")) ? "" : (String) smrz.get("acctCity");
            }

            person.setName(name);
            person.setIdNo(idNo);
            person.setMobile(mobile);
            person.setCardNo(cardNo);
            //以查询的为准，没值的用实名认证的覆盖掉
            person.setRepayAcProvince(
                    StringUtils.isEmpty(person.getRepayAcProvince()) ? repayAcProvince : person.getRepayAcProvince());
            person.setRepayAcCity(StringUtils.isEmpty(person.getRepayAcCity()) ? repayAcCity : person.getRepayAcCity());
            person.setCommonCustNo(custNo);
        }
        return success(personList);
    }

    @Override
    @Transactional
    public Map<String, Object> deleteCommonRepaymentPerson(String applSeq) {
        Map<String, Object> getCommonPerson = this.getCommonRepaymentPerson(applSeq);
        if (!HttpUtil.isSuccess(getCommonPerson)) {
            return getCommonPerson;
        }
        List<CommonRepaymentPerson> personList = (List<CommonRepaymentPerson>) getCommonPerson.get("body");
        if (personList == null || personList.isEmpty()) {
            return fail("10", "要删除的共同还款人不存在！");
        }
        //删除共同还款人信息
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("applSeq", applSeq);
        String acqUrl = EurekaServer.ACQUIRER + "/api/appt/deleteAppt";
        Map<String, Object> acqResponse = AcqUtil
                .getAcqResponse(acqUrl, AcqTradeCode.DELETE_APPT, super.getChannel(), super.getChannelNo(), "", "",
                        paramMap);
        if (acqResponse == null || acqResponse.isEmpty()) {
            return fail(ConstUtil.ERROR_CODE, "收单系统通信失败！");
        }
        if (!AcqUtil.isSuccess(acqResponse)) {
            return fail("22", "删除共同还款人失败!");
        }
        return success();
    }

}

