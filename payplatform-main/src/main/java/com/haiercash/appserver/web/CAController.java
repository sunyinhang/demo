package com.haiercash.appserver.web;

import com.haiercash.common.config.EurekaServer;
import com.haiercash.appserver.service.CASignService;
import com.haiercash.appserver.util.ConstUtil;
import com.haiercash.appserver.util.sign.SignType;
import com.haiercash.appserver.util.sign.ca.CAService;
import com.haiercash.common.data.UAuthCASignRequest;
import com.haiercash.common.data.UAuthCASignRequestRepository;
import com.haiercash.common.data.UAuthUserToken;
import com.haiercash.common.data.UAuthUserTokenRepository;
import com.haiercash.commons.util.HttpUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author liuhongbin
 * @date 2016/4/13
 * @description: CA签章服务接口
 **/
@RestController
@EnableRedisHttpSession
public class CAController extends BaseController {
    private Log logger = LogFactory.getLog(CAController.class);
    @Autowired
    UAuthUserTokenRepository uAuthUserTokenRepository;
    @Autowired
    UAuthCASignRequestRepository uAuthCASignRequestRepository;
    @Autowired
    CASignService cASignService;
    @Autowired
    CHController chController;

    public static String MODULE_NO = "03";

    public CAController() {
        super(MODULE_NO);
    }

    /**
     * 第三方提交签章请求
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/appserver/caRequest", method = RequestMethod.POST)
    public Map<String, Object> caRequest(@RequestBody Map<String, Object> request) {
        // 校验参数
        Map<String, Object> confirmMsg = confirmCaData(request);
        if(confirmMsg != null){
            return confirmMsg;
        }

        String url = EurekaServer.APPCA +"/api/caRequest";
        Map resultMap = HttpUtil.restPostMap(url, super.getToken(), request, 200);
        if(StringUtils.isEmpty(resultMap) || resultMap.isEmpty()){
            return fail("18", "签章系统通信失败");
        }
        HashMap head = (HashMap) resultMap.get("head");
        if (head.get("retFlag").equals("00000")) {
            return success();
        }
        return fail(String.valueOf(head.get("retFlag")),String.valueOf(head.get("retMsg")));
    }

    /**
     * 互动金融签章请求
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/appserver/ca/hdjr", method = RequestMethod.POST)
    public Map<String,Object> hdjrCaRequest(@RequestBody Map<String, Object> request){
        // 校验参数
        Map<String, Object> confirmMsg = confirmCaData(request);
        if(confirmMsg != null){
            return confirmMsg;
        }

        String url = EurekaServer.APPCA +"/api/hdjr";
        Map<String, Object> resultMap = HttpUtil.restPostMap(url, request);
        if(StringUtils.isEmpty(resultMap) || resultMap.isEmpty()){
            return fail("18", "签章系统通信失败");
        }
        HashMap head = (HashMap) resultMap.get("head");
        if (head.get("retFlag").equals("00000")) {
            return success();
        }
        return fail(String.valueOf(head.get("retFlag")),String.valueOf(head.get("retMsg")));
    }

    /**
     * 提交签章请求参数校验
     * @return 错误：返回fail信息；正确：返回null
     */
    public Map<String, Object> confirmCaData(Map<String, Object> request){
        if (StringUtils.isEmpty(request.get("applseq"))) {
            return fail("11", "订单申请流水号(applseq)不能为空");
        } else if (StringUtils.isEmpty(request.get("custName"))) {
            return fail("12", "客户姓名(custName）不能为空");
        } else if (StringUtils.isEmpty(request.get("custIdCode"))) {
            return fail("13", "客户身份证号(custIdCode)不能为空");
        } else if (StringUtils.isEmpty(request.get("signType"))) {
            return fail("14", "签章类型(signType)不能为空");
        } else if (StringUtils.isEmpty(request.get("orderJson"))) {
            return fail("15", "订单信息(orderJson)不能为空");
        } else if (StringUtils.isEmpty(request.get("sysFlag"))) {
            return fail("16", "系统标识(sysFlag)不能为空");
        }
        return null;
    }


    /**
     * 协议/合同签章确认接口.
     *
     * @param signCode 签章uuid
     * @return
     */
    @RequestMapping(value = "/app/appserver/ca/{code}/confirm", method = RequestMethod.PUT)
    public Map<String, Object> caConfirm(@PathVariable("code") String signCode) {
        if (StringUtils.isEmpty(signCode)) {
            return fail("06", "获取唯一码失败");
        }
        String url = EurekaServer.APPCA + "/api/" + signCode + "/confirm";
        logger.info("signCode:"+signCode+",url:"+url);
        String result = HttpUtil.restPut(url, super.getToken(), null, 200);
        logger.info("result:" + result);
        if(StringUtils.isEmpty(result)){
            return fail("18", "签章系统通信失败");
        }
        Map<String, Object> resultMap = HttpUtil.json2Map(result);
        if(StringUtils.isEmpty(resultMap) || resultMap.isEmpty()){
            return fail("18", "签章系统通信失败");
        }
        JSONObject head = (JSONObject) resultMap.get("head");
        if (head.get("retFlag").equals("00000")) {
            return success();
        }
        return fail(String.valueOf(head.get("retFlag")),String.valueOf(head.get("retMsg")));
    }

    /**
     * 协议、合同展示界面
     *
     * @param signCode
     * @return
     */
    @RequestMapping(value = "/app/appserver/ca/{code}/forward", method = RequestMethod.GET)
    public ModelAndView forward2AgreenmentAndContract(@PathVariable("code") String signCode, RedirectAttributes redirectAttributes) {
        if (StringUtils.isEmpty(signCode)) {
            return new ModelAndView("/agreement/404.html");
        }

        UAuthCASignRequest request = uAuthCASignRequestRepository.findBySignCode(signCode);
        if (request == null) {
            return new ModelAndView("/agreement/404.html");
        }

        redirectAttributes.addAttribute("code", signCode);
        redirectAttributes.addAttribute("applseq", request.getApplseq());



        // 银行卡变更
        if (request.getSignType().equals(SignType.grant.toString())) {
            Map<String, Object> orderMap = HttpUtil.json2Map(request.getOrderJson());
            Map<String, Object> map = HttpUtil.json2Map(orderMap.get("cardInfo").toString());
            redirectAttributes.addAttribute("custNo", map.get("custNo"));
            redirectAttributes.addAttribute("cardNo", map.get("cardNo"));
            return new ModelAndView("redirect:/app/appserver/grant");
        }
        // 征信
        else if (request.getSignType().equals(SignType.credit.toString())) {
            redirectAttributes.addAttribute("orderNo", request.getOrderNo());
            return new ModelAndView("redirect:/app/appserver/credit");
        }
        // 注册
        else if (request.getSignType().equals(SignType.register.toString())) {
            redirectAttributes.addAttribute("orderNo", request.getOrderNo());
            return new ModelAndView("redirect:/app/appserver/register");
        }
        // 美凯龙额度使用须知
       /* else if (request.getSignType().equals(SignType.mkledsyxz.toString())) {
            redirectAttributes.addAttribute("orderNo", request.getOrderNo());
            return new ModelAndView("redirect:/app/appserver/mkledsyxz");
        }*/
        // 共同还款人协议
        else if (request.getSignType().equals("common")) {
            redirectAttributes.addAttribute("orderNo", request.getOrderNo());
            redirectAttributes.addAttribute("commonCustNo", request.getCommonCustNo());
            return new ModelAndView("redirect:/app/appserver/comRepayPerson");
        }
        // 合同
        else {
            return new ModelAndView("redirect:/app/appserver/contract");
        }
    }

    /**
     * 根据signCode在本地uauthcasignrequest表中查得applseq
     * (不单单适用于互动金融合同页面)
     *
     * @param signCode
     * @return data.body.applseq
     */
    @RequestMapping(value="/app/appserver/ca/hdjrforward",method = RequestMethod.GET)
    public Map<String,Object> forward2HdjrContract(@RequestParam("code") String signCode){
        logger.debug("互动金融合同跳转接口：signCode="+signCode);
        if (StringUtils.isEmpty(signCode)) {
            return fail("31","code参数为空");
        }

        UAuthCASignRequest request = uAuthCASignRequestRepository.findBySignCode(signCode);
        if (request == null) {
            return fail("32","未查到request");
        }
        logger.debug("查得applseq="+request.getApplseq());

        //redirectAttributes.addAttribute("code", signCode);
        //redirectAttributes.addAttribute("applseq", request.getApplseq());
        Map<String,Object> map = new HashMap<>();
        map.put("applseq",request.getApplseq());
        return success(map);
    }


    /**
     * 提交CA签章申请
     *
     * @param orderNo 订单号
     * @return 操作成功，在消息body返回{sign_code: 签章申请流水号}
     */
    @RequestMapping(value = "/app/appServer/caSignRequest", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> caSignRequest(@RequestParam("orderNo") String orderNo) {
        // 查询当前用户信息
        String clientId = getParam("client_id");
        clientId = "A0000055B0FB82";// 临时写死
        Map<String, Object> resultMap = cASignService.caSignRequest(orderNo, clientId, "1");
        if ("err".equals((String) resultMap.get("resultCode"))) {
            return super.fail(ConstUtil.ERROR_CA_TYPE_INVALID_CODE, (String) resultMap.get("resultMsg"));
        }
        return success(resultMap);
    }

    /**
     * CA签章申请处理结果查询.
     *
     * @param signCode
     * @return 操作成功，在消息body返回{result: 处理结果 - 0处理中 1成功 2失败}
     */
    @RequestMapping(value = "/app/appServer/caSignResult", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> caSignResult(@RequestParam("sign_code") String signCode) {
        // 从数据库查询签章申请信息
        UAuthCASignRequest signRequest = uAuthCASignRequestRepository.findOne(signCode);
        if (signRequest == null) {
            return super.fail(ConstUtil.ERROR_SIGN_CODE_INVALID_CODE, ConstUtil.ERROR_SIGN_CODE_INVALID_MSG);
        }

        Map<String, Object> resultMap = new LinkedHashMap<>();
        resultMap.put("result", signRequest.getState());
        return super.success(resultMap);
    }

    /**
     * 注册CA系统用户
     *
     * @param params
     * @return
     */
    @RequestMapping(value = "/app/appServer/caRegister", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> caRegister(@RequestBody Map<String, Object> params) {
        try {
            String name = (String) params.get("name");
            String idnum = (String) params.get("idnum");
            String email = (String) params.get("email");
            String mobile = (String) params.get("mobile");
            return CAService.registerUser(name, idnum, email, mobile);
        } catch (Exception e) {
            e.printStackTrace();
            return fail("99", "未知异常");
        }
    }

    /**
     * 变更银行卡进行授权签章
     *
     * @param custNo
     * @param custName
     * @param certNo
     * @param cardNo
     * @param bankName
     * @return
     */
    @RequestMapping(value = "/app/appserver/bankCardGrant", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> convertBankCardGrant(@RequestParam("custNo") String custNo,
                                                    @RequestParam("custName") String custName, @RequestParam("certNo") String certNo,
                                                    @RequestParam("cardNo") String cardNo, @RequestParam("bankName") String bankName) {
        /**
         //通过流水号查询订单
         AppOrder appOrder = appOrderRepository.findByApplseq(applseq);
         if( null == appOrder){
         return fail("01","查询订单为空！");
         }**/
        // 查询当前用户信息
        String clientId = "A0000055B0FB82"; // 临时写死
        UAuthUserToken userToken = uAuthUserTokenRepository.findByClientId(clientId);
        String userId;
        if (userToken == null) {
            userId = "admin";
        } else {
            userId = userToken.getUserId();
        }
        String signCode = UUID.randomUUID().toString().replaceAll("-", "");

        // 签章申请信息保存到数据库
        UAuthCASignRequest signRequest = new UAuthCASignRequest();
        signRequest.setSignCode(signCode);
        signRequest.setOrderNo("");
        signRequest.setCustName(custName);
        signRequest.setCustIdCode(certNo);
        signRequest.setApplseq("");
        signRequest.setSignType(SignType.grant.toString()); // 变更银行卡授权书
        signRequest.setClientId(clientId);
        signRequest.setUserId(userId);
        signRequest.setSubmitDate(new Date());
        signRequest.setState("0");// 0 - 未处理
        signRequest.setTimes(0);
        signRequest.setCommonCustNo("");
        signRequest.setCommonCustName("");
        signRequest.setCommonCustCertNo("");
        signRequest.setCommonFlag("0");
        Map<String, Object> cardMap = new HashMap<>();
        Map<String, Object> cardInfo = new HashMap<>();
        cardInfo.put("custNo", custNo);
        cardInfo.put("custName", custName);
        cardInfo.put("certNo", certNo);
        cardInfo.put("cardNo", cardNo);
        cardInfo.put("bankName", bankName);
        cardMap.put("cardInfo", cardInfo);
        signRequest.setOrderJson(new JSONObject(cardMap).toString());
        uAuthCASignRequestRepository.save(signRequest);
        // 签章申请信息保存到redis队列左侧
//        RedisUtil.lpush(CommonProperties.get("other.redisKeyCA").toString(), signCode);

        return success();
    }

    @RequestMapping(value="/app/appserver/caCheckFourKeys", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object>  checkCaFourKeysInfo(@RequestParam("code")String code,
                                                    @RequestParam(value = "userId", required = false)String userId){
        if(StringUtils.isEmpty(code)){
            return fail("09", "code不能为空");
        }
        Map<String, Object> resultMap = cASignService.checkCaFourKeysInfo(code, userId);
        if (resultMap == null){
            return fail("11", "签章信息查询错误");
        }
        if(HttpUtil.isSuccess(resultMap)){
            return success(resultMap.get("body"));
        }
        HashMap<String, Object> head = (HashMap) resultMap.get("head");
        String retFlag = (String) head.get("retFlag");
        if("11".equals(retFlag) || "12".equals(retFlag)) {
            return fail(retFlag, "四要素认证不通过");
        }
        return fail((String)head.get("retFlag"),(String)head.get("retMsg"));
    }




}
