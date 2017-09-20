package com.haiercash.payplatform.service.impl;

import com.haiercash.commons.redis.Session;
import com.haiercash.payplatform.service.BaseService;
import com.haiercash.payplatform.service.CommonPageService;
import com.haiercash.payplatform.utils.ConstUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yuanli on 2017/9/20.
 */
@Service
public class CommonPageServiceImpl extends BaseService implements CommonPageService {
    @Autowired
    private Session session;

    @Value("${app.other.appServer_page_url}")
    protected String appServer_page_url;
    /**
     * 合同展示
     * @param map
     * @return
     */
    @Override
    public Map<String, Object> showcontract(Map<String, Object> map) throws Exception{
        String flag = (String) map.get("flag");

        String token = super.getToken();
        if(StringUtils.isEmpty(flag) || StringUtils.isEmpty(token)){
            logger.info("前台传入参数为空");
            logger.info("flag:" + flag + "  token:" + token);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }

        Map<String, Object> cacheMap = session.get(token, Map.class);
        if (cacheMap == null || "".equals(cacheMap)) {
            logger.info("Jedis数据获取失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        String custName = (String) cacheMap.get("custName");
        String custNo = (String) cacheMap.get("custNo");
        String certNo = (String) cacheMap.get("certNo");

        Map result = new HashMap();
        //征信协议展示
        if("1".equals(flag)){
            String name = new String(Base64.encode(custName.getBytes()), "UTF-8");
            String url = appServer_page_url + "app/appserver/edCredit?custName=" + name + "&certNo=" + certNo;
            url= URLEncoder.encode(url,"UTF-8");
            result.put("url", url);
        }
        //签章协议展示
        if("2".equals(flag)){
            String applseq = (String) map.get("applseq");
            if(StringUtils.isEmpty(applseq)){
                logger.info("applseq:" + applseq);
                return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
            }
            String url = appServer_page_url + "app/appserver/contract?custNo=" + custNo + "&applseq=" + applseq;
            result.put("url", url);
        }
        return success(map);
    }
}
