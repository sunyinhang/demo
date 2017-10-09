package com.haiercash.payplatform.service.impl;

import com.haiercash.commons.redis.Session;
import com.haiercash.payplatform.service.AppServerService;
import com.haiercash.payplatform.service.RegisterService;
import com.haiercash.payplatform.utils.ConstUtil;
import com.haiercash.payplatform.utils.EncryptUtil;
import com.haiercash.payplatform.service.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ljy on 2017/8/17.
 */
@Service
public class RegisterServiceImpl extends BaseService implements RegisterService {
    @Autowired
    private Session session;
    @Autowired
    private AppServerService appServerService;

    @Override
    public Map<String, Object> isRegister(String token, String channel, String channelNo, Map<String, Object> params) throws Exception {
        logger.info("*********判断用户是否注册**************开始");
        Map<String, Object> resultparamMap = new HashMap<String, Object>();
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
        if (params.get("userId") == null || "".equals(params.get("userId"))) {
            logger.info("userId为空");
            return fail(ConstUtil.ERROR_CODE, "参数userId为空!");
        }
        String userIdEncrypt = EncryptUtil.simpleEncrypt((String) params.get("userId"));
        //缓存数据获取
//        Map<String, Object> cacheMap = session.get(token, Map.class);
//        if(cacheMap == null || "".equals(cacheMap)){
//            logger.info("Redis数据获取失败");
//            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
//        }
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("channelNo", channelNo);
        paramMap.put("channel", channel);
        paramMap.put("mobile", userIdEncrypt);
        Map<String, Object> registerMap = appServerService.isRegister(token, paramMap);
        if (registerMap == null) {
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }
        Map resultmapjsonMap = (Map<String, Object>) registerMap.get("head");
        String resultmapFlag = (String) resultmapjsonMap.get("retFlag");
        if (!"00000".equals(resultmapFlag)) {
            String retMsg = (String) resultmapjsonMap.get("retMsg");
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }
        Map resultmapbodyMap = (Map<String, Object>) registerMap.get("body");
        String isRegister = (String) resultmapbodyMap.get("isRegister");
        if ("N".equals(isRegister)) {
            resultparamMap.put("flag", "1");//还未注册
        }
        if ("Y".equals(isRegister)) {
            resultparamMap.put("flag", "2");//已注册
        }
        if ("C".equals(isRegister)) {
            resultparamMap.put("flag", "3");//手机号已被占用
        }

        return success(resultparamMap);
    }

    /**
     * @Title
     * @Description:
     * @author yu jianwei
     * @date 2017/10/9 9:48
     */
    @Override
    public Map<String, Object> saveUauthUsers(String token, Map<String, Object> params) throws Exception {
        Map returnmap = new HashMap<String, Object>();//返回的map
        params.put("mobile", EncryptUtil.simpleEncrypt(String.valueOf(params.get("mobile"))));
        params.put("password", EncryptUtil.simpleEncrypt(String.valueOf(params.get("password"))));
        Map usermap = appServerService.saveUauthUsers(token, params);
        String userretFlag = String.valueOf(((Map<String, Object>) (usermap.get("head"))).get("retFlag"));
        if ("00000".equals(userretFlag)) {
            //注册成功
            String uidLocal = usermap.get("body").toString();//统一认证内userId
            returnmap.put("flag", "1");//注册成功
            return success(returnmap);
        } else if ("U0143".equals(userretFlag)) {
            returnmap.put("flag", "2");
            return success(returnmap);//用户已注册
        } else {
            //注册失败
            String userretmsg = ((Map<String, Object>) (usermap.get("head"))).get("retMsg").toString();
            return fail(ConstUtil.ERROR_CODE, userretmsg);
        }
    }
}
