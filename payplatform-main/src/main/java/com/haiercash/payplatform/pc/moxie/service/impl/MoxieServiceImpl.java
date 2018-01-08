package com.haiercash.payplatform.pc.moxie.service.impl;

import com.haiercash.payplatform.common.dao.MoxieInfoDao;
import com.haiercash.payplatform.common.data.MoxieInfo;
import com.haiercash.payplatform.pc.moxie.service.MoxieService;
import com.haiercash.payplatform.service.AppServerService;
import com.haiercash.payplatform.utils.EncryptUtil;
import com.haiercash.spring.service.BaseService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by yuanli on 2017/10/9.
 */
@Service
public class MoxieServiceImpl extends BaseService implements MoxieService {
    @Autowired
    private AppServerService appServerService;
    @Autowired
    private MoxieInfoDao moxieInfoDao;
    @Override
    public void moxie(String body, String flag) {
        logger.info("魔蝎回调接收数据：" + body);
        JSONObject json1 = new JSONObject(body);
        String taskid = json1.get("task_id").toString();
        String user_id = json1.get("user_id").toString();

        String idNo = user_id.substring(0,18);
        String applseq = user_id.substring(18);
//        String user = EncryptUtil.simpleDecrypt(user_id);
//        JSONObject jsonObject = new JSONObject(user);

//        String applseq = jsonObject.get("applseq").toString();
//        String idNo = jsonObject.get("idNo").toString();
        logger.info("申请流水号：" + applseq);
        logger.info("身份证号：" + idNo);

        Map identityMap = new HashMap();
        identityMap.put("certNo", idNo);

        //根据省份证查询客户信息
        Map<String,Object> mapCust = appServerService.getCustInfoByCertNo("", identityMap);
        Map<String, Object> mapcrm = (Map) mapCust.get("head");
        String retFlag = (String) mapcrm.get("retFlag");
        if(!"00000".equals(retFlag)){
            logger.info("CRM查询客户信息失败");
            return;
        }

        Map<String, Object> m = (Map) mapCust.get("body");

        String custName = m.get("custName").toString();//得到客户姓名
        String mobile = m.get("mobile").toString();//mobile
        String custNo = m.get("custNo").toString();//得到客户编号

        List<Map<String, Object>> list = new ArrayList<>();
        Map map0 = new HashMap<>();
        map0.put("content", EncryptUtil.simpleEncrypt(taskid));
        map0.put("reserved6",applseq);
        if("01".equals(flag)){//公积金
            map0.put("reserved7","antifraud_fund");
        }
        if("02".equals(flag)){//网银
            map0.put("reserved7","antifraud_banking");
        }
        if("03".equals(flag)){//运营商
            String moxiemobile = (String) json1.get("mobile");
            map0.put("reserved7",moxiemobile);
        }
        list.add(map0);

        List<Object> l1 = new ArrayList<>();
        l1.add(list);

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("idNo", EncryptUtil.simpleEncrypt(idNo));
        map.put("name", EncryptUtil.simpleEncrypt(custName));
        map.put("mobile", EncryptUtil.simpleEncrypt(mobile));
        if("03".equals(flag)){
            map.put("dataTyp", "A602");//运营商
        } else{
            map.put("dataTyp", "A601");//网银公积金
        }

        map.put("source", "2");
        map.put("remark3", custNo);
        map.put("reserved6", applseq);
        map.put("content", l1);
        map.put("applSeq",applseq);
        map.put("channel","11");

        //风险信息上送
        appServerService.updateRiskInfo("", map);

        //风险信息上送
//        AppCmisInfo appCmisInfo = new AppCmisInfo();
//        appCmisInfo.setFlag("0");
//        appCmisInfo.setInsertTime(new Date());
//        appCmisInfo.setRequestMap(map);
//        appCmisInfo.setTradeCode(CmisTradeCode.TRADECODE_WWRISK);
//        appCmisInfo.setId(UUID.randomUUID().toString().replace("-", ""));
//        appCmisInfoRepository.save(appCmisInfo);


        //数据库数据添加
        String id = UUID.randomUUID().toString().replace("-","");
        //String userid = jsonObject.toString();
        MoxieInfo moxie = new MoxieInfo();
        moxie.setId(id);
        moxie.setTaskId(taskid);
        moxie.setUserId(user_id);
        moxie.setApplseq(applseq);
        moxie.setIdno(idNo);
        moxie.setFlag(flag);//01：公积金   02：网银
        Date date=new Date();
        DateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time=format.format(date);
        moxie.setCreateDate(time);
        moxieInfoDao.saveMoxieInfoDao(moxie);
    }

    @Override
    public Map getMoxieByApplseq(String applseq) {
        Map<String, Object> map = new HashMap<String, Object>();
        MoxieInfo moxieFund = moxieInfoDao.getMoxieInfo(applseq, "01");//公积金
        MoxieInfo moxieBank = moxieInfoDao.getMoxieInfo(applseq, "02");//网银
        MoxieInfo moxieCarrier = moxieInfoDao.getMoxieInfo(applseq, "03");//运营商
        map.put("isFund","N");
        map.put("isBank","N");
        map.put("isCarrier","N");
        //查询是否做公积金
        if(moxieFund != null){
            map.put("isFund","Y");
        }
        //是否做过网银
        if(moxieBank != null){
            map.put("isBank","Y");
        }
        //是否做过运营商
        if(moxieCarrier != null){
            map.put("isCarrier","Y");
        }

        return  map;

//        Map paramMap = new HashMap<>();
//        paramMap.put("applseq", applseq);
//        Map moxiemap = appServerService.getMoxieByApplseq("", paramMap);
//        return (Map) moxiemap.get("body");
    }
}
