package com.haiercash.appserver.web;

import com.haiercash.common.config.EurekaServer;
import com.haiercash.common.data.AppOrderRequest;
import com.haiercash.common.data.AppOrderRequestRepository;
import com.haiercash.commons.util.HttpUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/app/appserver/apporderrequest")
public class AppOrderRequestController extends BaseController {
    public Log logger = LogFactory.getLog(getClass());

    public AppOrderRequestController() {
        super("41");
    }
    @Autowired
    AppOrderRequestRepository appOrderRequestRepository;

    @RequestMapping(value = "/selectAll", method = RequestMethod.GET)
    public Map<String, Object> selectAll() {
        List<AppOrderRequest> list=appOrderRequestRepository.findAll();
        if(list==null){
            return fail("99","查询失败！");
        }
        return success(list);
    }
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public Map<String, Object> save(@RequestBody AppOrderRequest appOrderRequest) {
        if(StringUtils.isEmpty(appOrderRequest.getApplSeq())){
            return fail("94","申请流水号不可为空！");
        }
        logger.debug("来自支付平台的申请流水号："+appOrderRequest.getApplSeq());
        String url = EurekaServer.CMISPROXY + "/api/appl/getCustInfo?applSeq=" + appOrderRequest.getApplSeq();
        logger.debug("支付平台：RequestUrl:"+url);
        String s = HttpUtil.restGet(url, super.getToken());
        Map<String,Object> map;
        if(StringUtils.isEmpty(s)){
            return fail("01","查询失败");
        }else{
            map = HttpUtil.json2Map(s);
        }
        logger.debug("支付平台：订单客户信息getCustInfo:"+map);
        appOrderRequest.setIdNo(String.valueOf(map.get("ID_NO")));
        appOrderRequest.setCustName(String.valueOf(map.get("CUST_NAME")));
        appOrderRequest.setState("0");
        appOrderRequest.setTryCount(0);
        if(StringUtils.isEmpty(appOrderRequest.getId())){
            appOrderRequest.setId(UUID.randomUUID().toString().replace("-",""));
            appOrderRequest.setCreateTime(new Date());
            appOrderRequestRepository.save(appOrderRequest);
        }else{
            appOrderRequestRepository.save(appOrderRequest);
        }
        HashMap<String,Object> hm=new HashMap<String,Object>();
        hm.put("id",appOrderRequest.getId());
        hm.put("applSeq",appOrderRequest.getApplSeq());
        hm.put("custName",appOrderRequest.getCustName());
        hm.put("idNo",appOrderRequest.getIdNo());
        hm.put("createTime",new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format( appOrderRequest.getCreateTime()));
        logger.debug("支付平台：返回hm"+hm);
        return success(hm);
    }
    @RequestMapping(value = "/deleteById", method = RequestMethod.DELETE)
    public Map<String, Object> deleteById(String id) {
        appOrderRequestRepository.delete(id) ;
        return success();
    }

}
