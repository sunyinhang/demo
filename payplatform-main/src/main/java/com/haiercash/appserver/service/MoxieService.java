package com.haiercash.appserver.service;

import com.haiercash.common.data.*;
import com.haiercash.common.service.BaseService;
import com.haiercash.commons.cmis.CmisTradeCode;
import com.haiercash.commons.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Created by yuanli on 2017/4/11.
 */
@Service
public class MoxieService extends BaseService {
    @Autowired
    private QiaoRongService qiaoRongService;
    @Autowired
    private AppCmisInfoRepository appCmisInfoRepository;
    @Autowired
    MoxieInfoRepository moxieInfoRepository;

    private static Log logger = LogFactory.getLog(MerchFaceService.class);

    public void moxie(String body, String flag){
        JSONObject json1 = new JSONObject(body);
        String taskid = json1.get("task_id").toString();
        String user_id = json1.get("user_id").toString();
        String user = EncryptUtil.simpleDecrypt(user_id);
        JSONObject jsonObject = new JSONObject(user);

        String applseq = jsonObject.get("applseq").toString();
        String idNo = jsonObject.get("idNo").toString();
        logger.info("申请流水号：" + applseq);
        logger.info("身份证号：" + idNo);

        //根据省份证查询客户信息
        Map<String,Object> mapCust = qiaoRongService.getCustInfoByCertNo(idNo);
        Map<String, Object> mapcrm = HttpUtil.json2Map(mapCust.get("head").toString());
        String retFlag = (String) mapcrm.get("retFlag");
        if(!"00000".equals(retFlag)){
            logger.info("CRM查询客户信息失败");
            return;
        }

        Map<String, Object> m = HttpUtil.json2Map(mapCust.get("body").toString());

        String custName = m.get("custName").toString();//得到客户姓名
        String mobile = m.get("mobile").toString();//mobile
        String custNo = m.get("custNo").toString();//得到客户编号

        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map map0 = new HashMap<>();
        map0.put("content",EncryptUtil.simpleEncrypt(taskid));
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

        List<Object> l1 = new ArrayList<Object>();
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

        //风险信息数据存于本地
        AppCmisInfo appCmisInfo = new AppCmisInfo();
        appCmisInfo.setFlag("0");
        appCmisInfo.setInsertTime(new Date());
        appCmisInfo.setRequestMap(map);
        appCmisInfo.setTradeCode(CmisTradeCode.TRADECODE_WWRISK);
        appCmisInfo.setId(UUID.randomUUID().toString().replace("-", ""));
        appCmisInfoRepository.save(appCmisInfo);

        //調用风险信息接口
//                logger.info("风险信息请求数据：" + map.toString());
//                Map<String, Object> resultmap = cmisApplService.updateRiskInfo(map);
//                logger.info("风险信息返回结果：" + resultmap);

        String id = UUID.randomUUID().toString().replace("-","");
        String userid = jsonObject.toString();
        MoxieInfo moxie = new MoxieInfo();
        moxie.setId(id);
        moxie.setTaskId(taskid);
        moxie.setUserId(userid);
        moxie.setApplseq(applseq);
        moxie.setIdno(idNo);
        moxie.setFlag(flag);//01：公积金   02：网银
        Date date=new Date();
        DateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time=format.format(date);
        moxie.setCreateDate(time);
        moxieInfoRepository.save(moxie);

    }

}
