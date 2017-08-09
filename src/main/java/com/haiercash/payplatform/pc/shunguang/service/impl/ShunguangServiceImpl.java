package com.haiercash.payplatform.pc.shunguang.service.impl;

import com.haiercash.payplatform.common.config.EurekaServer;
import com.haiercash.payplatform.common.dao.CooperativeBusinessDao;
import com.haiercash.payplatform.common.data.CooperativeBusiness;
import com.haiercash.payplatform.common.service.HaierDataService;
import com.haiercash.payplatform.common.utils.Base64Utils;
import com.haiercash.payplatform.common.utils.ConstUtil;
import com.haiercash.payplatform.common.utils.HttpUtil;
import com.haiercash.payplatform.common.utils.RSAUtils;
import com.haiercash.payplatform.pc.shunguang.service.ShunguangService;
import com.haiercash.payplatform.service.BaseService;
import com.netflix.ribbon.proxy.annotation.Http;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * shunguang service impl.
 * @author yuan li
 * @since v1.0.1
 *
 */
@Service
public class ShunguangServiceImpl extends BaseService implements ShunguangService{
    public Log logger = LogFactory.getLog(getClass());

    @Autowired
    private CooperativeBusinessDao cooperativeBusinessDao;
    @Autowired
    private HaierDataService haierDataService;

    @Override
    public Map<String, Object> edApply(Map<String, Object> map) throws Exception{
        logger.info("白条额度申请接口*******************开始");
        String applyNo = (String) map.get("applyNo");//交易流水号
        String channleNo = (String) map.get("channleNo");//交易渠道
        String tradeCode = (String) map.get("tradeCode");//交易编码
        String data = (String) map.get("data");//交易信息

        //获取渠道公钥
        logger.info("获取渠道" + channleNo +"公钥");
        CooperativeBusiness cooperativeBusiness = cooperativeBusinessDao.selectBycooperationcoed(channleNo);
        if(cooperativeBusiness == null){
            return fail(ConstUtil.ERROR_CODE, "渠道编码传输有误");
        }
        String publicKey = cooperativeBusiness.getRsapublic();//获取公钥

        //请求数据解析
        String params = new String(RSAUtils.decryptByPublicKey(Base64Utils.decode(data), publicKey));
        logger.info("额度申请接口请求数据：" + params);
        JSONObject json = new JSONObject(params);
        String token = (String) json.get("token");
        String userType = (String) json.get("userType");
        String custmessage =  json.get("custmessage").toString();
        JSONObject custjson = new JSONObject(custmessage);
        String name = (String) custjson.get("name");
        String idNo = (String) custjson.get("idNo");
        String mobile = (String) custjson.get("mobile");
        String bankCode = (String) custjson.get("bankCode");

        //根据token获取userid
        JSONObject userjson = haierDataService.userinfo(token);
        if(userjson == null || "".equals(userjson)){
            logger.info("验证客户信息接口调用失败");
            return fail(ConstUtil.ERROR_CODE, "验证客户信息失败");
        }
        String user_id = userjson.get("user_id").toString();
        //TODO!!!!

        String result = HttpUtil.restGet(EurekaServer.APPSERVERNOAUTHNEW + "/app/appserver/face");
        if (HttpUtil.isSuccess(result) ) {
           Map<String, Object>  resultMap = HttpUtil.json2Map(result);
        }


        return success();
    }
}
