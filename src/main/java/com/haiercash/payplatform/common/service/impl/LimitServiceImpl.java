package com.haiercash.payplatform.common.service.impl;

import com.haiercash.commons.redis.Session;
import com.haiercash.payplatform.common.service.AppServerService;
import com.haiercash.payplatform.common.service.LimitService;
import com.haiercash.payplatform.common.utils.ConstUtil;
import com.haiercash.payplatform.common.utils.EncryptUtil;
import com.haiercash.payplatform.service.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ljy on 2017/8/15.
 */
@Service
public class LimitServiceImpl extends BaseService implements LimitService{
    @Autowired
    private Session session;
    @Autowired
    private AppServerService appServerService;

    @Value("${app.shunguang.sg_typCde}")
    protected String sg_typCde;
    //模块编码  02
    private static String MODULE_NO = "02";
    public LimitServiceImpl() {
        super(MODULE_NO);
    }

    @Override
    public Map<String, Object> CreditLineApply(String token, String channel, String channelNo) throws Exception {
        logger.info("*********点击额度激活判断跳转页面**************开始");
        Map<String, Object> resultparamMap = new HashMap<String, Object>();
        Map<String, Object> ifNeedFaceChkByTypCdeMap = new HashMap<String, Object>();
        Map<String, Object> validateUserFlagMap = new HashMap<String, Object>();
        String typCde = "" ;//贷款品种
        String tag = "";//标签
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
        Map<String, Object> cacheMap = session.get(token, Map.class);
        if(cacheMap == null || "".equals(cacheMap)){
            logger.info("Redis数据获取失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        if("46".equals(channelNo)){
            typCde = sg_typCde;//贷款品种
            tag = "SHH";
        }else{
            //查询贷款品种类型
        }
        //TODO 总入口需查询客户信息数据
        String custNo = (String)cacheMap.get("custNo");
        String userId = (String)cacheMap.get("userId");
        String custName = (String)cacheMap.get("name");
        String idNumber = (String)cacheMap.get("idCard"); //身份证
//        String custNo = "C201708010722561X68720";
//        String userId = "15264826872";
//        String custName = "李甲团";
//        String idNumber = "37040319910722561X";

        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("channelNo",channelNo);
        paramMap.put("tag",tag);//标签
        paramMap.put("businessType","EDJH");//业务类型 现金贷：XJD   商品分期：SPFQ      额度激活：EDJH    提额：TE   额度申请：EDSQ   个人信息维护：GRXX
        paramMap.put("channel",channel);//渠道号
        paramMap.put("isOrder","N");//是否为订单
        paramMap.put("orderNo","");//订单编号
        paramMap.put("applSeq","");//订单流水号
        paramMap.put("userId",userId);//用户id
        paramMap.put("custNo",custNo);//用户编号
        paramMap.put("typCde",typCde);//贷款品种代码
        paramMap.put("custName",custName);//用户名称
        paramMap.put("noEduLocal","NO");//是否校验最高学历与户口性质
        paramMap.put("idNo",idNumber);//身份证号
        Map<String, Object> stringObjectMap = appServerService.checkIfMsgComplete(token, paramMap);
        if(stringObjectMap == null){
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }
        Map resultmapjsonMap = (HashMap<String, Object>) stringObjectMap.get("head");
        String resultmapFlag = (String) resultmapjsonMap.get("retFlag");
        if(!"00000".equals(resultmapFlag)){
            String retMsg = (String) resultmapjsonMap.get("retMsg");
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }
        Map resultmapbodyMap = (HashMap<String, Object>) stringObjectMap.get("body");
        String SMRZ = (String) resultmapbodyMap.get("SMRZ");//实名认证信息
        String GRJBXX = (String) resultmapbodyMap.get("GRJBXX");//个人基本信息
        String DWXX = (String) resultmapbodyMap.get("DWXX");//单位信息
        String JZXX = (String) resultmapbodyMap.get("JZXX");//居住信息
        String LXRXX = (String) resultmapbodyMap.get("LXRXX");//联系人信息
        Map BCYXMap =   (HashMap<String, Object>) resultmapbodyMap.get("BCYX");//必传影像信息
        String BCYX =  (String) BCYXMap.get("BCYX");
        if("N".equals(SMRZ)){
        //没有做过实名认证，跳转实名认证页面
            resultparamMap.put("flag", "6");//转实名认证页面
        }else{
            if ("Y".equals(GRJBXX) && "Y".equals(DWXX) && "Y".equals(JZXX) && "Y".equals(LXRXX) && "Y".equals(BCYX)){//
                //如个人信息完整，则判断是否做过人脸识别
                ifNeedFaceChkByTypCdeMap.put("typCde",typCde);
                ifNeedFaceChkByTypCdeMap.put("source",channel);
//                ifNeedFaceChkByTypCdeMap.put("custNo","C201708010722561X68720");
//                ifNeedFaceChkByTypCdeMap.put("name","李甲团");
//                ifNeedFaceChkByTypCdeMap.put("idNumber","37040319910722561X");
                ifNeedFaceChkByTypCdeMap.put("custNo",custNo);
                ifNeedFaceChkByTypCdeMap.put("name",custName);
                ifNeedFaceChkByTypCdeMap.put("idNumber",idNumber);
                ifNeedFaceChkByTypCdeMap.put("isEdAppl","Y");
                Map<String, Object> saveCustFCiCustContactMap = appServerService.ifNeedFaceChkByTypCde(token, ifNeedFaceChkByTypCdeMap);
                if(saveCustFCiCustContactMap == null){
                    return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
                }
                Map saveCustFCiCustContactMapHeadMap = (HashMap<String, Object>) saveCustFCiCustContactMap.get("head");
                String saveCustFCiCustContactMapHeadFlag = (String) saveCustFCiCustContactMapHeadMap.get("retFlag");
                if(!"00000".equals(saveCustFCiCustContactMapHeadFlag)){
                    String retMsg = (String) saveCustFCiCustContactMapHeadMap.get("retMsg");
                    return fail(ConstUtil.ERROR_CODE, retMsg);
                }
                Map saveCustFCiCustContactMapBodyMap = (HashMap<String, Object>) saveCustFCiCustContactMap.get("body");
                String code = (String) saveCustFCiCustContactMapBodyMap.get("code");
                if(code != null && !"".equals(code)){
                    logger.info("*********人脸识别标识码："+code);
                    if("00".equals(code)){// 00：已经通过了人脸识别（得分合格），不需要再做人脸识别
//                resultparamMap.put("faceFlag", "1");
                        validateUserFlagMap.put("channelNo", channelNo);// 渠道
                        validateUserFlagMap.put("channel", channel);
                        validateUserFlagMap.put("userId", EncryptUtil.simpleEncrypt(userId));//客户编号18254561920
                        Map<String, Object> alidateUserMap = appServerService.validateUserFlag(token, validateUserFlagMap);
                        if(alidateUserMap == null){
                            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
                        }
                        Map alidateUserHeadMap = (HashMap<String, Object>) alidateUserMap.get("head");
                        String alidateUserHeadMapFlag = (String) alidateUserHeadMap.get("retFlag");
                        if(!"00000".equals(alidateUserHeadMapFlag)){
                            String retMsg = (String) alidateUserHeadMap.get("retMsg");
                            return fail(ConstUtil.ERROR_CODE, retMsg);
                        }
                        Map alidateUserBodyMap = (HashMap<String, Object>) alidateUserMap.get("body");
                        String payPasswdFlag = (String) alidateUserBodyMap.get("payPasswdFlag");
                        if(payPasswdFlag == null || "".equals(payPasswdFlag)){
                            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
                        }
                        if("1".equals(payPasswdFlag)){//1.已设置支付密码
                            resultparamMap.put("flag", "1");
                        }else{//没有设置支付密码
                            resultparamMap.put("flag", "2");
                        }
                    }else if("01".equals(code)){// 01：未通过人脸识别，剩余次数为0，不能再做人脸识别，录单终止
                        resultparamMap.put("flag", "3");
                    }else if("02".equals(code)){// 02：未通过人脸识别，剩余次数为0，不能再做人脸识别，但可以上传替代影像
                        resultparamMap.put("flag", "4");
                    }else{//跳转人脸识别
                        resultparamMap.put("flag", "5");
                    }

                }
            }else{
                //个人信息不完成，跳转完善个人扩展信息页面
                resultparamMap.put("flag", "7");//跳转完善个人扩展信息页面
            }
        }
        return success(resultparamMap);
    }
}
