package com.haiercash.payplatform.service.impl;

import com.haiercash.core.lang.Convert;
import com.haiercash.payplatform.common.entity.LoanType;
import com.haiercash.payplatform.config.ShunguangConfig;
import com.haiercash.payplatform.config.StorageConfig;
import com.haiercash.payplatform.pc.cashloan.service.CashLoanService;
import com.haiercash.payplatform.service.AppServerService;
import com.haiercash.payplatform.service.CrmService;
import com.haiercash.payplatform.service.CustExtInfoService;
import com.haiercash.payplatform.utils.EncryptUtil;
import com.haiercash.spring.redis.RedisUtils;
import com.haiercash.spring.rest.IResponse;
import com.haiercash.spring.rest.common.CommonResponse;
import com.haiercash.spring.service.BaseService;
import com.haiercash.spring.utils.ConstUtil;
import com.haiercash.spring.utils.HttpUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.stream.FileImageOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class CustExtInfoServiceImpl extends BaseService implements CustExtInfoService {
    public Log logger = LogFactory.getLog(getClass());
    @Autowired
    private AppServerService appServerService;
    @Autowired
    private CrmService crmService;
    @Autowired
    private CashLoanService cashLoanService;
    @Autowired
    private ShunguangConfig shunguangConfig;
    @Autowired
    private StorageConfig storageConfig;

    @Override
    public Map<String, Object> getAllCustExtInfoAndDocCde(String token, String channel, String channelNo) throws Exception {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        List<JSONObject> resultList = new ArrayList<JSONObject>();

        String typCde = "";//贷款品种

        Map<String, Object> allCustExtInfo = getAllCustExtInfo(token, channel, channelNo);
//        Map<String, Object> allCustExtInfoHeadMap = (Map<String, Object>) allCustExtInfo.get("head");
//        String allCustExtInfotMsg =  (String) allCustExtInfoHeadMap.get("retMsg");
//        String allCustExtreflagMsg =  (String) allCustExtInfoHeadMap.get("retFlag");
//        if("C8602".equals(allCustExtreflagMsg) || "C1109".equals(allCustExtreflagMsg)){//无客户信息，正常
//            return success();
//        }
//        if(!ifError(allCustExtInfoHeadMap)){
//            return fail(ConstUtil.ERROR_CODE, allCustExtInfotMsg);
//        }
        if ("46".equals(channelNo)) {
            typCde = shunguangConfig.getTypCde();//贷款品种
        } else {
            //查询贷款品种类型
        }
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("channel", channel);
        paramMap.put("channelNo", channelNo);
        paramMap.put("typCde", typCde);
        Map<String, Object> pLoanTypImagesMap = appServerService.pLoanTypImages(token, paramMap);
        Map pLoanTypImagesHeadMap = (Map<String, Object>) pLoanTypImagesMap.get("head");
        String resultmapFlag = (String) pLoanTypImagesHeadMap.get("retFlag");
        String pLoanTypImagesretMsg = (String) pLoanTypImagesHeadMap.get("retMsg");
        if (!ifError(pLoanTypImagesHeadMap)) {
            return fail(ConstUtil.ERROR_CODE, pLoanTypImagesretMsg);
        }
        //缓存数据获取
        Map<String, Object> cacheMap = RedisUtils.getExpireMap(token);
        String custNo = (String) cacheMap.get("custNo");
//        String custNo = "C201607151029101937960";
        Map<String, Object> paramYXMap = new HashMap<String, Object>();
        //获取贷款类型查询客户所有影像ID
//        JSONObject pLoanTypImagesBodyjson = new JSONObject(pLoanTypImagesMap.get("body"));
        List<Map<String, String>> list = (List<Map<String, String>>) pLoanTypImagesMap.get("body");
        for (int i = 0; i < list.size(); i++) {
            List<JSONObject> resultList_ = new ArrayList<JSONObject>();
            String docCde = list.get(i).get("docCde");//影像代码
            String docDesc = list.get(i).get("docDesc");//影像名称
            paramYXMap.put("attachType", docCde);
            paramYXMap.put("custNo", custNo);
            paramYXMap.put("channel", channel);
            paramYXMap.put("channelNo", channelNo);
            Map<String, Object> stringObjectMap = appServerService.attachTypeSearchPerson(token, paramYXMap);
            Map<String, Object> stringObjectMapHeadMap = (Map<String, Object>) stringObjectMap.get("head");
            String stringObjectMapMsg = (String) stringObjectMapHeadMap.get("retMsg");
            if (!ifError(stringObjectMapHeadMap)) {
                return fail(ConstUtil.ERROR_CODE, stringObjectMapMsg);
            }
            List<Map<String, Object>> list_ = (List<Map<String, Object>>) stringObjectMap.get("body");
            if (list_.size() > 0) {
                for (int j = 0; j < list_.size(); j++) {
                    int id = (int) list_.get(j).get("id");
                    Map<String, Object> paramYXbyIDMap = new HashMap<String, Object>();
                    //TODO
                    paramYXbyIDMap.put("id", id);
                    paramYXbyIDMap.put("channel", channel);
                    paramYXbyIDMap.put("channelNo", channelNo);
                    Map<String, Object> filePathByFileId = appServerService.getFilePathByFileId(token, paramYXbyIDMap);
                    Map<String, Object> filePathByFileIdHeadMap = (Map<String, Object>) filePathByFileId.get("head");
                    String filePathByFileIdMsg = (String) filePathByFileIdHeadMap.get("retMsg");

                    if (!ifError(filePathByFileIdHeadMap)) {
                        return fail(ConstUtil.ERROR_CODE, filePathByFileIdMsg);
                    }
                    Map<String, Object> bodyJSONObjectMap = (Map<String, Object>) filePathByFileId.get("body");
//                JSONObject bodyJSONObject = new JSONObject(filePathByFileId.get("body"));
                    String filePath = (String) bodyJSONObjectMap.get("filePath");
                    if (filePath == null || "".equals(filePath)) {
                        return fail(ConstUtil.ERROR_CODE, "图片路径为空");
                    }
//                filePath = filePath.replace(baseSharePath, "");
                    JSONObject resultJson_ = new JSONObject();
                    resultJson_.put("id", id);//影像ID
                    resultJson_.put("filePath", filePath);//图片地址
                    resultList_.add(resultJson_);
//
//                j= list_.size()-1;
                }
                //////
//            JSONObject resultJson_ = new JSONObject();
//            resultJson_.put("id", 9999999);//影像ID
//            resultJson_.put("filePath", "/A.jpg");//图片地址
//            resultList_.add(resultJson_);
                //////


            }
//            else{
            JSONObject resultJson = new JSONObject();
            resultJson.put("docCde", docCde);//影像代码
            resultJson.put("docDesc", docDesc);//影像名称
            resultJson.put("urlList", resultList_);//地址List
            resultList.add(resultJson);
//                continue;
//            }
        }
        resultMap.put("CustExtInfoMap", (Map<String, Object>) allCustExtInfo.get("body"));
        resultMap.put("docList", resultList);
        return success(resultMap);
    }

    private boolean ifError(Map<String, Object> map) {
        boolean ifError = true;
        String retFlag = (String) map.get("retFlag");
        if (!"00000".equals(retFlag)) {
            ifError = false;
        }
        return ifError;
    }

    @Override
    public Map<String, Object> getAllCustExtInfo(String token, String channel, String channelNo) throws Exception {
        logger.info("*********查询个人扩展信息**************开始");
        Map<String, Object> redisMap = null;
        Map<String, Object> paramMap = new HashMap<String, Object>();
        //参数非空判断
        if (token.isEmpty()) {
            logger.info("token为空");
            return fail(ConstUtil.ERROR_CODE, "参数token为空!");
        }
        if (channel.isEmpty()) {
            logger.info("channel为空");
            return fail(ConstUtil.ERROR_CODE, "参数channel为空!");
        }
        if (channelNo.isEmpty()) {
            logger.info("channelNo为空");
            return fail(ConstUtil.ERROR_CODE, "参数channelNo为空!");
        }
        //缓存数据获取
        Map<String, Object> cacheMap = RedisUtils.getExpireMap(token);
        if (cacheMap == null || "".equals(cacheMap)) {
            logger.info("Redis数据获取失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        // 总入口需查询客户信息数据
        String custNo = (String) cacheMap.get("custNo");
//        String custNo = "C201506300921381093450";
        paramMap.put("custNo", custNo);
        paramMap.put("flag", "Y");
        paramMap.put("channelNo", channelNo);
        paramMap.put("channel", channel);
        Map<String, Object> resultmap = appServerService.getAllCustExtInfo(token, paramMap);
//        if (resultmap == null){
//            String retMsg = ConstUtil.ERROR_INFO;
//            return fail(ConstUtil.ERROR_CODE, retMsg);
//        }
//        Map resultmapjsonMap = (Map<String, Object>) resultmap.get("head");
//        String resultmapFlag = (String) resultmapjsonMap.get("retFlag");
//        if(!"00000".equals(resultmapFlag)){
//            String retMsg = (String) resultmapjsonMap.get("retMsg");
//            return fail(ConstUtil.ERROR_CODE, retMsg);
//        }
//        logger.info("查询个人扩展信息***********************结束");

        Map<String, Object> allCustExtInfoHeadMap = (Map<String, Object>) resultmap.get("head");
        String allCustExtInfotMsg = (String) allCustExtInfoHeadMap.get("retMsg");
        String allCustExtreflagMsg = (String) allCustExtInfoHeadMap.get("retFlag");
        if ("C8602".equals(allCustExtreflagMsg) || "C1109".equals(allCustExtreflagMsg)) {//无客户信息，正常
            return success();
        }
        if (!ifError(allCustExtInfoHeadMap)) {
            return fail(ConstUtil.ERROR_CODE, allCustExtInfotMsg);
        }

        return resultmap;
    }


    @Override
    public Map<String, Object> saveAllCustExtInfo(String token, String channel, String channelNo, Map<String, Object> params) throws Exception {
        logger.info("*********保存个人扩展信息**************开始");
        String typCde = "";//贷款品种
        Map<String, Object> redisMap = null;
        Map<String, Object> paramMap = new HashMap<String, Object>();
        Map<String, Object> custparamMap_one = new HashMap<String, Object>();
        Map<String, Object> custparamMap_two = new HashMap<String, Object>();
        Map<String, Object> resultparamMap = new HashMap<String, Object>();
        Map<String, Object> validateUserFlagMap = new HashMap<String, Object>();
        Map<String, Object> ifNeedFaceChkByTypCdeMap = new HashMap<String, Object>();
        //参数非空判断
        if (token.isEmpty()) {
            logger.info("token为空");
            return fail(ConstUtil.ERROR_CODE, "参数token为空!");
        }
        if (channel.isEmpty()) {
            logger.info("channel为空");
            return fail(ConstUtil.ERROR_CODE, "参数channel为空!");
        }
        if (channelNo.isEmpty()) {
            logger.info("channelNo为空");
            return fail(ConstUtil.ERROR_CODE, "参数channelNo为空!");
        }
        //缓存数据获取
        Map<String, Object> cacheMap = RedisUtils.getExpireMap(token);
        if (cacheMap == null || "".equals(cacheMap)) {
            logger.info("Redis数据获取失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }

        //判断联系人信息管控
        String contactMobile_one = (String) params.get("contactMobile_one");
        String contactMobile_two = (String) params.get("contactMobile_two");
        if (contactMobile_one != null && !"".equals(contactMobile_one) && contactMobile_two != null && !"".equals(contactMobile_two)) {
            if (contactMobile_one.equals(contactMobile_two)) {
                logger.info("两个联系人手机号不能重复");
                return fail(ConstUtil.ERROR_CODE, "联系人手机号不能重复!");
            }
        } else {
            logger.info("联系人手机号为空");
            return fail(ConstUtil.ERROR_CODE, "联系人手机号为空!");
        }


        //总入口需查询客户信息数据
        String custNo = (String) cacheMap.get("custNo");
        String userid = (String) cacheMap.get("userId");
        String name = (String) cacheMap.get("name");//姓名
        String idNumber = (String) cacheMap.get("idCard"); //身份证
//        String userid = "1231231";
//        String custNo = "B201706011214031809670";
//        String name = "张三丰";
//        String idNumber = "232302198201012540";
        if (custNo == null || "".equals(custNo)) {
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        String liveAddress_code = (String) params.get("liveAddress_code");//现居住省市区编码
        String[] liveAddress_code_split = liveAddress_code.split(",");
        String officeAddress_code = (String) params.get("officeAddress_code");//单位省市区编码
        String[] officeAddress_split = officeAddress_code.split(",");
        paramMap.put("channelNo", channelNo);
        paramMap.put("channel", channel);
        paramMap.put("custNo", custNo);
        paramMap.put("maritalStatus", params.get("maritalStatus"));//婚姻状况
        paramMap.put("liveProvince", liveAddress_code_split[0]);// 现住房地址（省）
        paramMap.put("liveCity", liveAddress_code_split[1]);// 现住房地址（市）
        paramMap.put("liveArea", liveAddress_code_split[2]);// 现住房地址（区）
        paramMap.put("liveAddr", params.get("liveAddr"));// 现住房详细地址
        paramMap.put("officeName", params.get("officeName"));// 工作单位
        paramMap.put("officeTel", params.get("officeTel"));// 单位电话
        paramMap.put("officeProvince", officeAddress_split[0]);// 单位地址（省）
        paramMap.put("officeCity", officeAddress_split[1]);// 单位地址（市）
        paramMap.put("officeArea", officeAddress_split[2]);// 单位地址（区）
        paramMap.put("officeAddr", params.get("officeAddr"));// 单位详细地址
        paramMap.put("dataFrom", channelNo);// 数据来源
        //CRM 为额度激活填写默认值
        if ("46".equals(channelNo)) {
            paramMap.put("education", "20");// 最高学历   大专
            paramMap.put("liveInfo", "99");//现居住情况    其他
            paramMap.put("localResid", "10");//户口性质   本地城镇
            paramMap.put("mthInc", 5000);//月收入
            paramMap.put("position", "03");//职务   基层
        }
        Map<String, Object> stringObjectMap = appServerService.saveAllCustExtInfo(token, paramMap);
        if (stringObjectMap == null) {
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }
        Map resultmapjsonMap = (Map<String, Object>) stringObjectMap.get("head");
        String resultmapFlag = (String) resultmapjsonMap.get("retFlag");
        if (!"00000".equals(resultmapFlag)) {
            String retMsg = (String) resultmapjsonMap.get("retMsg");
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }
        logger.info("*********保存个人扩展信息**************结束");
        logger.info("*********保存联系人一**************开始");
        Integer id_one = (Integer) params.get("id_one");
        if (id_one != null && !"null".equals(id_one)) {
            custparamMap_one.put("id", id_one);// 联系人ID
        }
        custparamMap_one.put("channelNo", channelNo);// 渠道
        custparamMap_one.put("channel", channel);
        custparamMap_one.put("custNo", custNo);//客户编号
        custparamMap_one.put("relationType", params.get("relationType_one"));//联系人关系
        custparamMap_one.put("contactName", params.get("contactName_one"));//联系人名称
        custparamMap_one.put("contactMobile", params.get("contactMobile_one"));//联系人手机
        Map<String, Object> CustFCiCustContactMap = appServerService.saveCustFCiCustContact(token, custparamMap_one);
        if (CustFCiCustContactMap == null) {
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }
        Map CustFCiCustContactHeadMap = (Map<String, Object>) CustFCiCustContactMap.get("head");
        String CustFCiCustContactHeadMapFlag = (String) CustFCiCustContactHeadMap.get("retFlag");
        if (!"00000".equals(CustFCiCustContactHeadMapFlag)) {
            String retMsg = (String) CustFCiCustContactHeadMap.get("retMsg");
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }
        logger.info("*********保存联系人一**************结束");
        logger.info("*********保存联系人二**************开始");
        Integer id_two = (Integer) params.get("id_two");
        if (id_two != null && !"null".equals(id_two)) {
            custparamMap_two.put("id", id_two);// 联系人ID
        }
        custparamMap_two.put("channelNo", channelNo);// 渠道
        custparamMap_two.put("channel", channel);
        custparamMap_two.put("custNo", custNo);//客户编号
        custparamMap_two.put("relationType", params.get("relationType_two"));//联系人关系
        custparamMap_two.put("contactName", params.get("contactName_two"));//联系人名称
        custparamMap_two.put("contactMobile", params.get("contactMobile_two"));//联系人手机
        Map<String, Object> CustFCiCustContactTwoMap = appServerService.saveCustFCiCustContact(token, custparamMap_two);
        if (CustFCiCustContactTwoMap == null) {
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }
        Map CustFCiCustContactTwoMapHeadMap = (Map<String, Object>) CustFCiCustContactTwoMap.get("head");
        String CustFCiCustContactTwoMapHeadMapFlag = (String) CustFCiCustContactTwoMapHeadMap.get("retFlag");
        if (!"00000".equals(CustFCiCustContactTwoMapHeadMapFlag)) {
            String retMsg = (String) CustFCiCustContactTwoMapHeadMap.get("retMsg");
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }
        logger.info("*********保存联系人二**************结束");
        logger.info("*********通过贷款品种判断是否需要进行人脸识别**************开始");
        if ("46".equals(channelNo)) {
            typCde = shunguangConfig.getTypCde();//贷款品种
        }
        ifNeedFaceChkByTypCdeMap.put("typCde", typCde);
        ifNeedFaceChkByTypCdeMap.put("source", channel);
        ifNeedFaceChkByTypCdeMap.put("custNo", custNo);
        ifNeedFaceChkByTypCdeMap.put("name", name);
        ifNeedFaceChkByTypCdeMap.put("idNumber", idNumber);
        ifNeedFaceChkByTypCdeMap.put("isEdAppl", "");
        ifNeedFaceChkByTypCdeMap.put("channel", channel);
        ifNeedFaceChkByTypCdeMap.put("channelNo", channelNo);
        Map<String, Object> saveCustFCiCustContactMap = appServerService.ifNeedFaceChkByTypCde(token, ifNeedFaceChkByTypCdeMap);
        if (saveCustFCiCustContactMap == null) {
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }
        Map saveCustFCiCustContactMapHeadMap = (Map<String, Object>) saveCustFCiCustContactMap.get("head");
        String saveCustFCiCustContactMapHeadFlag = (String) saveCustFCiCustContactMapHeadMap.get("retFlag");
        if (!"00000".equals(saveCustFCiCustContactMapHeadFlag)) {
            String retMsg = (String) saveCustFCiCustContactMapHeadMap.get("retMsg");
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }
        Map saveCustFCiCustContactMapBodyMap = (Map<String, Object>) saveCustFCiCustContactMap.get("body");
        String code = (String) saveCustFCiCustContactMapBodyMap.get("code");
        if (code != null && !"".equals(code)) {
            logger.info("*********人脸识别标识码：" + code);
            if ("00".equals(code)) {// 00：已经通过了人脸识别（得分合格），不需要再做人脸识别
//                resultparamMap.put("faceFlag", "1");
                validateUserFlagMap.put("channelNo", channelNo);// 渠道
                validateUserFlagMap.put("channel", channel);
                validateUserFlagMap.put("userId", EncryptUtil.simpleEncrypt(userid));//客户编号
                Map<String, Object> alidateUserMap = appServerService.validateUserFlag(token, validateUserFlagMap);
                if (alidateUserMap == null) {
                    return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
                }
                Map alidateUserHeadMap = (Map<String, Object>) alidateUserMap.get("head");
                String alidateUserHeadMapFlag = (String) alidateUserHeadMap.get("retFlag");
                if (!"00000".equals(alidateUserHeadMapFlag)) {
                    String retMsg = (String) alidateUserHeadMap.get("retMsg");
                    return fail(ConstUtil.ERROR_CODE, retMsg);
                }
                Map alidateUserBodyMap = (Map<String, Object>) alidateUserMap.get("body");
                String payPasswdFlag = (String) alidateUserBodyMap.get("payPasswdFlag");
                if (payPasswdFlag == null || "".equals(payPasswdFlag)) {
                    return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
                }
                if ("1".equals(payPasswdFlag)) {//1.已设置支付密码
                    resultparamMap.put("flag", "1");
                } else {//没有设置支付密码
                    resultparamMap.put("flag", "2");
                }
            } else if ("01".equals(code)) {// 01：未通过人脸识别，剩余次数为0，不能再做人脸识别，录单终止
                resultparamMap.put("flag", "3");
            } else if ("02".equals(code)) {// 02：未通过人脸识别，剩余次数为0，不能再做人脸识别，但可以上传替代影像
                resultparamMap.put("flag", "4");
            } else {//跳转人脸识别
                resultparamMap.put("flag", "5");
            }

        }
        return success(resultparamMap);
    }

    @Override
    public Map<String, Object> upIconPic(MultipartFile iconImg, HttpServletRequest request, HttpServletResponse response) throws Exception {
        logger.info("上传影像*****************开始");
        if (iconImg.isEmpty()) {
            logger.info("图片为空");
            return fail(ConstUtil.ERROR_CODE, "图片为空");
        }
        //前台参数获取
        String token = request.getHeader("token");
        String channel = request.getHeader("channel");
        String channelNo = request.getHeader("channelNo");
        String docCde = request.getParameter("docCde");//影像代码
        String docDesc = request.getParameter("docDesc");//影像名称
        if (StringUtils.isEmpty(token) || StringUtils.isEmpty(channel) || StringUtils.isEmpty(channelNo)) {
            logger.info("token：" + token + "   channel:" + channel + "    channelNo:" + channelNo + "    docCde:" + docCde + "    docDesc:" + docDesc);
            logger.info("前台传入数据有误");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.FAILED_INFO);
        }
        //缓存数据获取
        Map<String, Object> cacheMap = RedisUtils.getExpireMap(token);
        if (cacheMap == null) {
            logger.info("Redis数据获取失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        String typCde = (String) cacheMap.get("typCde");// 贷款品种
        String idNumber = (String) cacheMap.get("idCard");// 身份证号
        String name = (String) cacheMap.get("name");// 姓名
        String mobile = (String) cacheMap.get("phoneNo");// 手机号
        String custNo = (String) cacheMap.get("custNo");
        String userId = (String) cacheMap.get("userId");
//        String typCde = "17044a";// 贷款品种
//        String idNumber = "37040319910722561X";// 身份证号
//        String name = "李甲团";// 姓名
//        String mobile = "15264826872";// 手机号
//        String custNo = "C201708010722561X68720";
//        String userId = "18678282831";
        if (StringUtils.isEmpty(idNumber) || StringUtils.isEmpty(name) || StringUtils.isEmpty(mobile)
                || StringUtils.isEmpty(custNo) || StringUtils.isEmpty(userId)) {
            logger.info("idNumber:" + idNumber + "  name:" + name + "  mobile:" + mobile + "   custNo:" + custNo + "    userId:" + userId);
            logger.info("redis获取数据为空");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }
        InputStream inputStream = iconImg.getInputStream();
        //TODO
        StringBuffer filePath = new StringBuffer(storageConfig.getFacePath()).append(File.separator).append(custNo).append(File.separator).append(docCde).append(File.separator);
        createDir(String.valueOf(filePath));
        String filestreamname = custNo + ".jpg";
        String fileName = UUID.randomUUID().toString().replaceAll("-", "");
//        fileName = "1111";
        filePath = filePath.append(fileName).append(".jpg"); // 测试打开
        FileImageOutputStream outImag = new FileImageOutputStream(new File(String.valueOf(filePath)));
        byte[] bufferOut = new byte[1024];
        int bytes = 0;
        while ((bytes = inputStream.read(bufferOut)) != -1) {
            outImag.write(bufferOut, 0, bytes);
        }
        outImag.close();
        inputStream.close();
        InputStream is = new BufferedInputStream(new FileInputStream(String.valueOf(filePath)));
        String MD5 = DigestUtils.md5Hex(is);
        is.close();
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("channel", channel);
        paramMap.put("channelNo", channelNo);
        paramMap.put("custNo", custNo);// 客户编号
        paramMap.put("attachType", docCde);// 影像类型
        paramMap.put("attachName", docDesc);//影像名称
        paramMap.put("md5", MD5);//文件md5码
        paramMap.put("filePath", filePath.toString());
        paramMap.put("commonCustNo", null);
        paramMap.put("id", request.getParameter("id"));//删除的id
        //影像上传
        Map<String, Object> uploadresultmap = appServerService.attachUploadPersonByFilePath(token, paramMap);
        Map uploadheadjson = (Map<String, Object>) uploadresultmap.get("head");
        String uploadretFlag = (String) uploadheadjson.get("retFlag");
        /*if(!"00000".equals(uploadretFlag)){
            String retMsg = (String) uploadheadjson.get("retMsg");
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }*/
        logger.info("上传影像*****************结束");
//        uploadresultmap = new HashMap<String, Object>();
//        uploadresultmap.put("id", 9999999);
//        return success(uploadresultmap);
        return uploadresultmap;
    }

    @Override
    public Map<String, Object> attachDelete(String token, String channel, String channelNo, Map<String, Object> params) {
        //参数非空判断
        if (token.isEmpty()) {
            logger.info("token为空");
            return fail(ConstUtil.ERROR_CODE, "参数token为空!");
        }
        if (channel.isEmpty()) {
            logger.info("channel为空");
            return fail(ConstUtil.ERROR_CODE, "参数channel为空!");
        }
        if (channelNo.isEmpty()) {
            logger.info("channelNo为空");
            return fail(ConstUtil.ERROR_CODE, "参数channelNo为空!");
        }
        params.put("channel", channel);
        params.put("channelNo", channelNo);
        //影像上传
        Map<String, Object> uploadresultmap = appServerService.attachDelete(token, params);
        Map uploadheadjson = HttpUtil.json2Map(uploadresultmap.get("head").toString());
//        HttpUtil.json2DeepMap(uploadresultmap.get("head"));
//        Map uploadheadjson = (Map<String, Object>) uploadresultmap.get("head");
        String uploadretFlag = (String) uploadheadjson.get("retFlag");
        if (!"00000".equals(uploadretFlag)) {
            String retMsg = (String) uploadheadjson.get("retMsg");
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }
        logger.info("删除影像*****************结束");
        return uploadheadjson;
    }

    public static void createDir(String destDirName) {
        File dir = new File(destDirName);
        if (dir.exists()) {
            return;
        }
        if (!destDirName.endsWith(File.separator)) {
            destDirName = destDirName + File.separator;
        }
        // 创建目录
        if (dir.mkdirs()) {
            return;
        }
    }

    @Override
    public Map<String, Object> attachPic(String token, String channelNo, String channel, Map<String, Object> map) {
        Map<String, Object> paramMap = new HashMap<String, Object>();
        //参数非空判断
        if (token.isEmpty()) {
            logger.info("token为空");
            return fail(ConstUtil.ERROR_CODE, "参数token为空!");
        }
        if (channel.isEmpty()) {
            logger.info("channel为空");
            return fail(ConstUtil.ERROR_CODE, "参数channel为空!");
        }
        if (channelNo.isEmpty()) {
            logger.info("channelNo为空");
            return fail(ConstUtil.ERROR_CODE, "参数channelNo为空!");
        }
        Integer id = (Integer) map.get("id");
        if (id == null && "null".equals(id)) {
            logger.info("影像ID为空");
            return fail(ConstUtil.ERROR_CODE, "参数影像ID为空!");
        }
        paramMap.put("channel", channel);
        paramMap.put("channelNo", channelNo);
        paramMap.put("attachId", id);// 用户ID
        Map<String, Object> uploadresultmap = appServerService.attachPic(token, paramMap);

        return uploadresultmap;
    }

    @Override
    public Map<String, Object> saveAllCustExtInfoForXjd(String token, String channel, String channelNo, Map<String, Object> params) throws Exception {
        logger.info("*********保存个人扩展信息**************开始");
        String typCde = "";//贷款品种
        Map<String, Object> redisMap = null;
        Map<String, Object> paramMap = new HashMap<String, Object>();
        Map<String, Object> custparamMap_one = new HashMap<String, Object>();
        Map<String, Object> custparamMap_two = new HashMap<String, Object>();
        Map<String, Object> resultparamMap = new HashMap<String, Object>();
        Map<String, Object> validateUserFlagMap = new HashMap<String, Object>();
        Map<String, Object> ifNeedFaceChkByTypCdeMap = new HashMap<String, Object>();
        //参数非空判断
        if (token.isEmpty()) {
            logger.info("token为空");
            return fail(ConstUtil.ERROR_CODE, "参数token为空!");
        }
        if (channel.isEmpty()) {
            logger.info("channel为空");
            return fail(ConstUtil.ERROR_CODE, "参数channel为空!");
        }
        if (channelNo.isEmpty()) {
            logger.info("channelNo为空");
            return fail(ConstUtil.ERROR_CODE, "参数channelNo为空!");
        }
        //缓存数据获取
        Map<String, Object> cacheMap = RedisUtils.getExpireMap(token);
        if (cacheMap == null || "".equals(cacheMap)) {
            logger.info("Redis数据获取失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        //判断联系人信息管控
        String contactMobile_one = (String) params.get("contactMobile_one");
        String contactMobile_two = (String) params.get("contactMobile_two");
        //预授信额度flag
        String preAmountFlag = (String) params.get("preAmountFlag");
        if (contactMobile_one != null && !"".equals(contactMobile_one) && contactMobile_two != null && !"".equals(contactMobile_two)) {
            if (contactMobile_one.equals(contactMobile_two)) {
                logger.info("两个联系人手机号不能重复");
                return fail(ConstUtil.ERROR_CODE, "联系人手机号不能重复!");
            }
        } else {
            logger.info("联系人手机号为空");
            return fail(ConstUtil.ERROR_CODE, "联系人手机号为空!");
        }
        String positionType = (String) params.get("positionType");//从业性质
        logger.info("positionType=" + positionType);
        if (positionType == null || "".equals(positionType)) {
            logger.info("positionType为空");
            return fail(ConstUtil.ERROR_CODE, "参数positionType为空!");
        }

        //总入口需查询客户信息数据
        String custNo = (String) cacheMap.get("custNo");
        String userid = (String) cacheMap.get("userId");
        String name = (String) cacheMap.get("name");//姓名
        String idNumber = (String) cacheMap.get("idCard"); //身份证
//        String userid = "1231231";
//        String custNo = "B201706011214031809670";
//        String name = "张三丰";
//        String idNumber = "232302198201012540";
        if (custNo == null || "".equals(custNo)) {
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        String liveAddress_code = (String) params.get("liveAddress_code");//现居住省市区编码
        String[] liveAddress_code_split = liveAddress_code.split(",");
//        String officeAddress_code = (String) params.get("officeAddress_code");//单位省市区编码
//        String[] officeAddress_split = officeAddress_code.split(",");
        paramMap.put("channelNo", channelNo);
        paramMap.put("channel", channel);
        paramMap.put("custNo", custNo);
        paramMap.put("maritalStatus", params.get("maritalStatus"));//婚姻状况
        paramMap.put("liveProvince", liveAddress_code_split[0]);// 现住房地址（省）
        paramMap.put("liveCity", liveAddress_code_split[1]);// 现住房地址（市）
        paramMap.put("liveArea", liveAddress_code_split[2]);// 现住房地址（区）
        paramMap.put("liveAddr", params.get("liveAddr"));// 现住房详细地址
        paramMap.put("dataFrom", channelNo);// 数据来源
        //CRM 为额度激活填写默认值
        paramMap.put("education", "20");// 最高学历   大专
        paramMap.put("liveInfo", "99");//现居住情况    其他
        paramMap.put("localResid", "10");//户口性质   本地城镇
        paramMap.put("mthInc", 5000);//月收入
        paramMap.put("position", "03");//职务   基层
        //（标准化现金贷字段）
        paramMap.put("providerNum", 0);// 供养人数
        paramMap.put("positionType", positionType);// 工作性质
        if ("10".equals(positionType)) {//受薪人士
            paramMap.put("officeName", params.get("officeName"));// 工作单位
            paramMap.put("officeProvince", "990000");// 单位地址（省）990000  370000
            paramMap.put("officeCity", "990100");// 单位地址（市） 990100   370200
            paramMap.put("officeArea", "990101");// 单位地址（区）990101  370212
            paramMap.put("officeAddr", "未知");// 单位详细地址
            paramMap.put("officeTel", params.get("officeTel"));// 单位电话
        }/*else if ("20".equals(positionType)){//自雇人士

        }*/ else if ("50".equals(positionType)) {    //其他 默认值
            paramMap.put("officeName", "未知");// 工作单位
            paramMap.put("officeProvince", "990000");// 单位地址（省）990000
            paramMap.put("officeCity", "990100");// 单位地址（市）990100
            paramMap.put("officeArea", "990101");// 单位地址（区）990101
            paramMap.put("officeAddr", "未知");// 单位详细地址
            paramMap.put("officeTel", "13800000000");// 单位电话
        } else {
            logger.info("从业性质positionType传参有误！ positionType=" + positionType);
            return fail(ConstUtil.ERROR_CODE, "从业性质传参有误！");
        }
//        }
        Map<String, Object> stringObjectMap = appServerService.saveAllCustExtInfo(token, paramMap);
        if (stringObjectMap == null) {
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }
        Map resultmapjsonMap = (Map<String, Object>) stringObjectMap.get("head");
        String resultmapFlag = (String) resultmapjsonMap.get("retFlag");
        if (!"00000".equals(resultmapFlag)) {
            String retMsg = (String) resultmapjsonMap.get("retMsg");
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }
        logger.info("*********保存个人扩展信息**************结束");
        logger.info("*********保存联系人一**************开始");
        Integer id_one = (Integer) params.get("id_one");
        if (id_one != null && !"null".equals(id_one)) {
            custparamMap_one.put("id", id_one);// 联系人ID
        }
        custparamMap_one.put("channelNo", channelNo);// 渠道
        custparamMap_one.put("channel", channel);
        custparamMap_one.put("custNo", custNo);//客户编号
        custparamMap_one.put("relationType", params.get("relationType_one"));//联系人关系
        custparamMap_one.put("contactName", params.get("contactName_one"));//联系人名称
        custparamMap_one.put("contactMobile", params.get("contactMobile_one"));//联系人手机
        Map<String, Object> CustFCiCustContactMap = appServerService.saveCustFCiCustContact(token, custparamMap_one);
        if (CustFCiCustContactMap == null) {
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }
        Map CustFCiCustContactHeadMap = (Map<String, Object>) CustFCiCustContactMap.get("head");
        String CustFCiCustContactHeadMapFlag = (String) CustFCiCustContactHeadMap.get("retFlag");
        if (!"00000".equals(CustFCiCustContactHeadMapFlag)) {
            String retMsg = (String) CustFCiCustContactHeadMap.get("retMsg");
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }
        logger.info("*********保存联系人一**************结束");
        logger.info("*********保存联系人二**************开始");
        Integer id_two = (Integer) params.get("id_two");
        if (id_two != null && !"null".equals(id_two)) {
            custparamMap_two.put("id", id_two);// 联系人ID
        }
        custparamMap_two.put("channelNo", channelNo);// 渠道
        custparamMap_two.put("channel", channel);
        custparamMap_two.put("custNo", custNo);//客户编号
        custparamMap_two.put("relationType", params.get("relationType_two"));//联系人关系
        custparamMap_two.put("contactName", params.get("contactName_two"));//联系人名称
        custparamMap_two.put("contactMobile", params.get("contactMobile_two"));//联系人手机
        Map<String, Object> CustFCiCustContactTwoMap = appServerService.saveCustFCiCustContact(token, custparamMap_two);
        if (CustFCiCustContactTwoMap == null) {
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }
        Map CustFCiCustContactTwoMapHeadMap = (Map<String, Object>) CustFCiCustContactTwoMap.get("head");
        String CustFCiCustContactTwoMapHeadMapFlag = (String) CustFCiCustContactTwoMapHeadMap.get("retFlag");
        if (!"00000".equals(CustFCiCustContactTwoMapHeadMapFlag)) {
            String retMsg = (String) CustFCiCustContactTwoMapHeadMap.get("retMsg");
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }
        logger.info("*********保存联系人二**************结束");
        logger.info("*********通过贷款品种判断是否需要进行人脸识别**************开始");
        //默认贷款品种类型
        typCde = "17021a";
        ifNeedFaceChkByTypCdeMap.put("typCde", typCde);
        ifNeedFaceChkByTypCdeMap.put("source", channel);
        ifNeedFaceChkByTypCdeMap.put("custNo", custNo);
        ifNeedFaceChkByTypCdeMap.put("name", name);
        ifNeedFaceChkByTypCdeMap.put("idNumber", idNumber);
        ifNeedFaceChkByTypCdeMap.put("isEdAppl", "");
        ifNeedFaceChkByTypCdeMap.put("channel", channel);
        ifNeedFaceChkByTypCdeMap.put("channelNo", channelNo);
        Map<String, Object> saveCustFCiCustContactMap = appServerService.ifNeedFaceChkByTypCde(token, ifNeedFaceChkByTypCdeMap);
        if (saveCustFCiCustContactMap == null) {
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }
        Map saveCustFCiCustContactMapHeadMap = (Map<String, Object>) saveCustFCiCustContactMap.get("head");
        String saveCustFCiCustContactMapHeadFlag = (String) saveCustFCiCustContactMapHeadMap.get("retFlag");
        if (!"00000".equals(saveCustFCiCustContactMapHeadFlag)) {
            String retMsg = (String) saveCustFCiCustContactMapHeadMap.get("retMsg");
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }
        Map saveCustFCiCustContactMapBodyMap = (Map<String, Object>) saveCustFCiCustContactMap.get("body");
        String code = (String) saveCustFCiCustContactMapBodyMap.get("code");
        if (code != null && !"".equals(code)) {
            logger.info("*********人脸识别标识码：" + code);
            if ("00".equals(code)) {// 00：已经通过了人脸识别（得分合格），不需要再做人脸识别
//                resultparamMap.put("faceFlag", "1");
                validateUserFlagMap.put("channelNo", channelNo);// 渠道
                validateUserFlagMap.put("channel", channel);
                validateUserFlagMap.put("userId", EncryptUtil.simpleEncrypt(userid));//客户编号
                Map<String, Object> alidateUserMap = appServerService.validateUserFlag(token, validateUserFlagMap);
                if (alidateUserMap == null) {
                    return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
                }
                Map alidateUserHeadMap = (Map<String, Object>) alidateUserMap.get("head");
                String alidateUserHeadMapFlag = (String) alidateUserHeadMap.get("retFlag");
                if (!"00000".equals(alidateUserHeadMapFlag)) {
                    String retMsg = (String) alidateUserHeadMap.get("retMsg");
                    return fail(ConstUtil.ERROR_CODE, retMsg);
                }
                Map alidateUserBodyMap = (Map<String, Object>) alidateUserMap.get("body");
                String payPasswdFlag = (String) alidateUserBodyMap.get("payPasswdFlag");
                if (payPasswdFlag == null || "".equals(payPasswdFlag)) {
                    return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
                }
                if ("1".equals(payPasswdFlag)) {//1.已设置支付密码
                    if ("1".equals(preAmountFlag)) {
                        cacheMap.put("preAmountFlag", preAmountFlag);
                        RedisUtils.setExpire(token, cacheMap);
                        resultparamMap.put("flag", "6");//跳转借款页面
                        return success(resultparamMap);
                    }
                    resultparamMap.put("flag", "1");
                } else {//没有设置支付密码
                    resultparamMap.put("flag", "2");
                }
            } else if ("01".equals(code)) {// 01：未通过人脸识别，剩余次数为0，不能再做人脸识别，录单终止
                resultparamMap.put("flag", "3");
            } else if ("02".equals(code)) {// 02：未通过人脸识别，剩余次数为0，不能再做人脸识别，但可以上传替代影像
                resultparamMap.put("flag", "4");
            } else {//跳转人脸识别
                if ("1".equals(preAmountFlag)) {
                    cacheMap.put("preAmountFlag", preAmountFlag);
                    RedisUtils.setExpire(token, cacheMap);
                }
                resultparamMap.put("flag", "5");
            }

        }
        return success(resultparamMap);
    }

    @Override
    public Map<String, Object> getBankCard(String token, String channel, String channelNo) throws Exception {
        //参数非空判断
        if (token.isEmpty()) {
            logger.info("token为空");
            return fail(ConstUtil.ERROR_CODE, "参数token为空!");
        }
        if (channel.isEmpty()) {
            logger.info("channel为空");
            return fail(ConstUtil.ERROR_CODE, "参数channel为空!");
        }
        if (channelNo.isEmpty()) {
            logger.info("channelNo为空");
            return fail(ConstUtil.ERROR_CODE, "参数channelNo为空!");
        }
        //缓存数据获取
        Map<String, Object> cacheMap = RedisUtils.getExpireMap(token);
        if (cacheMap == null || "".equals(cacheMap)) {
            logger.info("Redis数据获取失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        String custNo = (String) cacheMap.get("custNo");
        if (custNo.isEmpty()) {
            logger.info("custNo为空");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        Map<String, Object> bankCardMap = crmService.getBankCard(custNo);
        Map updmobileheadjson = (Map<String, Object>) bankCardMap.get("head");
        String updmobileretflag = (String) updmobileheadjson.get("retFlag");
        if (!"00000".equals(updmobileretflag)) {
            String retMsg = (String) updmobileheadjson.get("retMsg");
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }
        return bankCardMap;
    }

    @Override
    public IResponse<Map> getLoanTypeAndBankInfo(String token, String channel, String channelNo) throws Exception {
        //参数非空判断
        if (token.isEmpty()) {
            logger.info("token为空");
            return CommonResponse.fail(ConstUtil.ERROR_CODE, "参数token为空!");
        }
        if (channel.isEmpty()) {
            logger.info("channel为空");
            return CommonResponse.fail(ConstUtil.ERROR_CODE, "参数channel为空!");
        }
        if (channelNo.isEmpty()) {
            logger.info("channelNo为空");
            return CommonResponse.fail(ConstUtil.ERROR_CODE, "参数channelNo为空!");
        }
        //缓存数据获取
        Map<String, Object> cacheMap = RedisUtils.getExpireMap(token);
        if (cacheMap == null || "".equals(cacheMap)) {
            logger.info("Redis数据获取失败");
            return CommonResponse.fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        String custName = (String) cacheMap.get("name");
        String idType = (String) cacheMap.get("idType");
        String idNo = (String) cacheMap.get("idCard");
        String cardNo = (String) cacheMap.get("cardNo");//默认实名银行卡号
        String bankCode = (String) cacheMap.get("bankCode");//银行卡代码
        String bankName = (String) cacheMap.get("bankName");//银行卡名称
        if (custName.isEmpty()) {
            logger.info("custName为空");
            return CommonResponse.fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        if (idType.isEmpty()) {
            logger.info("idType为空");
            return CommonResponse.fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        if (idNo.isEmpty()) {
            logger.info("idNo为空");
            return CommonResponse.fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        IResponse<List<LoanType>> loanTypeData = cashLoanService.getLoanType(null, custName, idType, idNo);

        loanTypeData.assertSuccessNeedBody();
        Map<String, Object> map = new HashMap<>();
        map.put("cardNo", cardNo);
        map.put("bankCode", bankCode);
        map.put("bankName", bankName);
        map.put("loanTypes", loanTypeData.getBody());
        return CommonResponse.success(map);
    }

    @Override
    public Map<String, Object> getPaySs(String token, String channel, String channelNo, Map<String, Object> params) throws Exception {
        //参数非空判断
        if (token.isEmpty()) {
            logger.info("token为空");
            return fail(ConstUtil.ERROR_CODE, "参数token为空!");
        }
        if (channel.isEmpty()) {
            logger.info("channel为空");
            return fail(ConstUtil.ERROR_CODE, "参数channel为空!");
        }
        if (channelNo.isEmpty()) {
            logger.info("channelNo为空");
            return fail(ConstUtil.ERROR_CODE, "参数channelNo为空!");
        }
        //缓存数据获取
        Map<String, Object> cacheMap = RedisUtils.getExpireMap(token);
        if (cacheMap == null || "".equals(cacheMap)) {
            logger.info("Redis数据获取失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        String typCde = Convert.toString(params.get("typCde"));
        BigDecimal apprvAmt = Convert.toDecimal(params.get("apprvAmt"));
        String applyTnrTyp = Convert.toString(params.get("applyTnrTyp"));
        String applyTnr = Convert.toString(params.get("applyTnr"));
        String mtdCde = Convert.toString(params.get("mtdCde"));
        if (StringUtils.isEmpty(typCde) || StringUtils.isEmpty(apprvAmt) || StringUtils.isEmpty(applyTnrTyp) ||
                StringUtils.isEmpty(applyTnr) || StringUtils.isEmpty(mtdCde)) {
            logger.info("typCde=" + typCde + "  apprvAmt=" + apprvAmt + "  applyTnrTyp=" + applyTnrTyp + "  applyTnr=" + applyTnr + "mtdCde=" + mtdCde);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.FAILED_INFO);
        }
//        BigDecimal apprvAmt = new BigDecimal(_apprvAmt);
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("channel", channel);
        paramMap.put("channelNo", channelNo);
        paramMap.put("typCde", typCde);
        paramMap.put("apprvAmt", apprvAmt);
        paramMap.put("applyTnrTyp", applyTnrTyp);
        paramMap.put("applyTnr", applyTnr);
//        //TODO   后期改动
//        if ("01".equals(mtdCde)) {
//            mtdCde = "M0002";
//        } else if ("13".equals(mtdCde)) {
//            mtdCde = "M0001";
//        }
        paramMap.put("mtdCde", mtdCde);
        Map<String, Object> paySs = appServerService.getPaySs(token, paramMap);
        Map<String, Object> head = (Map) paySs.get("head");
        if (!"00000".equals(head.get("retFlag"))) {
            logger.info("还款试算,错误信息：" + head.get("retMsg"));
            return fail(ConstUtil.ERROR_CODE, (String) head.get("retMsg"));
        }
        return paySs;
    }

    @Override
    public Map<String, Object> getCustWhiteListCmis(String token, String channel, String channelNo, Map<String, Object> params) throws Exception {
        //参数非空判断
        if (token.isEmpty()) {
            logger.info("token为空");
            return fail(ConstUtil.ERROR_CODE, "参数token为空!");
        }
        if (channel.isEmpty()) {
            logger.info("channel为空");
            return fail(ConstUtil.ERROR_CODE, "参数channel为空!");
        }
        if (channelNo.isEmpty()) {
            logger.info("channelNo为空");
            return fail(ConstUtil.ERROR_CODE, "参数channelNo为空!");
        }
        //缓存数据获取
        Map<String, Object> cacheMap = RedisUtils.getExpireMap(token);
        if (cacheMap == null || "".equals(cacheMap)) {
            logger.info("Redis数据获取失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        String custName = (String) params.get("custName");
        String idTyp = (String) params.get("idTyp");
        String idNo = (String) params.get("idNo");
        if (StringUtils.isEmpty(custName) || StringUtils.isEmpty(idTyp) || StringUtils.isEmpty(idNo)) {
            logger.info("custName=" + custName + "  idTyp=" + idTyp + "  idNo=" + idNo);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.FAILED_INFO);
        }
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("custName", custName);
        paramMap.put("idTyp", idTyp);
        paramMap.put("idNo", idNo);
        Map<String, Object> custWhiteListCmis = crmService.getCustWhiteListCmis(paramMap);
        return custWhiteListCmis;
    }

    @Override
    public Map<String, Object> getCustYsxEd(String token, String channel, String channelNo) throws Exception {
        Map<String, Object> paramMap = new HashMap<String, Object>();
        Map<String, Object> returnParamMap = new HashMap<String, Object>();
        //参数非空判断
        if (token.isEmpty()) {
            logger.info("token为空");
            return fail(ConstUtil.ERROR_CODE, "参数token为空!");
        }
        if (channel.isEmpty()) {
            logger.info("channel为空");
            return fail(ConstUtil.ERROR_CODE, "参数channel为空!");
        }
        if (channelNo.isEmpty()) {
            logger.info("channelNo为空");
            return fail(ConstUtil.ERROR_CODE, "参数channelNo为空!");
        }
        //缓存数据获取
        Map<String, Object> cacheMap = RedisUtils.getExpireMap(token);
        if (cacheMap == null || "".equals(cacheMap)) {
            logger.info("Redis数据获取失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        String custName = (String) cacheMap.get("name");
        String idType = (String) cacheMap.get("idType");
        String idNo = (String) cacheMap.get("idCard");
        paramMap.put("custName", custName);
        paramMap.put("idTyp", idType);
        paramMap.put("idNo", idNo);
        Map<String, Object> custWhiteListCmis = getCustWhiteListCmis(token, channel, channelNo, paramMap);
        Map updmobileheadjson = (Map<String, Object>) custWhiteListCmis.get("head");
        String updmobileretflag = (String) updmobileheadjson.get("retFlag");
        if (!"00000".equals(updmobileretflag)) {
            String retMsg = (String) updmobileheadjson.get("retMsg");
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }
        List<Map<String, String>> custWhiteListCmisList = (List<Map<String, String>>) custWhiteListCmis.get("body");
        for (int i = 0; i < custWhiteListCmisList.size(); i++) {
            if (custWhiteListCmisList.get(i).get("whiteName").startsWith("海尔员工-")) {
                String haierCreditInt = custWhiteListCmisList.get(i).get("haierCredit");
                returnParamMap.put("haierCredit", haierCreditInt);
                break;
            }
        }
        return success(returnParamMap);
    }

}