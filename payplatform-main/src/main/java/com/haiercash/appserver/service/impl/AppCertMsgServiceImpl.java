package com.haiercash.appserver.service.impl;

import com.haiercash.appserver.service.CrmService;
import com.haiercash.common.data.AppCertMsg;
import com.haiercash.common.data.AppCertMsgRepository;
import com.haiercash.appserver.service.AppCertMsgService;
import com.haiercash.common.service.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * Created by yinjun on 2017/2/20.
 */
@Service
public class AppCertMsgServiceImpl extends BaseService implements AppCertMsgService {
    public AppCertMsgServiceImpl() {
        super("20");
    }

    @Autowired
    AppCertMsgRepository appCertMsgRepository;
    @Autowired
    CrmService crmService;

    @Override
    public Map<String, Object> saveCardMsg(AppCertMsg appCertMsg, String channelNo) {
        logger.info("saveCardMsg:===>" + appCertMsg);
        //格式校验
        String certNo = appCertMsg.getCertNo();
//        String birthDt = appCertMsg.getBirthDt();
//        String gender = appCertMsg.getGender();
//        String endDate = appCertMsg.getCertEndDt();
//        String staDate = appCertMsg.getCertStrDt();
//        if (Objects.equals("长期", endDate)) {
//            appCertMsg.setCertEndDt("9999-99-99");
//        }
//        if (Objects.equals("长期", appCertMsg.getAfterCertEndDt())) {
//            appCertMsg.setAfterCertEndDt("9999-99-99");
//        }
//        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
//        try {
//            if (!StringUtils.isEmpty(appCertMsg.getAfterCertStrDt())) {
//                Date date = formatter.parse(appCertMsg.getAfterCertStrDt());
//            }
//            if (!StringUtils.isEmpty(appCertMsg.getAfterCertEndDt())) {
//                Date date2 = formatter.parse(appCertMsg.getAfterCertEndDt());//对长期已进行了处理
//            }
//            if (!StringUtils.isEmpty(appCertMsg.getAfterBirthDt())) {
//                Date date3 = formatter.parse(appCertMsg.getAfterBirthDt());
//            }
//
//        } catch (Exception e) {
//            return fail("13", "日期格式不正确！");
//        }
//        if (!IdcardUtils.validateCard(certNo.toUpperCase())) {
//            return fail("10", "身份证格式校验不通过！");
//        }
//        if (!Objects.equals(birthDt.replaceAll("-", ""), IdcardUtils.getBirthByIdCard(certNo))) {
//            return fail("11", "出生日期与本人身份信息有误！");
//        }
//        if (!Objects.equals(gender, IdcardUtils.getGenderByIdCard(certNo))) {
//            return fail("12", "性别与本人身份信息有误！");
//
//        }
        AppCertMsg orgAppCertMsg = appCertMsgRepository.getCardMsgByCertNo(certNo);
        if (orgAppCertMsg == null) {
            appCertMsg.setId(UUID.randomUUID().toString().replace("-", ""));
        } else {
            appCertMsg.setId(orgAppCertMsg.getId());
        }
        // appCertMsg.setCertNo(certNo.toUpperCase());//身份证号转成大写
        appCertMsg.setInsertTime(new Date());
        appCertMsg.setDeleteFlag("0");//默认为0 ==》 未删除
        //判断用户是否已经实名认证，若已实名认证，则直接存至crm
        String crm_custName = StringUtils.isEmpty(appCertMsg.getAfterCustName()) ? appCertMsg.getCustName() : appCertMsg.getAfterCustName();
        String crm_certNo = StringUtils.isEmpty(appCertMsg.getAfterCertNo()) ? appCertMsg.getCertNo() : appCertMsg.getAfterCertNo();
        Map smrzMap = crmService.queryMerchCustInfo(crm_custName, crm_certNo);
        Map<String, Object> smrzHeadMap = (Map<String, Object>) smrzMap.get("head");
        String smrzRetMsg = String.valueOf(smrzHeadMap.get("retMsg"));
        String smrzRetFlag = String.valueOf(smrzHeadMap.get("retFlag"));
        if (Objects.equals("00000", smrzRetFlag)) {
            logger.info("已实名认证，需将此信息上传crm(85接口)");
            HashMap reqMap = new HashMap<String, Object>();
            logger.info("身份证信息查询结果：" + appCertMsg);
            reqMap.put("custNo", String.valueOf(((Map<String, Object>) smrzMap.get("body")).get("custNo")));
            reqMap.put("certStrDt", StringUtils.isEmpty(appCertMsg.getAfterCertStrDt()) ? appCertMsg.getCertStrDt() : appCertMsg.getAfterCertStrDt());
            reqMap.put("certEndDt", StringUtils.isEmpty(appCertMsg.getAfterCertEndDt()) ? appCertMsg.getCertEndDt() : appCertMsg.getCertEndDt());
            reqMap.put("certOrga", StringUtils.isEmpty(appCertMsg.getAfterCertOrga()) ? appCertMsg.getCertOrga() : appCertMsg.getAfterCertOrga());
            reqMap.put("ethnic", StringUtils.isEmpty(appCertMsg.getAfterEthnic()) ? appCertMsg.getEthnic() : appCertMsg.getAfterEthnic());
            reqMap.put("birthDt", StringUtils.isEmpty(appCertMsg.getAfterBirthDt()) ? appCertMsg.getBirthDt() : appCertMsg.getAfterBirthDt());
            reqMap.put("dataFrom", channelNo);
            reqMap.put("gender", StringUtils.isEmpty(appCertMsg.getAfterGender()) ? appCertMsg.getGender() : appCertMsg.getAfterGender());
            //户籍详细地址
            reqMap.put("regAddr", StringUtils.isEmpty(appCertMsg.getAfterRegAddr()) ? appCertMsg.getRegAddr() : appCertMsg.getAfterRegAddr());
/**
            if (       //开始时间
                    ((!StringUtils.isEmpty(appCertMsg.getAfterCertStrDt()) && (!Objects.equals(appCertMsg.getAfterCertStrDt(), appCertMsg.getCertStrDt()))))
                            ||//结束时间
                            (!StringUtils.isEmpty(appCertMsg.getAfterCertEndDt()) && (!Objects.equals(appCertMsg.getAfterCertEndDt(), appCertMsg.getCertEndDt())))
                            ||//签证机关
                            (!StringUtils.isEmpty(appCertMsg.getAfterCertOrga()) && (!Objects.equals(appCertMsg.getAfterCertOrga(), appCertMsg.getCertOrga())))
                            ||//民族
                            (!StringUtils.isEmpty(appCertMsg.getAfterEthnic()) && (!Objects.equals(appCertMsg.getAfterEthnic(), appCertMsg.getEthnic())))
                            ||//出生年月日
                            (!StringUtils.isEmpty(appCertMsg.getAfterBirthDt()) && (!Objects.equals(appCertMsg.getAfterBirthDt(), appCertMsg.getBirthDt())))
                            //姓名 custName
                            ||
                            (!StringUtils.isEmpty(appCertMsg.getAfterCustName()) && (!Objects.equals(appCertMsg.getAfterCustName(), appCertMsg.getCustName())))
                            //性别：gender
                            ||
                            (!StringUtils.isEmpty(appCertMsg.getAfterGender()) && (!Objects.equals(appCertMsg.getAfterGender(), appCertMsg.getGender())))
                            //户籍地址：regAddr
                            ||
                            (!StringUtils.isEmpty(appCertMsg.getAfterRegAddr()) && (!Objects.equals(appCertMsg.getAfterRegAddr(), appCertMsg.getRegAddr())))
                    ) {

                reqMap.put("certStrDt", "");
                reqMap.put("certEndDt", "");
            }

 **/
            logger.info("crm85封装后的请求map:" + reqMap);
            Map<String, Object> saveToCrmMap = crmService.saveAllCustExtInfo(reqMap);
            /**
             * 判断扩展信息保存成功与否
             */
            Map<String, Object> crm85HeadMap = (Map<String, Object>) saveToCrmMap.get("head");
            String crm85RetMsg = String.valueOf(crm85HeadMap.get("retMsg"));
            String crm85RetFlag = String.valueOf(crm85HeadMap.get("retFlag"));
            if (!Objects.equals("00000", crm85RetFlag)) {
                return fail(crm85RetFlag, crm85RetMsg);
            }
            //已实名认证的直接上传85
            // appCertMsg.setDeleteFlag("1");
        } else {
            //未实名认证的保存至数据库
            appCertMsgRepository.save(appCertMsg);
        }

        return success();
    }

    @Override
    public Map<String, Object> getCardMsgByCertNo(String certNo) {
        AppCertMsg appCertMsg = appCertMsgRepository.getCardMsgByCertNo(certNo);
        return success(appCertMsg);
    }
}
