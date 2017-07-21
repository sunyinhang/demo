package com.haiercash.appserver.service.impl;

import com.haiercash.common.apporder.utils.FormatUtil;
import com.haiercash.common.config.EurekaServer;
import com.haiercash.appserver.service.AppManageService;
import com.haiercash.appserver.service.CmisApplService;
import com.haiercash.appserver.service.CrmService;
import com.haiercash.appserver.util.ConstUtil;
import com.haiercash.appserver.web.CrmController;
import com.haiercash.appserver.web.PubController;
import com.haiercash.common.data.AppCertMsg;
import com.haiercash.common.data.AppCertMsgRepository;
import com.haiercash.common.data.CustomerInfoBean;
import com.haiercash.commons.service.BaseService;
import com.haiercash.commons.util.EncryptUtil;
import com.haiercash.commons.util.HttpUtil;
import com.haiercash.commons.util.RestUtil;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.haiercash.commons.util.RestUtil.fail;
import static com.haiercash.commons.util.RestUtil.success;

/**
 * Crm service impl.
 *
 * @author Liu qingxiang
 * @since v1.4.0
 */
@Service("crmService")
public class CrmServiceImpl extends BaseService implements CrmService {

    /**
     * log.
     */
    private Log logger = LogFactory.getLog(CrmServiceImpl.class);


    @Autowired
    AppCertMsgRepository appCertMsgRepository;

    @Autowired
    CrmController crmController;

    @Autowired
    AppManageService appManageService;

    @Autowired
    PubController pubController;

    @Autowired
    CmisApplService cmisApplService;

    @Override
    public Map<String, Object> getCustExtInfo(String custNo, String flag) {
        String url = EurekaServer.CRM + "/app/crm/cust/getCustExtInfo?pageName=1&custNo=" + custNo;
        logger.info("CRM 1.4 请求url:" + url);
        logger.debug("CRM getCustExtInfo...");
        String json = HttpUtil.restGet(url, null);
        logger.info("CRM1.4==>返回：" + json);
        if (StringUtils.isEmpty(json)) {
            logger.error("客户扩展信息接口返回异常！--》CRM 1.4");
            return fail("51", RestUtil.ERROR_INTERNAL_MSG);
        }
        logger.debug("CRM getCustExtInfo DONE");
        logger.debug("CRM 1.4返回json==" + json);
        Map<String, Object> custExtInfoMap = HttpUtil.json2Map(json);
        logger.debug("custExtInfoMap==" + custExtInfoMap);
        if (!StringUtils.isEmpty(custExtInfoMap.get("body"))) {
            // 客户扩展信息查询
            Map<String, Object> custExtInfoBodyMap = HttpUtil.json2Map(custExtInfoMap.get("body").toString());
            logger.info("custExtInfoBodyMap==" + custExtInfoBodyMap);
            if ("N".equals(flag)) {
                return success(custExtInfoBodyMap);
            } else if ("Y".equals(flag)) {
                String lxrUrl = EurekaServer.CRM + "/app/crm/cust/findCustFCiCustContactByCustNo?custNo=" + custNo;
                logger.debug("CRM findCustFCiCustContactByCustNo...");
                String lxrJson = HttpUtil.restGet(lxrUrl, null);
                logger.debug("CRM lxrJson:" + lxrJson);
                logger.debug("CRM findCustFCiCustContactByCustNo DONE");
                if (StringUtils.isEmpty(lxrJson)) {
                    logger.error("联系人列表查询失败！——》CRM 1.8");
                    return fail("52", RestUtil.ERROR_INTERNAL_MSG);
                }
                Map<String, Object> lxrMap = HttpUtil.json2Map(lxrJson);
                List<Map<String, Object>> lxrlist = new ArrayList<Map<String, Object>>();
                if (!StringUtils.isEmpty(lxrMap.get("body"))) {

                    lxrlist = (ArrayList) lxrMap.get("body");
                }
                //联系人数量超过3个，只去前3个
                for (int i = lxrlist.size(); i > 3; i--) {
                    lxrlist.remove(i - 1);
                }
                custExtInfoBodyMap.put("lxrList", lxrlist);
                return success(custExtInfoBodyMap);
            } else {
                return success(custExtInfoBodyMap);
            }
        } else {
            logger.info("客户扩展信息接口返回异常！--》CRM 1.4");
            JSONObject headJson = (JSONObject) custExtInfoMap.get("head");
            String retMsg = headJson.getString("retMsg");
            String retFlag = headJson.getString("retFlag");
            //此处只返回crm的错误码，故不适用父类的fail方法
            return fail(retFlag, retMsg);
        }
    }

    @Override
    public Map<String, Object> checkAndAddFourKeysRealInfo(CustomerInfoBean customerInfoBean) {
        logger.info("添加并验证四要素传入数据：custName=" + customerInfoBean.getApptCustName() + ",certNo=" + customerInfoBean.getApptIdNo()
                + ",cardNo=" + customerInfoBean.getRepayApplCardNo() + ",mobile=" + customerInfoBean.getIndivMobile() + ",dataFrom=" + customerInfoBean.getAppInAdvice()
                + ",threeParamVal=" + customerInfoBean.getReserved1() + ",userId=" + customerInfoBean.getUserId() + ",acctProvince=" + customerInfoBean.getRepayAcProvince()
                + ",acctCity=" + customerInfoBean.getRepayAcCity() + ",belongStore=" + customerInfoBean.getCooprCde());
        if (StringUtils.isEmpty(customerInfoBean.getApptCustName())) {
            return fail("03", "客户姓名不能为空");
        }
        if (StringUtils.isEmpty(customerInfoBean.getApptIdNo())) {
            return fail("04", "身份证号不能为空");
        }
        if (StringUtils.isEmpty(customerInfoBean.getRepayApplCardNo())) {
            return fail("05", "银行卡号不能为空");
        }
        if (StringUtils.isEmpty(customerInfoBean.getIndivMobile())) {
            return fail("06", "手机号码不能为空");
        }
        if (StringUtils.isEmpty(customerInfoBean.getAppInAdvice())) {
            return fail("07", "数据来源不能为空");
        }
        String url = EurekaServer.CRM + "/app/crm/cust/fCiCustRealThreeInfo";
        Map<String, Object> sendParam = new HashMap<>();
        // 必输字段
        sendParam.put("custName", customerInfoBean.getApptCustName());
        sendParam.put("certNo", customerInfoBean.getApptIdNo());
        sendParam.put("cardNo", customerInfoBean.getRepayApplCardNo());
        sendParam.put("mobile", customerInfoBean.getIndivMobile());
        sendParam.put("dataFrom", customerInfoBean.getAppInAdvice());
        // 非必输字段
        if (!StringUtils.isEmpty(customerInfoBean.getReserved1())) {
            sendParam.put("threeParamVal", customerInfoBean.getReserved1());
        }
        if (!StringUtils.isEmpty(customerInfoBean.getUserId())) {
            sendParam.put("userId", customerInfoBean.getUserId());
        }
        if (!StringUtils.isEmpty(customerInfoBean.getRepayAcProvince())) {
            sendParam.put("acctProvince", customerInfoBean.getRepayAcProvince());
        }
        if (!StringUtils.isEmpty(customerInfoBean.getRepayAcCity())) {
            sendParam.put("acctCity", customerInfoBean.getRepayAcCity());
        }
        if (!StringUtils.isEmpty(customerInfoBean.getCooprCde())) {
            sendParam.put("belongStore", customerInfoBean.getCooprCde());
        }
        Map<String, Object> resultMap = HttpUtil.restPostMap(url, sendParam);
        logger.info("四要素验证crm返回：" + resultMap);
        if (resultMap == null || resultMap.isEmpty()) {
            return fail("08", "请求CRM返回结果失败");
        }

        return resultMap;
    }

    @Override
    public Map<String, Object> getAllCustExtInfo(String custNo, String flag) {
        String url = EurekaServer.CRM + "/app/crm/cust/getAllCustExtInfo?custNo=" + custNo;
        logger.info("CRM 86 请求url:" + url);
        logger.debug("CRM getCustExtInfo...");
        String json = HttpUtil.restGet(url, null);
        logger.info("CRM86==>返回：" + json);
        if (StringUtils.isEmpty(json)) {
            logger.error("客户扩展信息接口返回异常！--》CRM 86");
            return fail("51", RestUtil.ERROR_INTERNAL_MSG);
        }
        logger.debug("CRM getCustExtInfo DONE");
        logger.debug("CRM 86返回json==" + json);
        Map<String, Object> custExtInfoMap = HttpUtil.json2Map(json);
        logger.debug("custExtInfoMap==" + custExtInfoMap);
        if (!StringUtils.isEmpty(custExtInfoMap.get("body"))) {
            // 客户扩展信息查询
            Map<String, Object> custExtInfoBodyMap = HttpUtil.json2Map(custExtInfoMap.get("body").toString());
            logger.info("custExtInfoBodyMap==" + custExtInfoBodyMap);
            if ("N".equals(flag)) {
                return success(custExtInfoBodyMap);
            } else if ("Y".equals(flag)) {
                String lxrUrl = EurekaServer.CRM + "/app/crm/cust/findCustFCiCustContactByCustNo?custNo=" + custNo;
                logger.debug("CRM findCustFCiCustContactByCustNo...");
                String lxrJson = HttpUtil.restGet(lxrUrl, null);
                logger.debug("CRM lxrJson:" + lxrJson);
                logger.debug("CRM findCustFCiCustContactByCustNo DONE");
                if (StringUtils.isEmpty(lxrJson)) {
                    logger.error("联系人列表查询失败！——》CRM 1.8");
                    return fail("52", RestUtil.ERROR_INTERNAL_MSG);
                }
                Map<String, Object> lxrMap = HttpUtil.json2Map(lxrJson);
                List<Map<String, Object>> lxrlist = new ArrayList<Map<String, Object>>();
                if (!StringUtils.isEmpty(lxrMap.get("body"))) {

                    lxrlist = (ArrayList) lxrMap.get("body");
                }
                //联系人数量超过3个，只去前3个
                for (int i = lxrlist.size(); i > 3; i--) {
                    lxrlist.remove(i - 1);
                }
                custExtInfoBodyMap.put("lxrList", lxrlist);
                return success(custExtInfoBodyMap);
            } else {
                return success(custExtInfoBodyMap);
            }
        } else {
            logger.info("客户扩展信息接口返回异常！--》CRM 86");
            JSONObject headJson = (JSONObject) custExtInfoMap.get("head");
            String retMsg = headJson.getString("retMsg");
            String retFlag = headJson.getString("retFlag");
            //此处只返回crm的错误码，故不适用父类的fail方法
            return fail(retFlag, retMsg);
        }

    }

    @Override
    public Map<String, Object> saveAllCustExtInfo(Map requestMap) {
        String url = EurekaServer.CRM + "/app/crm/cust/saveAllCustExtInfo";
        logger.info("CRM85 请求url:" + url);
        Map json = HttpUtil.restPostMap(url, requestMap);
        logger.info("CRM85==>返回：" + json);
        if (StringUtils.isEmpty(json)) {
            logger.error("CRM85修改保存客户所有扩展信息接口返回异常！");
            return fail("51", RestUtil.ERROR_INTERNAL_MSG);
        }
        logger.debug("CRM 85返回json==" + json);
        return json;
    }

    @Override
    public Map<String, Object> saveCustExtInfo(Map requestMap) {
        String url = EurekaServer.CRM + "/app/crm/cust/saveCustExtInfo";
        logger.info("CRM 1 请求url:" + url);
        Map json = HttpUtil.restPostMap(url, requestMap);
        logger.info("CRM1==>返回：" + json);
        if (StringUtils.isEmpty(json)) {
            logger.error("CRM1保存/修改 单位(个人、房产)信息接口返回异常！");
            return fail("51", RestUtil.ERROR_INTERNAL_MSG);
        }
        logger.debug("CRM 1返回json==" + json);
        return json;
    }

    @Override
    public Map<String, Object> saveCustFCiCustContact(Map requestMap) {
        String url = EurekaServer.CRM + "/app/crm/saveCustFCiCustContact";
        logger.info("CRM6 请求url:" + url);
        Map json = HttpUtil.restPostMap(url, requestMap);
        logger.info("CRM6==>返回：" + json);
        if (StringUtils.isEmpty(json)) {
            logger.error("CRM6新增/修改 联系人接口返回异常！");
            return fail("51", RestUtil.ERROR_INTERNAL_MSG);
        }
        logger.debug("CRM 6返回json==" + json);
        return json;
    }

    @Override
    public Map<String, Object> getCrm4CustExtInfo(String custNo, String pageName) {
        String url = EurekaServer.CRM + "/app/crm/cust/getCustExtInfo?custNo=" + custNo + "&pageName=" + pageName;
        Map json = HttpUtil.restGetMap(url, HttpStatus.OK.value());
        logger.debug("CRM4 getCrm4CustExtInfo:" + json);
        if (StringUtils.isEmpty(json)) {
            logger.error("CRM4 查询个人(单位、房产)信息查询失败！");
            return fail("52", RestUtil.ERROR_INTERNAL_MSG);
        }
        return json;
    }

    @Override
    public Map<String, Object> getBankInfo(String cardNo) {
        String url = EurekaServer.CRM + "/app/crm/cust/getBankInfo?cardNo=" + cardNo;
        Map json = HttpUtil.restGetMap(url, HttpStatus.OK.value());
        logger.debug("CRM64 getBankInfo:" + json);
        if (StringUtils.isEmpty(json)) {
            logger.error("CRM64 查询指定银行卡的所有信息！");
            return fail("52", RestUtil.ERROR_INTERNAL_MSG);
        }
        return json;
    }

    @Override
    public Map<String, Object> getBankList() {
        String url = EurekaServer.CRM + "/app/crm/cust/getBankList";
        Map json = HttpUtil.restGetMap(url, HttpStatus.OK.value());
        logger.debug("CRM12 getBankList:" + json);
        if (StringUtils.isEmpty(json)) {
            logger.error("CRM12 查询所有支持的银行列表失败！");
            return fail("52", RestUtil.ERROR_INTERNAL_MSG);
        }
        return json;
    }

    @Override
    public Map<String, Object> fCiCustRealThreeInfo(Map requestMap, String channelNo) {
        String url = EurekaServer.CRM + "/app/crm/cust/fCiCustRealThreeInfo";
        logger.info("CRM66 请求url:" + url);
        Map json = HttpUtil.restPostMap(url, requestMap);
        logger.info("CRM66==>返回：" + json);
        if (StringUtils.isEmpty(json)) {
            logger.error("CRM66 验证并新增实名认证信息返回空！");
            return fail("51", RestUtil.ERROR_INTERNAL_MSG);
        }
        //解析json，获取接口调成功与否
        Map<String, Object> crmResHeadMap = (Map<String, Object>) json.get("head");
        String retMsg = String.valueOf(crmResHeadMap.get("retMsg"));
        String retFlag = String.valueOf(crmResHeadMap.get("retFlag"));
        if (!Objects.equals("00000", retFlag)) {
            return fail(retFlag, retMsg);
        }
        //66接口调通，则查询客户编号
        //从参数中解析出姓名、身份证号
        String custName = String.valueOf(requestMap.get("custName"));
        String certNo = String.valueOf(requestMap.get("certNo"));
        Map smrzMap = queryMerchCustInfo(custName, certNo);
        Map<String, Object> smrzHeadMap = (Map<String, Object>) smrzMap.get("head");
        String smrzRetMsg = String.valueOf(smrzHeadMap.get("retMsg"));
        String smrzRetFlag = String.valueOf(smrzHeadMap.get("retFlag"));
        if (!Objects.equals("00000", smrzRetFlag)) {
            return fail(smrzRetFlag, smrzRetMsg);
        }
        //实名认证信息查询成功获取客户编号
        Map<String, Object> smrzBodyMap = (Map<String, Object>) smrzMap.get("body");
        //获取客户编号
        String custNo = String.valueOf(smrzBodyMap.get("custNo"));
        //获取身份证正反面影像信息；
        /**身份证正反面信息不再处理
         List<AttachFile> fileList=attachFileRepository.findByCustNoAndIdType(custNo);
         if(fileList==null||fileList.size()!=2){
         logger.info("身份证信息完整新查询失败！");
         logger.info("身份证影像信息查询结果："+fileList.size());
         return fail("21","身份证影像信息完整性查询失败！");
         }
         **/
        //判断app_cert_msg表中是否有该用户的信息
        AppCertMsg appCertMsg = appCertMsgRepository.getCardMsgByCertNo(certNo);
        if (appCertMsg == null) {
            //return fail("22", "该用户的身份证有效信息未存储！");
            return json;//查询不到扫描信息，不再调85接口，直接返回。
        }
        //查询DOC53(身份证正面)  DOC54(身份证反面)
        //向crm传输身份证正反面(业务要求，不再上传身份证正反面信息至crm)
        /**
         try {
         for (AttachFile attachFile : fileList) {
         logger.debug("即将传输影像："+custNo+"===>"+attachFile.getAttachType());
         if (Objects.equals(attachFile.getAttachType(), "DOC53")) {
         logger.info("DOC53传输中。。。。");
         fileSignService.sendGrantToCrm(custNo, "", attachFile.getFileName(), "", "2");//身份证正面
         logger.info("DOC53传输成功！");
         } else if (Objects.equals(attachFile.getAttachType(), "DOC54")) {
         logger.info("DOC54传输中。。。。");
         fileSignService.sendGrantToCrm(custNo, "", attachFile.getFileName(), "", "3");//身份证反面
         logger.info("DOC54传输成功！");
         }
         }
         logger.info("身份证正反面传输至crm成功");
         }catch (Exception e){
         logger.info("传输身份证信息至crm失败！");
         logger.info("失败原因："+e.getMessage());
         }
         **/
        //调crm85接口发起保存
        /**
         * 身份证有效期限开始日期	certStrDt	VARCHAR2(10)	否	格式如“2017-02-16”
         身份证有效期限终止日期	certEndDt	VARCHAR2(10)	否	如为“长期”，需填写：“9999-99-99”
         签发机关	certOrga	VARCHAR2(100)	否
         民族	ethnic	VARCHAR2(50)	否	汉字，无字典表
         性别	gender		否	10：男，20：女
         生日	birthDt		否	格式如“2017-02-16”

         */
        HashMap reqMap = new HashMap<String, Object>();
        logger.info("身份证信息查询结果：" + appCertMsg);
        reqMap.put("custNo", custNo);
        reqMap.put("certStrDt", StringUtils.isEmpty(appCertMsg.getAfterCertStrDt()) ? appCertMsg.getCertStrDt() : appCertMsg.getAfterCertStrDt());
        reqMap.put("certEndDt", StringUtils.isEmpty(appCertMsg.getAfterCertEndDt()) ? appCertMsg.getCertEndDt() : appCertMsg.getCertEndDt());
        reqMap.put("certOrga", StringUtils.isEmpty(appCertMsg.getAfterCertOrga()) ? appCertMsg.getCertOrga() : appCertMsg.getAfterCertOrga());
        reqMap.put("ethnic", StringUtils.isEmpty(appCertMsg.getAfterEthnic()) ? appCertMsg.getEthnic() : appCertMsg.getAfterEthnic());
        reqMap.put("birthDt", StringUtils.isEmpty(appCertMsg.getAfterBirthDt()) ? appCertMsg.getBirthDt() : appCertMsg.getAfterBirthDt());
        if (requestMap.containsKey("dataFrom") && !StringUtils.isEmpty(requestMap.get("dataFrom"))) {
            reqMap.put("dataFrom", String.valueOf(requestMap.get("dataFrom")));
        } else {
            reqMap.put("dataFrom", channelNo);
        }
        reqMap.put("gender", StringUtils.isEmpty(appCertMsg.getAfterGender()) ? appCertMsg.getGender() : appCertMsg.getAfterGender());
        reqMap.put("regAddr", StringUtils.isEmpty(appCertMsg.getAfterRegAddr()) ? appCertMsg.getRegAddr() : appCertMsg.getAfterRegAddr());
//        if (!(StringUtils.isEmpty(appCertMsg.getAfterCertStrDt()) && StringUtils.isEmpty(appCertMsg.getAfterCertEndDt()) && StringUtils.isEmpty(appCertMsg.getAfterCertOrga()) && StringUtils.isEmpty(appCertMsg.getAfterEthnic()) && StringUtils.isEmpty(appCertMsg.getAfterBirthDt()))) {
//            reqMap.put("certStrDt", "");
//            reqMap.put("certEndDt", "");
//
//        }
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
        Map<String, Object> saveToCrmMap = saveAllCustExtInfo(reqMap);
        /**
         * 判断扩展信息保存成功与否
         */
        Map<String, Object> crm85HeadMap = (Map<String, Object>) saveToCrmMap.get("head");
        String crm85RetMsg = String.valueOf(crm85HeadMap.get("retMsg"));
        String crm85RetFlag = String.valueOf(crm85HeadMap.get("retFlag"));
        if (!Objects.equals("00000", crm85RetFlag)) {
            return fail(crm85RetFlag, crm85RetMsg);
        }
        /**
         //信息传输成功后，将本地身份证信息表的身份证删除与否字段设置成1（删除）
         appCertMsg.setDeleteFlag("1");
         appCertMsgRepository.save(appCertMsg);
         **/
        //date:2017-3-2日，不在至为1，而是直接删除数据，数据库中将设为联合唯一性
        appCertMsgRepository.delete(appCertMsg);
        /**(本地文件不删除)
         //删除本地的doc53和doc54的影像
         for(AttachFile attachFile :fileList) {
         File file = new File(attachFile.getFileName());
         try {
         logger.debug("删除文件"+attachFile.getId()+"==>"+attachFile.getAttachType());
         // 路径为文件且不为空则进行删除
         if (file.isFile() && file.exists()) {
         file.delete();
         }
         // 删除本地影像文件记录
         attachFileRepository.delete(attachFile.getId());
         } catch (Exception e) {
         e.printStackTrace();
         }
         }
         logger.info("本地身份证正反面文件删除成功！流程处理结束！");
         //
         **/
        // 如果用户上传了原绑定手机号，则直接进行用户绑定手机号修改，修改为实名手机号
        if (!StringUtils.isEmpty(requestMap.get("bindMobile"))) {
            StringBuffer bindUrl = new StringBuffer(EurekaServer.UAUTH + "/app/uauth/updateMobile");
            HashMap uma = new HashMap();
            uma.put("verifyNo", "0");
            uma.put("oldMobile", EncryptUtil.simpleEncrypt(String.valueOf(requestMap.get("bindMobile"))));
            uma.put("newMobile", EncryptUtil.simpleEncrypt(String.valueOf(requestMap.get("mobile"))));
            uma.put("userId", EncryptUtil.simpleEncrypt(String.valueOf(requestMap.get("userId"))));
            // logger.info();
            Map<String, Object> bindResult = HttpUtil.restPutMap(bindUrl.toString(), uma);
            logger.info("统一认证：1.18(PUT) 修改绑定手机号 返回：" + bindResult);
            String isUpdateMobileSuccess = "true";
            if (!HttpUtil.isSuccess(bindResult)) {
                // 修改绑定手机号如果失败，不返回前端，只记录。不影响业务流程。
                logger.error("修改实名手机号为绑定手机号失败" + requestMap.get("userId"));
                //return bindResult;
                isUpdateMobileSuccess = "false";
            }
            ((Map<String, Object>)json.get("body")).put("isUpdateMobileSuccess", isUpdateMobileSuccess);
        }
        return json;
    }

    @Override
    public Map<String, Object> queryCustRealInfoByCustNo(String custNo) {
        String url = EurekaServer.CRM + "/app/crm/cust/queryCustRealInfoByCustNo?custNo=" + custNo;
        Map json = HttpUtil.restGetMap(url, HttpStatus.OK.value());
        logger.debug("CRM26 queryCustRealInfoByCustNo:" + json);
        if (StringUtils.isEmpty(json)) {
            logger.error("CRM26 查询实名认证信息失败！");
            return fail("52", RestUtil.ERROR_INTERNAL_MSG);
        }
        return json;
    }

    @Override
    public Map<String, Object> queryMerchCustInfo(String custName, String certNo) {
        String url = EurekaServer.CRM + "/app/crm/cust/queryMerchCustInfo?custName=" + custName + "&certNo=" + certNo;
        String jsonStr = HttpUtil.restGet(url);
        logger.debug("CRM13 queryMerchCustInfo:" + jsonStr);
        if (StringUtils.isEmpty(jsonStr)) {
            logger.error("CRM13 查询实名认证客户信息失败！");
            return fail("52", RestUtil.ERROR_INTERNAL_MSG);
        }
        return HttpUtil.json2DeepMap(jsonStr);
    }

    @Override
    public Map<String, Object> findMerchStore(String merchNo, String userId) {
        String url = EurekaServer.CRM + "/app/crm/findMerchStore?merchNo=" + merchNo + "&userId=" + userId;
        Map json = HttpUtil.restGetMap(url, HttpStatus.OK.value());
        logger.debug("CRM50 findMerchStore:" + json);
        if (StringUtils.isEmpty(json)) {
            logger.error("CRM50 查询商户对应门店列表失败！");
            return fail("52", RestUtil.ERROR_INTERNAL_MSG);
        }
        return json;
    }

    @Override
    public Map<String, Object> getMerchs(String userId) {
        String url = EurekaServer.CRM + "/app/crm/cust/getMerchs?userId=" + userId;
        Map json = HttpUtil.restGetMap(url, HttpStatus.OK.value());
        logger.debug("CRM24 getMerchs:" + json);
        if (StringUtils.isEmpty(json)) {
            logger.error("CRM24 查询商户列表失败！");
            return fail("52", RestUtil.ERROR_INTERNAL_MSG);
        }
        return json;
    }

    @Override
    public Map<String, Object> queryPointByCustNo(String custNo) {
        String url = EurekaServer.CRM + "/app/crm/cust/queryPointByCustNo?custNo=" + custNo;
        Map json = HttpUtil.restGetMap(url, HttpStatus.OK.value());
        logger.debug("CRM90 queryPointByCustNo:" + json);
        if (StringUtils.isEmpty(json)) {
            logger.error("CRM90 查询客户当前集团总积分失败！");
            return fail("52", RestUtil.ERROR_INTERNAL_MSG);
        }
        Map<String, Object> result = new HashedMap();
        if (!HttpUtil.isSuccess(json)) {
            logger.info("返回结果result：" + json);
            return json;
        }
        Map<String, Object> map = (Map) json.get("body");
        String totalPoints = StringUtils.isEmpty(map.get("totalPoints")) ? "" : map.get("totalPoints").toString();
        result.put("totalPoints", totalPoints);
        return success(result);
    }

    @Override
    public Map<String, Object> getLoanCdeByTagId(String tagId) {
        String url = EurekaServer.CRM + "/app/crm/cust/getLoanCodeByTagId?tagId=" + tagId;
        logger.debug("CRM87 getLoanCodeByTagId ==> " + url);
        Map<String, Object> resultMap = HttpUtil.restGetMap(url, HttpStatus.OK.value());
        logger.debug("CRM87 getLoanCodeByTagId <== " + resultMap);
        if (StringUtils.isEmpty(resultMap)) {
            logger.error("CRM87 根据标签ID查询贷款品种失败！");
        }
        if (HttpUtil.isSuccess(resultMap)) {
            List<Map<String, Object>> listMap = (List<Map<String, Object>>)((Map<String, Object>)resultMap.get("body")).get("info");
            logger.debug("listMap=" + listMap);
            List<Map<String, Object>> result = listMap.stream().filter(map->map.get("typGrp").equals("02")).collect(Collectors.toList());
            result.forEach(map -> map.remove("typGrp"));
            Map<String, Object> info = new HashedMap();
            info.put("info", result);
            resultMap.put("body", info);
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getCustISExistsInvitedCauseTag(String cusName, String certNo, String phoneNumber, String cardNo) {
        logger.info("有效邀请原因service层");
        return HttpUtil.restGetMap(EurekaServer.CRM + "/app/crm/cust/getCustISExistsInvitedCauseTag?"
                + "custName=" + cusName
                + "&certNo=" + certNo
                + "&phonenumber=" + phoneNumber
                + "&cardNo=" + cardNo, HttpStatus.OK.value());
    }

    @Override
    public Map<String, Object> findAreaCodes(String areaCode) {
        return HttpUtil.restGetMap(EurekaServer.CRM + "/pub/crm/findAreaCodes?areaCode=" + areaCode, HttpStatus.OK.value());
    }

    /**
     * app/appserver/crm/cust/getCustISExistsInvitedCauseTag
     * 查询邀请原因
     * app/crm/cust/getCustTag
     * 无邀请原因用户查询有效标签
     * app/appmanage/citybean/checkCitySmrz
     * 无标签用户 查询城市是否允许
     *
     * @param custName
     * @param idNo
     * @param mobile
     * @param idTyp
     * @param provinceCode
     * @param cityCode
     * @return
     */
    @Override
    public Map<String, Object> getIfShhSmrz(String custName, String idNo, String mobile, String idTyp, String cardNo, String provinceCode, String cityCode) {
        Map<String, Object> yqCause = this.getCustISExistsInvitedCauseTag(custName, idNo, mobile, cardNo);
        logger.info("crm_31 查询未实名认证客户是否存在有效邀请原因 返回：" + yqCause);
        //解析邀请原因
        if (!RestUtil.isSuccess(yqCause)) {
            logger.info("crm_31接口调用失败，返回网络通讯异常！==>" + yqCause);
            return fail(RestUtil.ERROR_INTERNAL_CODE, RestUtil.ERROR_INTERNAL_MSG);
        }
        Map<String, Object> body = RestUtil.getObject(yqCause, "body");
        String flag = String.valueOf(body.get("isExits"));
        HashMap<String, Object> hm = new HashMap<String, Object>();

        if (Objects.equals("Y", flag)) {
            logger.debug("满足crm_31接口查询，有邀请原因，直接返回Y");
            hm.put("flag", flag);
            hm.put("msg", "");
        } else {
            logger.debug("crm_31接口返回无邀请原因，查询crm_46接口");
            StringBuffer getCustTagUrl = new StringBuffer(EurekaServer.CRM).append("/app/crm/cust/getCustTag?")
                    .append("custName=").append(custName)
                    .append("&idNo=").append(idNo);
            if (!StringUtils.isEmpty(idTyp)) {
                getCustTagUrl.append("&idTyp=").append(idTyp);
            }
            Map<String, Object> crm46 = HttpUtil.restGetMap(getCustTagUrl.toString(), HttpStatus.OK.value());
            logger.debug("crm_46接口返回" + crm46);
            if (!RestUtil.isSuccess(crm46)) {
                logger.info("crm_46接口调用失败，返回网络通讯异常！==>" + crm46);
                return fail(RestUtil.ERROR_INTERNAL_CODE, RestUtil.ERROR_INTERNAL_MSG);
            }
            //解析标签
            List list = (ArrayList) crm46.get("body");
            if (list != null && list.size() > 0) {
                logger.debug("满足crm_46接口查询的要求，返回Y");
                hm.put("flag", "Y");
                hm.put("msg", "");
            } else {
                //查询城市允许范围
                Map<String, Object> appmanage = HttpUtil.restGetMap(EurekaServer.APPMANAGE + "/app/appmanage/ citybean/ checkCitySmrz?"
                        + "provinceCode=" + provinceCode
                        + "&cityCode=" + cityCode, HttpStatus.OK.value());
                logger.info("app_8.10接口从appmanage查询城市是否允许准入结果：" + appmanage);
                if (!RestUtil.isSuccess(appmanage)) {
                    logger.info("app_8.10接口接口调失败，返回网络通讯异常！" + appmanage);
                    return fail(RestUtil.ERROR_INTERNAL_CODE, RestUtil.ERROR_INTERNAL_MSG);
                } else {
                    String thisflag = String.valueOf(((Map<String, Object>) appmanage.get("body")).get("IsExist"));
                    if ("Y".equals(thisflag)) {
                        hm.put("flag", "Y");
                        hm.put("msg", "");
                    } else {
                        hm.put("flag", "N");
                        hm.put("msg", "您所在的城市暂未开放此业务，敬请期待！");
                    }
                }
            }
        }
        return success(hm);
    }

    @Override
    public Map<String, Object> findUserIdByStoreNo(String storeNo) {
        String url = EurekaServer.CRM + "/pub/crm/findUserIdByStoreNo?storeNo=" + storeNo;
        logger.debug("CRM93 findUserIdByStoreNo ==> " + url);
        Map<String, Object> resultMap = HttpUtil.restGetMap(url, HttpStatus.OK.value());
        logger.debug("CRM93 findUserIdByStoreNo <== " + resultMap);
        if (StringUtils.isEmpty(resultMap)) {
            logger.error("CRM93 根据门店编号查询销售代表id失败！");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> findDmAreaInfo(String areaCode, String flag) {

//        String url = EurekaServer.CRM + "/pub/crm/findDmAreaInfo?areaCode=" + areaCode + "&flag=" + flag;
        StringBuffer url = new StringBuffer(EurekaServer.CRM).append("/pub/crm/findDmAreaInfo?")
                .append("flag=").append(flag);
        if (!StringUtils.isEmpty(areaCode)) {
            url.append("&areaCode=").append(areaCode);
        }
        logger.debug("CRM94 findDmAreaInfo ==> " + url);
        Map<String, Object> resultMap = HttpUtil.restGetMap(url.toString(), HttpStatus.OK.value());
        logger.debug("CRM94 findDmAreaInfo <== " + resultMap);
        if (StringUtils.isEmpty(resultMap)) {
            logger.error("CRM94 查询行政编码级联信息失败！");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getMsgByScan(String userId, String scanCode) {
        Map<String, Object> returnMap = new HashMap<>();
        // 查询用户实名信息
        Map<String, Object> queryPerCustInfo = crmController.queryPerCustInfo(userId);
        logger.info("CRM17 查询实名认证信息 <== " + queryPerCustInfo);
        if (!HttpUtil.isSuccess(queryPerCustInfo)) {
            return queryPerCustInfo;
        }
        // 用户已实名认证
        HashMap<String, Object> queryPerCustInfoBody = (HashMap<String, Object>) queryPerCustInfo.get("body");
        returnMap.putAll(queryPerCustInfoBody);
        // 查询用户准入资格
        if (StringUtils.isEmpty(queryPerCustInfoBody.get("custName"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "crm返回用户姓名为空");
        }
        if (StringUtils.isEmpty(queryPerCustInfoBody.get("certNo"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "crm返回用户身份证号为空");
        }
        if (StringUtils.isEmpty(queryPerCustInfoBody.get("custNo"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "crm返回用户编号为空");
        }
        Map<String, Object> isPassRequest = new HashMap<>();
        isPassRequest.put("custName", queryPerCustInfoBody.get("custName"));
        isPassRequest.put("certNo", queryPerCustInfoBody.get("certNo"));
        Map<String, Object> custIsPass = crmController.getCustIsPass(isPassRequest);
        logger.info("CRM28 查询用户是否准入 <== " + custIsPass);
        if (StringUtils.isEmpty(custIsPass) || !HttpUtil.isSuccess(custIsPass)) {
            return custIsPass;
        }
        HashMap<String, Object> custIsPassBody = (HashMap<String, Object>) custIsPass.get("body");
        String isPass = (String) custIsPassBody.get("isPass");
        if ("-1".equals(isPass)) {
            return fail("36", "此账号无贷款权限，详情请拨打4000187777");
        }
        returnMap.putAll(custIsPassBody);

        // 额度申请校验
        Map<String, Object> checkEdMap = cmisApplService.checkEdAppl("", "", userId);
        if (!HttpUtil.isSuccess(checkEdMap)) {
            return checkEdMap;
        }
        Map<String, Object> checkEdBody = (Map<String, Object>) checkEdMap.get("body");
        returnMap.put("crdNorAvailAmt", checkEdBody.get("crdNorAvailAmt"));
        returnMap.put("crdComAvailAnt", checkEdBody.get("crdComAvailAnt"));
        returnMap.put("crdSts", checkEdBody.get("crdSts"));
        returnMap.put("outSts", checkEdBody.get("outSts"));
        returnMap.put("crdSeq", checkEdBody.get("crdSeq"));
        returnMap.put("crdAmt", checkEdBody.get("crdAmt"));
        returnMap.put("crdNorAmt", checkEdBody.get("crdNorAmt"));
        returnMap.put("flag", checkEdBody.get("flag"));
        returnMap.put("applType", checkEdBody.get("applType"));
        returnMap.put("outStsName", checkEdBody.get("outStsName"));
        returnMap.put("crdSeq", checkEdBody.get("crdSeq"));
        returnMap.put("expectCredit", checkEdBody.get("expectCredit"));
        returnMap.put("rejectRsn", checkEdBody.get("rejectRsn"));

        if ("C".equals(returnMap.get("level"))) {
            returnMap.put("edActiveCheck", "Y");
        } else {
            returnMap.put("edActiveCheck", "N");
        }

        // 获取用户的贷款品种，放入返回结果
        String url = EurekaServer.CRM + "/app/crm/cust/getCustLoanCode?custNo=" + queryPerCustInfoBody.get("custNo") + "&typGrp=";
        logger.info("CRM 30 getCustLoanCode ==> " + url);
        String getCustLoanCodeJson = HttpUtil.restGet(url);
        logger.info("CRM 30 getCustLoanCode <== " + getCustLoanCodeJson);
        HashMap<String, Object> getCustLoanCode = new HashMap<>();
        ArrayList<HashMap<String, Object>> info = new ArrayList<>();
        if (!StringUtils.isEmpty(getCustLoanCodeJson) && HttpUtil.isSuccess(getCustLoanCodeJson)) {
            getCustLoanCode = HttpUtil.json2DeepMap(getCustLoanCodeJson);
            info = (ArrayList<HashMap<String, Object>>) ((HashMap<String, Object>) getCustLoanCode.get("body")).get("info");
        }
        if (info.isEmpty()) {
            returnMap.put("custTypCde", Collections.EMPTY_LIST);
        } else {
            returnMap.put("custTypCde", info.stream().map(loanInfo -> (String) loanInfo.get("typCde")).collect(Collectors.toList()));
        }


        // 检查贷款品种
        // 链接设置返回格式{"cashLink":["https://www.haiercash.com/","haierCashabc"],"branchStoreLink":["https://www.haiercash.com/perbank/"]}
        String linkSetResult = appManageService.getDictDetailByDictCde("linkSet");
        logger.debug("获取链接设置：" + linkSetResult);
        if (StringUtils.isEmpty(linkSetResult)) {
            return fail("34", "现金贷与伞下店网址未配置");
        }
        Map<String, Object> linkMap = HttpUtil.json2DeepMap(linkSetResult);
        List<String> cashLinkList = (List<String>) linkMap.get("cashLink");
        List<String> branchStoreLinkList = (List<String>) linkMap.get("branchStoreLink");

        if (cashLinkList.stream().filter(cashLink -> scanCode.equals(cashLink)).count() > 0) {
            // 现金贷
            returnMap.put("type", "01");
            return success(returnMap);
        } else if (branchStoreLinkList.stream().filter(branchLink -> scanCode.equals(branchLink)).count() > 0) {
            // 伞下店
            returnMap.put("type", "02");
            return success(returnMap);
        }
        if (!scanCode.contains("userId")) {
            // 非现金贷和伞下店，必传userId
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "扫描编码未传送商户id");
        }


        if (scanCode.contains("goods")) {
            // 商品类型
            returnMap.put("type", "11");
            String storeUserId = scanCode.substring(scanCode.indexOf("userId") + 6,
                    scanCode.indexOf("goods") < scanCode.indexOf("userId") ? scanCode.length() : scanCode.indexOf("goods"));
            returnMap.put("storeUserId", storeUserId);
            String goodsId = scanCode.substring(scanCode.indexOf("goods") + 5,
                    scanCode.indexOf("userId") < scanCode.indexOf("goods") ? scanCode.length() : scanCode.indexOf("userId"));
            Map<String, Object> goodsByCodeResult = pubController.getGoodsAddImgByCode(goodsId);
            if (!HttpUtil.isSuccess(goodsByCodeResult)) {
                return fail("35", "查询商品详情失败");
            }
            HashMap<String, Object> goodsInfoBody = (HashMap<String, Object>) goodsByCodeResult.get("body");
            returnMap.putAll(goodsInfoBody);
            /*String isGetCommonTypCde = appManageService.getDictDetailByDictCde("getCommonTypCde");
            if ("Y".equals(isGetCommonTypCde)) {
                if (StringUtils.isEmpty(goodsInfoBody.get("loan"))) {
                    return success(returnMap);
                }
                ArrayList<HashMap<String, Object>> loans = (ArrayList<HashMap<String, Object>>) goodsInfoBody.get("loan");
                if (loans == null || loans.isEmpty()) {
                    returnMap.put("commonLoan", Collections.EMPTY_LIST);
                } else {
                    List custTypCdeList = (List) returnMap.get("custTypCde");
                    List<HashMap<String, Object>> commonLoan = loans.stream().filter(loan -> custTypCdeList.contains(loan.get("loanCode"))).collect(Collectors.toList());
                    returnMap.put("commonLoan", commonLoan);
                }
            }*/
            return success(returnMap);
        } else if (scanCode.contains("merchant")) {
            // 商户类型
            returnMap.put("type", "12");
            String storeUserId = scanCode.substring(scanCode.indexOf("userId") + 6,
                    scanCode.indexOf("merchant") < scanCode.indexOf("userId") ? scanCode.length() : scanCode.indexOf("merchant"));
            String storeId = scanCode.substring(scanCode.indexOf("merchant") + 8,
                    scanCode.indexOf("userId") < scanCode.indexOf("merchant") ? scanCode.length() : scanCode.indexOf("userId"));
            returnMap.put("storeUserId", storeUserId);
            returnMap.put("storeId", storeId);
        } else if (scanCode.contains("store")) {
            // 门店类型
            returnMap.put("type", "13");
            String storeUserId = scanCode.substring(scanCode.indexOf("userId") + 6,
                    scanCode.indexOf("store") < scanCode.indexOf("userId") ? scanCode.length() : scanCode.indexOf("store"));
            returnMap.put("storeUserId", storeUserId);
            String merchId = scanCode.substring(scanCode.indexOf("store") + 5,
                    scanCode.indexOf("userId") < scanCode.indexOf("store") ? scanCode.length() : scanCode.indexOf("userId"));

            Map<String, Object> storeResult = crmController.getStore(merchId);
            logger.info("CRM 10 getStores <== " + storeResult);
            if (HttpUtil.isSuccess(storeResult)) {
                returnMap.putAll((HashMap) storeResult.get("body"));
            } else {
                return storeResult;
            }
            returnMap.put("addGoodsSwitch", appManageService.getDictDetailByDictCde("addGoodsSwitch"));
        }
        return success(returnMap);
    }

    /**
     * 商品和个人贷款品种取交集接口
     *
     * @param userId
     * @param goodsCode
     * @return 贷款品种和商品详情
     */
    @Override
    public Map<String, Object> getCommonApplType(String userId, String goodsCode) {
        Map<String, Object> returnMap = new HashMap<>();

        // 查询用户实名信息
        Map<String, Object> queryPerCustInfo = crmController.queryPerCustInfo(userId);
        logger.info("CRM17 查询实名认证信息 <== " + queryPerCustInfo);
        if (!HttpUtil.isSuccess(queryPerCustInfo)) {
            return queryPerCustInfo;
        }
        // 用户已实名认证
        // 查询用户贷款品种
        HashMap<String, Object> queryPerCustInfoBody = (HashMap<String, Object>) queryPerCustInfo.get("body");
        if (StringUtils.isEmpty(queryPerCustInfoBody.get("custNo"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "crm返回用户编号为空");
        }

        String url = EurekaServer.CRM + "/app/crm/cust/getCustLoanCode?custNo=" + userId + "&typGrp=";
        logger.info("CRM 30 getCustLoanCode ==> " + url);
        String getCustLoanCodeJson = HttpUtil.restGet(url);
        logger.info("CRM 30 getCustLoanCode <== " + getCustLoanCodeJson);
        HashMap<String, Object> getCustLoanCode = new HashMap<>();
        ArrayList<HashMap<String, Object>> info = new ArrayList<>();
        if (!StringUtils.isEmpty(getCustLoanCodeJson) && HttpUtil.isSuccess(getCustLoanCodeJson)) {
            getCustLoanCode = HttpUtil.json2DeepMap(getCustLoanCodeJson);
            info = (ArrayList<HashMap<String, Object>>) ((HashMap<String, Object>) getCustLoanCode.get("body")).get("info");
        }

        // 查询商品详情
        Map<String, Object> goodsByCodeResult = pubController.getGoodsByCode(goodsCode);
        if (!HttpUtil.isSuccess(goodsByCodeResult)) {
            return fail("35", "查询商品详情失败");
        }
        HashMap<String, Object> goodsInfoBody = (HashMap<String, Object>) goodsByCodeResult.get("body");
        returnMap.putAll(goodsInfoBody);
        if (StringUtils.isEmpty(goodsInfoBody.get("loan"))) {
            return success(returnMap);
        }
        ArrayList<HashMap<String, Object>> loans = (ArrayList<HashMap<String, Object>>) goodsInfoBody.get("loan");
        if (loans == null || loans.isEmpty()) {
            returnMap.put("commonLoan", Collections.EMPTY_LIST);
        } else {
            List custTypCdeList = info.stream().map(eachInfo -> eachInfo.get("typCde")).collect(Collectors.toList());
            List<HashMap<String, Object>> commonLoan = loans.stream().filter(loan -> custTypCdeList.contains(loan.get("loanCode"))).collect(Collectors.toList());
            returnMap.put("commonLoan", commonLoan);
        }
        return returnMap;
    }

    @Override
    public Map<String, Object> coopMerchs(String deptId, Integer page, Integer pageNum) {

        StringBuffer url = new StringBuffer(EurekaServer.CRM).append("/app/crm/cust/coopMerchs?")
                .append("deptId=").append(deptId)
                .append("&page=").append(page)
                .append("&pageNum=").append(pageNum);

        logger.debug("CRM83 coopMerchs ==> " + url);
        Map<String, Object> resultMap = HttpUtil.restGetMap(url.toString(), HttpStatus.OK.value());
        logger.debug("CRM83 coopMerchs <== " + resultMap);
        return resultMap;
    }

    @Override
    public Map<String, Object> getCustIsPass(Map<String, Object> params) {
        String url = EurekaServer.CRM + "/app/crm/cust/getCustIsPass";
        String paramUrl = FormatUtil.putParam2Url(url, params);
        logger.debug("CRM28 getCustIsPass ==> " + paramUrl);
        String resultJson = HttpUtil.restGet(paramUrl);
        logger.debug("CRM28 getCustIsPass <== " + resultJson);
        return HttpUtil.json2DeepMap(resultJson);
    }

    @Override
    public Map<String, Object> getBankCard(String custNo) {
        String url = EurekaServer.CRM + "/app/crm/cust/getBankCard?custNo=" + custNo;
        logger.debug("CRM 37 getBankCard ==>" + url);
        Map<String, Object> resultMap = HttpUtil.restGetMap(url);
        if (resultMap == null || resultMap.isEmpty()) {
            return fail(RestUtil.ERROR_INTERNAL_CODE, "CRM获取银行卡失败");
        }
        logger.debug("CRM 37 getBankCard <==" + resultMap);
        return resultMap;
    }

    @Override
    public Map<String, Object> getCustBankCardByCardNo(String cardNo) {
        String url = EurekaServer.CRM + "/app/crm/cust/getCustBankCardByCardNo?cardNo=" + cardNo;
        logger.debug("CRM 27 getBankCard ==>" + url);
        Map<String, Object> resultMap = HttpUtil.restGetMap(url);
        if (resultMap == null || resultMap.isEmpty()) {
            return fail(RestUtil.ERROR_INTERNAL_CODE, "CRM获取银行卡失败");
        }
        logger.debug("CRM 27 getBankCard <==" + resultMap);
        return resultMap;
    }

    @Override
    public Map<String, Object> getCardInfo(String custNo, String cardNo) {
        if (StringUtils.isEmpty(custNo) && StringUtils.isEmpty(cardNo)) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "客户编号银行卡号不能都为空");
        }
        if (StringUtils.isEmpty(cardNo)) {
            Map<String, Object> bankCard = this.getBankCard(custNo);
            if (!HttpUtil.isSuccess(bankCard)) {
                return bankCard;
            }
            Map<String, Object> infoMap = (Map<String, Object>) bankCard.get("body");
            List<Map<String, Object>> bankList = (List<Map<String, Object>>) infoMap.get("info");
            if (bankList == null || bankList.isEmpty()) {
                return fail("34", "该用户不存在银行卡信息");
            }
            for (Map<String, Object> bank : bankList) {
                // 银行卡号
                if ("Y".equals(bank.get("isDefaultCard"))) {
                    return success(bank);
                }
            }
            return success(bankList.get(0));
        } else {
            return this.getCustBankCardByCardNo(cardNo);
        }
    }

}
