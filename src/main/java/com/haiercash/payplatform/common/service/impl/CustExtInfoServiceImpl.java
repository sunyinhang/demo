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
        String typCde = "" ;//影像类型
        List<JSONObject> resultList_ =  new ArrayList<JSONObject>();
        Map<String, Object> allCustExtInfo = getAllCustExtInfo(token, channel, channelNo);
        //缓存数据获取
        Map<String, Object> cacheMap = cache.get(token);
        String custNo = (String)cacheMap.get("custNo");
        JSONObject allCustExtInfojson = new JSONObject(allCustExtInfo.get("head"));
        String allCustExtInfotMsg = allCustExtInfojson.getString("retMsg");
        if(!ifError(allCustExtInfojson)){
            return fail(ConstUtil.ERROR_CODE, allCustExtInfotMsg);
        }
        if("45".equals(channelNo)){
            typCde = "";
        }else{
            //TODO 查询贷款品种类型
        }
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("channel", channel);
        paramMap.put("channelNo", channelNo);
        paramMap.put("typCde",typCde);
        Map<String, Object> pLoanTypImagesMap = appServerService.pLoanTypImages(token,paramMap);
        JSONObject pLoanTypImagesHeadjson = new JSONObject(pLoanTypImagesMap.get("head"));
        String pLoanTypImagesretMsg = pLoanTypImagesHeadjson.getString("retMsg");
        if(!ifError(pLoanTypImagesHeadjson)){
            return fail(ConstUtil.ERROR_CODE, pLoanTypImagesretMsg);
        }
        Map<String, Object> paramYXMap = new HashMap<String, Object>();
        //获取贷款类型查询客户所有影像ID
        JSONObject pLoanTypImagesBodyjson = new JSONObject(pLoanTypImagesMap.get("body"));
        ArrayList<JSONObject> list = (ArrayList<JSONObject>)pLoanTypImagesMap.get("body");
        for (int i = 0; i < list.size(); i++) {
            String docCde = list.get(i).getString("docCde");//影像代码
            String docDesc = list.get(i).getString("docDesc");//影像名称
            paramYXMap.put("attachType",docCde);
            paramYXMap.put("custNo",custNo);
            paramYXMap.put("channel", channel);
            paramYXMap.put("channelNo", channelNo);
            Map<String, Object> stringObjectMap = appServerService.attachTypeSearchPerson(token, paramYXMap);
            JSONObject stringObjectMapjson = new JSONObject(stringObjectMap.get("head"));
            String stringObjectMapMsg = stringObjectMapjson.getString("retMsg");
            if(!ifError(stringObjectMapjson)){
                return fail(ConstUtil.ERROR_CODE, stringObjectMapMsg);
            }
            ArrayList<JSONObject> list_ = (ArrayList<JSONObject>)stringObjectMap.get("body");
            for (int j = 0; j < list_.size(); j++) {
                String id = (String) list_.get(j).get("id");
                Map<String, Object> paramYXbyIDMap = new HashMap<String, Object>();
                paramYXbyIDMap.put("id",id);
                paramYXbyIDMap.put("channel", channel);
                paramYXbyIDMap.put("channelNo", channelNo);
                Map<String, Object> filePathByFileId = appServerService.getFilePathByFileId(token, paramYXbyIDMap);
                JSONObject filePathByFileIdjson = new JSONObject(filePathByFileId.get("head"));
                String filePathByFileIdMsg = filePathByFileIdjson.getString("retMsg");
                if(!ifError(filePathByFileIdjson)){
                    return fail(ConstUtil.ERROR_CODE, filePathByFileIdMsg);
                }
                JSONObject bodyJSONObject = new JSONObject(filePathByFileId.get("body"));
                String filePath = bodyJSONObject.getString("filePath");
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

    private  boolean ifError(JSONObject jsonObject){
        boolean ifError = true;
        String retFlag = jsonObject.getString("retFlag");
        if(!"00000".equals(retFlag)){
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
        Map<String, Object> cacheMap = cache.get(token);
        if(cacheMap.isEmpty()){
            logger.info("Jedis数据获取失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        //TODO 总入口需查询客户信息数据
        String custNo = (String)cacheMap.get("custNo");
        paramMap.put("custNo", custNo);
        paramMap.put("flag", "Y");
        paramMap.put("channelNo", channelNo);
        paramMap.put("channel", channel);
        Map<String, Object> resultmap = appServerService.getAllCustExtInfo(token, paramMap);
        JSONObject resultmapjson = new JSONObject((String) resultmap.get("head"));
        String resultmapFlag = resultmapjson.getString("retFlag");
        if(!"00000".equals(resultmapFlag)){
            String retMsg = resultmapjson.getString("retMsg");
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }
        logger.info("查询个人扩展信息***********************结束");
        return success(resultmap);
    }


}