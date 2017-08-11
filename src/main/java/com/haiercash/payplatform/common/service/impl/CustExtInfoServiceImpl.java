package com.haiercash.payplatform.common.service.impl;

import com.haiercash.commons.redis.Cache;
import com.haiercash.payplatform.common.service.AppServerService;
import com.haiercash.payplatform.common.service.CustExtInfoService;
import com.haiercash.payplatform.common.utils.ConstUtil;
import com.haiercash.payplatform.service.BaseService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CustExtInfoServiceImpl extends BaseService implements CustExtInfoService{
    public Log logger = LogFactory.getLog(getClass());
    @Autowired
    private Cache cache;
    @Autowired
    private AppServerService appServerService;

    @Override
    public Map<String, Object> getAllCustExtInfoAndDocCde(String token, String channel, String channelNo) throws Exception {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        List<JSONObject> resultList =  new ArrayList<JSONObject>();
        JSONObject resultJson = new JSONObject();
        String typCde = "" ;//贷款品种
        List<JSONObject> resultList_ =  new ArrayList<JSONObject>();
        Map<String, Object> allCustExtInfo = getAllCustExtInfo(token, channel, channelNo);
        Map<String, Object> allCustExtInfoHeadMap = (HashMap<String, Object>) allCustExtInfo.get("head");
        String allCustExtInfotMsg =  (String) allCustExtInfoHeadMap.get("retMsg");
        if(!ifError(allCustExtInfoHeadMap)){
            return fail(ConstUtil.ERROR_CODE, allCustExtInfotMsg);
        }
        if("46".equals(channelNo)){
            typCde = "17044a";//贷款品种
        }else{
            //查询贷款品种类型
        }
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("channel", channel);
        paramMap.put("channelNo", channelNo);
        paramMap.put("typCde",typCde);
        Map<String, Object> pLoanTypImagesMap = appServerService.pLoanTypImages(token,paramMap);
        Map pLoanTypImagesHeadMap = (HashMap<String, Object>) pLoanTypImagesMap.get("head");
        String resultmapFlag = (String) pLoanTypImagesHeadMap.get("retFlag");
        String pLoanTypImagesretMsg = (String) pLoanTypImagesHeadMap.get("retMsg");
        if(!ifError(pLoanTypImagesHeadMap)){
            return fail(ConstUtil.ERROR_CODE, pLoanTypImagesretMsg);
        }
        //缓存数据获取
        Map<String, Object> cacheMap = cache.get(token);
//        String custNo = (String)cacheMap.get("custNo");
        String custNo = "C201607151029101937960";
        Map<String, Object> paramYXMap = new HashMap<String, Object>();
        //获取贷款类型查询客户所有影像ID
//        JSONObject pLoanTypImagesBodyjson = new JSONObject(pLoanTypImagesMap.get("body"));
        ArrayList<Map<String,String>> list = (ArrayList<Map<String,String>>)pLoanTypImagesMap.get("body");
        for (int i = 0; i < list.size(); i++) {
            String docCde = list.get(i).get("docCde");//影像代码
            String docDesc = list.get(i).get("docDesc");//影像名称
            paramYXMap.put("attachType",docCde);
            paramYXMap.put("custNo",custNo);
            paramYXMap.put("channel", channel);
            paramYXMap.put("channelNo", channelNo);
            Map<String, Object> stringObjectMap = appServerService.attachTypeSearchPerson(token, paramYXMap);
            Map<String, Object> stringObjectMapHeadMap = (HashMap<String, Object>) stringObjectMap.get("head");
            String stringObjectMapMsg = (String) stringObjectMapHeadMap.get("retMsg");
            if(!ifError(stringObjectMapHeadMap)){
                return fail(ConstUtil.ERROR_CODE, stringObjectMapMsg);
            }
            ArrayList<Map<String,Object>> list_ = (ArrayList<Map<String,Object>>)stringObjectMap.get("body");
            for (int j = 0; j < list_.size(); j++) {
                String id =  Integer.toString((int)list_.get(j).get("id")) ;
                Map<String, Object> paramYXbyIDMap = new HashMap<String, Object>();
                paramYXbyIDMap.put("id",id);
                paramYXbyIDMap.put("channel", channel);
                paramYXbyIDMap.put("channelNo", channelNo);
                Map<String, Object> filePathByFileId = appServerService.getFilePathByFileId(token, paramYXbyIDMap);
                Map<String, Object> filePathByFileIdHeadMap = (HashMap<String, Object>) filePathByFileId.get("head");
                String filePathByFileIdMsg = (String) filePathByFileIdHeadMap.get("retMsg");

                if(!ifError(filePathByFileIdHeadMap)){
                    return fail(ConstUtil.ERROR_CODE, filePathByFileIdMsg);
                }
                Map<String, Object> bodyJSONObjectMap = (HashMap<String, Object>) filePathByFileId.get("body");
//                JSONObject bodyJSONObject = new JSONObject(filePathByFileId.get("body"));
                String filePath = (String) bodyJSONObjectMap.get("filePath");
                JSONObject resultJson_ = new JSONObject();
                resultJson_.put("id",id);//影像ID
                resultJson_.put("filePath",filePath);//图片地址
                resultList_.add(resultJson_);
            }
            resultJson.put("docCde",docCde);//影像代码
            resultJson.put("docDesc",docDesc);//影像名称
            resultJson.put("urlList",resultList_);//地址List
            resultList.add(resultJson);
        }
        resultMap.put("CustExtInfoMap",allCustExtInfo);
        resultMap.put("docList",resultList);
        return resultMap;
    }

    private  boolean ifError(Map<String, Object> map){
        boolean ifError = true;
        String retFlag = (String) map.get("retFlag");
        if(!"00000".equals(retFlag)){
            ifError = false;
        }
        return ifError;
    }
//
//    private  boolean ifError_(ResultHead data){
//        boolean ifError = true;
//        String retFlag =  data.getRetFlag();
//        if(!"00000".equals(retFlag)){
//            ifError = false;
//        }
//        return ifError;
//    }


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
        Map<String, Object> cacheMap = cache.get(token);
//        if(cacheMap.isEmpty()){
//            logger.info("Jedis数据获取失败");
//            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
//        }
        //TODO 总入口需查询客户信息数据
//        String custNo = (String)cacheMap.get("custNo");
        String custNo = "B201706011214031809670";
        paramMap.put("custNo", custNo);
        paramMap.put("flag", "Y");
        paramMap.put("channelNo", channelNo);
        paramMap.put("channel", channel);
        Map<String, Object> resultmap = appServerService.getAllCustExtInfo(token, paramMap);
        if (resultmap == null){
            String retMsg = ConstUtil.ERROR_INFO;
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }
//        JSONObject resultmapjson = new JSONObject(resultmap.get("head").toString());
        Map resultmapjsonMap = (HashMap<String, Object>) resultmap.get("head");
        String resultmapFlag = (String) resultmapjsonMap.get("retFlag");
//        String resultmapFlag = resultmapjson.getString("retFlag");
        if(!"00000".equals(resultmapFlag)){
            String retMsg = (String) resultmapjsonMap.get("retMsg");
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }
        logger.info("查询个人扩展信息***********************结束");
        return resultmap;
    }


}