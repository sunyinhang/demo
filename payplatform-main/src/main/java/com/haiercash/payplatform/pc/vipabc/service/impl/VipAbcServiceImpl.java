package com.haiercash.payplatform.pc.vipabc.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.haiercash.core.lang.Base64Utils;
import com.haiercash.payplatform.common.dao.PublishDao;
import com.haiercash.payplatform.common.dao.VipAbcDao;
import com.haiercash.payplatform.common.data.VipAbcAppOrderGoods;
import com.haiercash.payplatform.common.data.Vipmessage;
import com.haiercash.payplatform.config.VipabcConfig;
import com.haiercash.payplatform.pc.vipabc.service.VipAbcService;
import com.haiercash.payplatform.service.AppServerService;
import com.haiercash.payplatform.service.CommonPageService;
import com.haiercash.payplatform.utils.Base64UtilsVIP;
import com.haiercash.payplatform.utils.DesUtil;
import com.haiercash.payplatform.utils.DesUtilvip;
import com.haiercash.payplatform.utils.EncryptUtil;
import com.haiercash.payplatform.utils.RSAUtils;
import com.haiercash.spring.redis.RedisUtils;
import com.haiercash.spring.service.BaseService;
import com.haiercash.spring.util.ConstUtil;
import com.haiercash.spring.util.HttpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2017/12/25.
 */
@Service
public class VipAbcServiceImpl extends BaseService implements VipAbcService {
    @Autowired
    private VipAbcDao vipAbcDao;
    @Autowired
    private AppServerService appServerService;
    @Autowired
    private CommonPageService commonPageService;
    @Autowired
    private VipabcConfig vipabcConfig;
    @Autowired
    private PublishDao publishDao;
    /**
     * 根据第三方订单号查询身份证号
     *
     * @param map
     * @return
     */
    @Override
    public Map<String, Object> getIdCardInfo(Map<String, Object> map) {
        logger.info("获取的参数为：" + map);
        HashMap<String, Object> mapIdCard = new HashMap<>();
        String ordersn = (String) map.get("ordersn");//vipabc方订单号
        String idcard = (String) map.get("idcard");
        List<VipAbcAppOrderGoods> idCards = vipAbcDao.selectIdCard(ordersn, idcard);
//        if (CollectionUtils.isEmpty(idCards))
//            mapIdCard.put("VipAbcAppOrderGoods", StringUtils.EMPTY);
//        else if (idCards.size() > 1)
//            return fail(ConstUtil.ERROR_CODE, "结果不唯一");
//        else
//            mapIdCard.put("VipAbcAppOrderGoods", idCards.get(0));

        return success(idCards);
    }

    /**
     * 判断用户是否注册
     *
     * @param token
     * @param channel
     * @param channelNo
     * @param params
     * @return
     */
    @Override
    public Map<String, Object> isRegister(String token, String channel, String channelNo, Map<String, Object> params) {
        logger.info("判断用户是否注册开始");
        Map<String, Object> resultparamMap = new HashMap<>();
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
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("channelNo", channelNo);
        paramMap.put("channel", channel);
        paramMap.put("mobile", userIdEncrypt);
        Map<String, Object> registerMap = appServerService.isRegister(token, paramMap);
        if (registerMap == null) {
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        Map resultmapjsonMap = (Map<String, Object>) registerMap.get("head");
        String resultmapFlag = (String) resultmapjsonMap.get("retFlag");
        if (!"00000".equals(resultmapFlag)) {
            String retMsg = (String) resultmapjsonMap.get("retMsg");
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }
        Map resultmapbodyMap = (Map<String, Object>) registerMap.get("body");
        String provider = (String) resultmapbodyMap.get("provider");
        if ("HAIERUAC".equals(provider)) {
            resultparamMap.put("flag", "你是海尔会员，请直接登录");
            success(resultparamMap);
        }
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
     * 刷新海尔会员验证码
     *
     * @param channel
     * @param channelNo
     * @return
     */
    public Map<String, Object> haierCaptcha(String channel, String channelNo) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("channel", channel);
        map.put("channelNo", channelNo);
        logger.info("根据客户信息查询额度审批进度,请求数据:" + map);
        Map<String, Object> haierCaptcha = appServerService.getHaierCaptcha("", map);
        if (StringUtils.isEmpty(haierCaptcha)) {
            logger.info("获取验证码信息为空" + haierCaptcha);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        Map<String, Object> headMap = (Map) haierCaptcha.get("head");
        String retflag = (String) headMap.get("retFlag");
        String retmsg = (String) headMap.get("retMsg");
        if (!"00000".equals(retflag)) {
            logger.info("获取验证码返回信息错误" + retflag);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        Map<String, Object> bodyMap = (Map) haierCaptcha.get("body");
        String captchaImage = (String) bodyMap.get("captchaImage");
        String captchaToken = (String) bodyMap.get("captchaToken");
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("captchaImage", captchaImage);//验证码图片
        hashMap.put("captchaToken", captchaToken);//验证码token
        return success(hashMap);
    }


    /**
     * 登录
     *
     * @param channel
     * @param channelNo
     * @param params
     * @return
     */
    public Map<String, Object> vipAbcLogin(String channel, String channelNo, Map<String, Object> params) {
        Map<String, Object> redisMap = new HashMap<String, Object>();
        Map hashMap = new HashMap();
        Map<String, Object> haierCaptcha = null;
        String captchaAnswer = null;
        String captchaToken = null;
        String uuid = (String) params.get("uuid");//第三方传送的uuid
        if (StringUtils.isEmpty(uuid)) {
            logger.info("uuid为空");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        Map<String, Object> expireUuid = RedisUtils.getExpireMap(uuid);
        if (StringUtils.isEmpty(expireUuid)) {
            logger.info("从redis中获取的数据为空" + expireUuid);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        Map<String, Object> map1 = (Map<String, Object>) expireUuid.get("appOrderGoods");
        String orderSn = (String) map1.get("orderSn");// 订单号
        logger.info("第三方订单号为：" + orderSn);
        String userId = (String) params.get("userId");//账号
        if (StringUtils.isEmpty(userId)) {
            logger.info("登录账号为空");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        String pwd = (String) params.get("pwd");//密码
        if (StringUtils.isEmpty(pwd)) {
            logger.info("登录密码为空");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        captchaAnswer = (String) params.get("captchaAnswer");//验证码
        logger.info("前端获取的验证码为" + captchaAnswer);
        captchaToken = (String) params.get("captchaToken");//验证码token
        logger.info("前端获取的验证码token为" + captchaToken);
        Map result = appServerService.customerLoginCaptcha(EncryptUtil.simpleEncrypt(userId), EncryptUtil.simpleEncrypt(pwd), channel, channelNo, captchaAnswer, captchaToken);
        if (StringUtils.isEmpty(result)) {
            logger.info("登录接口返回信息为空");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        Map head = (Map<String, Object>) result.get("head");
        if (StringUtils.isEmpty(head)) {
            logger.info("登录获取的head信息为空：" + head);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        String retFlag = (String) head.get("retFlag");
        if ("U0173".equals(retFlag)) {//集团在一定次数后返回的验证码标识（这边测试是三次后会返回）
            Map<String, Object> mapone = new HashMap<String, Object>();
            mapone.put("channel", channel);
            mapone.put("channelNo", channelNo);
            logger.info("根据客户信息查询额度审批进度,请求数据:" + mapone);
            haierCaptcha = haierCaptcha(channel, channelNo);
            if (StringUtils.isEmpty(haierCaptcha)) {
                logger.info("获取验证码信息为空" + haierCaptcha);
                return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
            }
        }
        //登录失败
        if (!"00000".equals(retFlag)) {
            return success(haierCaptcha);
        }
        Map body2 = (Map<String, Object>) result.get("body");
        if (StringUtils.isEmpty(body2)) {
            logger.info("登录获取的body信息为空：" + body2);
        }
        Map<String, Object> map2 = (Map<String, Object>) body2.get("token");
        String token = (String) map2.get("access_token");
        String isRealInfo = (String) body2.get("isRealInfo");
        userId = (String) body2.get("userId");
        //redis存储数据
        hashMap.put("token", token);
        redisMap.put("userId", userId);
        logger.info("登录存储的userId:" + userId);
        //获取绑定手机号
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("userId", EncryptUtil.simpleEncrypt(userId));
        map.put("channel", channel);
        map.put("channelNo", channelNo);
        Map<String, Object> result0 = appServerService.getMobile(token, map);
        if (StringUtils.isEmpty(result0)) {
            logger.info("获取绑定手机号返回信息为空");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        Map head1 = (Map<String, Object>) result0.get("head");
        if (StringUtils.isEmpty(head1)) {
            logger.info("获取绑定手机号获取head信息为空：" + head);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        String retflag0 = (String) head1.get("retFlag");
        String retMsg = (String) head1.get("retMsg");
        if (!"00000".equals(retflag0)) {
            return fail(retflag0, retMsg);
        }
        Map body3 = (Map<String, Object>) result0.get("body");
        if (StringUtils.isEmpty(body3)) {
            logger.info("获取绑定手机号获取body信息为空：" + body3);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        String phone = (String) body3.get("mobile");
        redisMap.put("phoneNo", phone);//绑定手机号
        //未做过实名认证，跳实名认证页面
//            if(!"Y".equals(isRealInfo)){
        if (true) {
            map.put("flag", "2");//实名认证
            RedisUtils.setExpire(token, redisMap);
            return success();
        }
        //查询实名认证信息
        Map json = (Map<String, Object>) body2.get("realInfo");
        redisMap.put("name", json.get("custName"));
        redisMap.put("idCard", json.get("certNo"));//身份证号
        redisMap.put("custNo", json.get("custNo"));//客户编码crm
        redisMap.put("cardNo", json.get("cardNo"));//银行卡号
        redisMap.put("bankCode", json.get("acctBankNo"));//银行代码
        redisMap.put("bankName", json.get("acctBankName"));//银行名称
        redisMap.put("cardPhone", json.get("mobile"));//实名认证手机号
        //查询全部订单开始
        //查询是否有待提交订单
        //{"head":{"retFlag":"00000","retMsg":"处理成功"},"body":{"orders":[{"orderNo":"6af071fc-c369-449b-9d11-bdcca2b3433e","applyTnrTyp":"18","applyDt":"2017-03-15","cooprName":"外联平台1","fee":"58.50","custName":"李华","cooprCde":"302017040190","applyAmt":"500.00","goodsCount":1,"mthAmt":"31.03","typGrp":"01","goodsName":"美白嫩肤","applyTnr":"18"},{"orderNo":"86003b83-9a9b-42f7-9b21-b17fabb23096","applyTnrTyp":"18","applyDt":"2017-03-14","cooprName":"外联平台1","fee":"35.10","custName":"李华","cooprCde":"302017040190","applyAmt":"300.00","goodsCount":1,"mthAmt":"18.62","typGrp":"01","goodsName":"美白嫩肤","applyTnr":"18"},{"orderNo":"9f6fc240-3d7f-4aa8-a978-69f29b7666f0","applyTnrTyp":"24","applyDt":"2017-03-14","cooprName":"外联平台1","fee":"54.00","custName":"李华","cooprCde":"302017040190","applyAmt":"300.00","goodsCount":1,"mthAmt":"14.75","typGrp":"01","goodsName":"美白嫩肤","applyTnr":"24"}]}}
        Map<String, Object> req = new HashMap<String, Object>();
        req.put("channelNo", channelNo);
        req.put("channel", channel);
        req.put("custNo", json.get("custNo").toString());
//        req.put("page", String.valueOf(page));
//        req.put("size", String.valueOf(size));
        logger.info("查询待提交订单列表接口，请求数据：" + req.toString());
        Map<String, Object> haveOrder = appServerService.getWtjAppOrderCust(token, req);
        if (StringUtils.isEmpty(haveOrder)) {
            logger.info("查询待提交订单接口返回信息为空");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        Map head2 = (Map<String, Object>) haveOrder.get("head");
        String retFlag1 = (String) head2.get("retFlag");
        String retMsg1 = (String) head2.get("retMsg");
        if (!"00000".equals(retFlag1)) {
            return fail(ConstUtil.ERROR_CODE, retMsg1);
        }
        Map body1 = (Map<String, Object>) haveOrder.get("body");
        JSONArray orderArray1 = JSONArray.parseArray((String) body1.get("orders"));//未提交订单列表
        //查询全部订单列表（已提交）
        Map reqs = new HashMap<String, Object>();
        reqs.put("channel", channel);
        reqs.put("crtUsr", userId);
        reqs.put("idNo", json.get("certNo"));
//        reqs.put("page", String.valueOf(page));
//        reqs.put("size", String.valueOf(size));
        logger.info("查询全部贷款信息列表接口，请求数据：" + reqs.toString());
        Map strOrder = appServerService.getDateAppOrderPerson(token, reqs);
        if (StringUtils.isEmpty(strOrder)) {
            logger.info("查询是否有已提交订单接口返回信息为空！");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        Map head3 = (Map<String, Object>) strOrder.get("head");
        String retFlag4 = (String) head3.get("retFlag");
        String retMsg2 = (String) head3.get("retMsg");
        if (!"00000".equals(retFlag4)) {
            return fail(ConstUtil.ERROR_CODE, retMsg2);
        }
        Map body = (Map<String, Object>) strOrder.get("body");
        List orderArray = (List) body.get("orders");
        //没有订单信息，跳转分期申请页面
        String queryvip = vipAbcDao.queryvipordersnandorderno(orderSn);//第一次进来的订单
        if ((orderArray.size() == 0 && orderArray1.size() == 0) || "1".equals(queryvip)) {
            map.put("flag", "1");//分期申请
            RedisUtils.setExpire(token, redisMap);
            return success(map);
        }
        //有订单信息跳转分期详情页面
        map.put("flag", "3");//订单列表
        redisMap.put("waitOrderArray", orderArray1.toString());//未提交订单
        redisMap.put("orderArray", orderArray.toString());//已提交订单
        RedisUtils.setExpire(token, redisMap);
        return success(map);
    }

    /**
     * 分期申请，订单保存
     *
     * @param token
     * @param channel
     * @param channelNo
     * @param params
     * @return
     * @throws Exception
     */
    public Map<String, Object> vipAbcsaveOrderServlet(String token, String channel, String channelNo, Map<String, Object> params) throws Exception {
        logger.info("VIPABC分期申请，订单保存开始");
        String orderNo = "";
        String reqData = "";
        String province = (String) params.get("province");//省名称
        logger.info("获取的省名称是：" + province);
        String city = (String) params.get("city");//市名称
        logger.info("获取的省、市名称是：" + city);
        String district = (String) params.get("district");//区名称
        logger.info("获取的区名称是：" + district);
        if (StringUtils.isEmpty(token)) {
            logger.info("token为空");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        Map<String, Object> redisMap = RedisUtils.getExpireMap(token);
        if (StringUtils.isEmpty(redisMap)) {
            logger.info("分期申请还款计划加载，redis缓存数据获取失败！");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        String orderFlags = (String) params.get("orderFlags");
        logger.info("获取的orderFlags：" + orderFlags);
        String idCard = (String) redisMap.get("idCard");//身份证号
        String uuid = (String) params.get("uuid");
        String applSeqth = (String) redisMap.get("applSeqth");
        Object object = RedisUtils.getExpireMap(uuid);
        if (StringUtils.isEmpty(object)) {
            logger.info("从redis中获取的数据为空" + object);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        Map jsonObject1 = (Map<String, Object>) object;
        String topLevel = (String) jsonObject1.get("topLevel");// 课程名称
        String goodsName = (String) jsonObject1.get("model");// 课程名称
        String orderSn = (String) jsonObject1.get("orderSn");// 订单号
        logger.info("第三方订单号为：" + orderSn);
        String queryvipordersnandorderno2 = vipAbcDao.queryvipordersnandorderno(orderSn);
        int parseInt = Integer.parseInt(queryvipordersnandorderno2);
        String orderFlag = (String) redisMap.get("orderFlag");//自己内部修改标识
        String typCde = (String) params.get("loanCode");
        if (StringUtils.isEmpty(typCde)) {
            logger.info("贷款品种不能为空");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        String applyAmt = (String) params.get("applyAmt");
        if (StringUtils.isEmpty(applyAmt)) {
            logger.info("申请金额不能为空");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        double applyAmt0 = Double.parseDouble(applyAmt);
        if (applyAmt0 > 50000) {
            logger.info("申请金额不能超过5万");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        String applyTnr = (String) params.get("applyTnr");
        if (StringUtils.isEmpty(applyTnr)) {
            logger.info("借款期限不能为空");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        String totalnormint = (String) params.get("totalNormInt");
        if (StringUtils.isEmpty(totalnormint)) {
            logger.info("总利息金额不能为空");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        String totalfeeamt = (String) params.get("totalFeeAmt");
        if (StringUtils.isEmpty(totalfeeamt)) {
            logger.info("费用总额不能为空");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        String StoreNo = (String) params.get("StoreNo");
        if (StringUtils.isEmpty(StoreNo)) {
            logger.info("门店编号为空");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        String merchNo = (String) params.get("merchNo");
        if (StringUtils.isEmpty(merchNo)) {
            logger.info("商户编号为空");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        String typLevelTwo = (String) params.get("typLvlCde");// 贷款品种小类
        if (typLevelTwo == null || "".equals(typLevelTwo)) {
            logger.info("贷款品种小类不能为空");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        Map<String, Object> areaCodeMap = commonPageService.getAreaCode(province, city, district);
        logger.info("通过省市区获取的对应编码结果：" + areaCodeMap);
        String provinceCode = (String) areaCodeMap.get("province");
        String cityCode = (String) areaCodeMap.get("city");
        String userId = (String) redisMap.get("userId");
        //录单校验
        Map<String, Object> ordercheakmap = new HashMap<>();
        ordercheakmap.put("userId", userId);
        ordercheakmap.put("provinceCode", provinceCode);
        ordercheakmap.put("typLevelTwo", typLevelTwo);
        ordercheakmap.put("cityCode", cityCode);
        ordercheakmap.put("channel", channel);
        ordercheakmap.put("channelNo", channelNo);
        Map<String, Object> result1 = appServerService.getCustInfoAndEdInfoPerson(token, ordercheakmap);
        if (StringUtils.isEmpty(result1)) {
            logger.info("录单校验接口,返回信息为空");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        Map head = (Map<String, Object>) result1.get("head");
        if (StringUtils.isEmpty(head)) {
            logger.info("录单校验返回head信息为空：" + head);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        String retflag1 = (String) head.get("retFlag");
        String retMsg = (String) head.get("retMsg");
        if (!"00000".equals(retflag1)) {
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        // 是否允许申请贷款
        SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd");
        String date = dateFormater.format(new Date());
        Map<String, Object> queryordermap = new HashMap<>();
        queryordermap.put("typCde", typCde);
        queryordermap.put("date", date);
        queryordermap.put("channel", channel);
        queryordermap.put("channelNo", channelNo);
        Map<String, Object> result2 = appServerService.queryBeyondContral(token, queryordermap);
        if (StringUtils.isEmpty(result2)) {
            logger.info("是否允许申请贷款接口，返回信息为空");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        Map head1 = (Map<String, Object>) result2.get("head");
        if (StringUtils.isEmpty(head1)) {
            logger.info("是否允许申请贷款返回head信息为空：" + head1);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        String retFlag = (String) head1.get("retFlag");
        String retMsg2 = (String) head1.get("retMsg");
        if (!"00000".equals(retFlag)) {
            logger.info("是否允许申请贷款返回状态码错误" + retMsg2);
            return fail(ConstUtil.ERROR_CODE, retMsg2);
        }
        Map body = (Map<String, Object>) result2.get("body");
        if (StringUtils.isEmpty(body)) {
            logger.info("是否允许申请贷款返回body信息为空" + body);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        String flag = (String) body.get("flag");
        if (!"Y".equals(flag)) {
            return fail("0008", "不允许申请贷款");
        }
        HashMap<String, Object> objectObjectHashMap = new HashMap<>();
        objectObjectHashMap.put("storeNo", StoreNo);
        objectObjectHashMap.put("channel", channel);
        objectObjectHashMap.put("channelNo", channelNo);
        Map<String, Object> userIdByStoreNo = appServerService.findUserIdByStoreNo(token, objectObjectHashMap);
        if (StringUtils.isEmpty(userIdByStoreNo)) {
            logger.info("获取销售代表信息接口，返回数据为空");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        Map head2 = (Map<String, Object>) userIdByStoreNo.get("head");
        String retFlag1 = (String) head2.get("retFlag");
        if (!"00000".equals(retFlag1)) {
            String retMsg3 = (String) head2.get("retMsg");
            return fail(retFlag1, retMsg3);
        }
        List jsonArray = (List) userIdByStoreNo.get("body");
        String crtUsr = "";
        for (int i = 0; i < jsonArray.size(); i++) {
            Object objectone = jsonArray.get(i);
            String s4 = JSONObject.toJSONString(objectone);
            JSONObject jsonObject = JSONObject.parseObject(s4);
            crtUsr = (String) jsonObject.get("userId");
        }
        String custNo = (String) redisMap.get("custNo");
        // 查询是订单保存or订单修改
        if ("2".equals(orderFlags) || "1".equals(orderFlag)) {// 订单修改
            logger.info("进入订单修改");
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("typCde", typCde);
            map.put("token", token);
            map.put("channel", channel);
            map.put("channelNo", channelNo);
            Map<String, Object> map1 = appServerService.pLoanTyp(token, map);
            if (StringUtils.isEmpty(map1)) {
                logger.info("查询贷款品种详情，返回信息为空");
                return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
            }
            Map head3 = (Map<String, Object>) map1.get("head");
            String retFlagone = (String) head3.get("retFlag");
            if (!"00000".equals(retFlagone)) {
                String msg = (String) head3.get("retMsg");
                return fail("0006", msg);
            }
            Map bodyJs = (Map<String, Object>) map1.get("body");
            double minAmt = Double.parseDouble((String) bodyJs.get("minAmt"));// token
            double maxAmt = Double.parseDouble((String) bodyJs.get("maxAmt"));
            double apprvAmt = Double.parseDouble(applyAmt);
            if (apprvAmt < minAmt) {
                return fail("0006", "借款金额不可少于" + minAmt + "元");
            } else if (apprvAmt > maxAmt) {
                return fail("0006", "借款金额最多不可超过" + maxAmt + "元");
            }
            JSONObject jb = new JSONObject();
            orderNo = (String) params.get("orderNo");
            logger.info("获取的订单号为：" + orderNo);
            jb.put("orderNo", orderNo);// 订单编号
            jb.put("typCde", typCde);// 贷款品种代码
            jb.put("fstPay", "0");// 首付金额
            jb.put("applyAmt", applyAmt);// 借款总额
            jb.put("applyTnr", applyTnr);// 借款期限
            jb.put("applyTnrTyp", applyTnr);// 借款期限类型
            jb.put("totalnormint", totalnormint);// 总利息金额
            jb.put("totalfeeamt", totalfeeamt);// 费用总额

            String deliverProvince = (String) redisMap.get("deliverProvince");
            String deliverCity = (String) redisMap.get("deliverCity");
            String deliverArea = (String) redisMap.get("deliverArea");
            String deliverAddr = (String) redisMap.get("deliverAddr");
            String deliverAddrTyp = (String) redisMap.get("deliverAddrTyp");
            jb.put("deliverAddrTyp", deliverAddrTyp);// 送货地址类型(TODO!!!!)
            jb.put("deliverProvince", deliverProvince);// 送货地址省代码
            jb.put("deliverCity", deliverCity);// 送货地址市代码
            jb.put("deliverArea", deliverArea);// 送货地址区代码
            jb.put("deliverAddr", deliverAddr);// 送货地址
            String cardNo = (String) redisMap.get("cardNo");
            String bankCode = (String) redisMap.get("bankCode");
            String bankName = (String) redisMap.get("bankName");
            jb.put("repayApplCardNo", cardNo);// 还款卡号
            jb.put("proPurAmt", applyAmt);// 商品总额
            jb.put("goodsBrand", vipabcConfig.getGoodsBrand());// 商品品牌
            jb.put("goodsKind", vipabcConfig.getGoodsKind());// 商品类型
            jb.put("goodsName", topLevel + "," + goodsName);// 商品名称 改动
            jb.put("goodsModel", topLevel + "," + goodsName);// 商品型号
            jb.put("goodsNum", 1);// 商品数量
            jb.put("goodsPrice", applyAmt);// 商品单价
            jb.put("merchNo", merchNo);// 商户编号
            jb.put("cooprCde", StoreNo);// 门店编号
            jb.put("token", token);
            jb.put("mallOrderNo", orderSn);//第三方商城号
            jb.put("channel", channel);
            jb.put("channelNo", channelNo);
            jb.put("source", channel);// 订单来源
            jb.put("acqCreditType", "00");//00：单纯贷款        01：额度申请加支用        02：支用
            redisMap.put("orderNo", orderNo);
            RedisUtils.set(token, redisMap, 30, TimeUnit.DAYS);
            Map<String, Object> result3 = appServerService.updateAppOrder(token, jb);
            if (StringUtils.isEmpty(result3)) {
                logger.info("订单修改接口，返回信息为空");
                return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
            }
            Map head4 = (Map<String, Object>) result3.get("head");
            String retFlag3 = (String) head4.get("retFlag");
            if (!"00000".equals(retFlag3)) {
                String msg = (String) head4.get("retMsg");
                return fail(retFlag3, msg);
            }
            Map body1 = (Map<String, Object>) result3.get("body");
            String applSeq = (String) body1.get("applSeq");
            redisMap.put("applSeq", applSeq);
            redisMap.put("applyAmt", applyAmt);
            redisMap.put("typCde", typCde);
            RedisUtils.set(token, redisMap, 30, TimeUnit.DAYS);
            return success();
        } else if (parseInt == 1) {//退回单子保存  //第一次订单保存
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("typCde", typCde);
            map.put("token", token);
            map.put("channel", channel);
            map.put("channelNo", channelNo);
            Map<String, Object> map1 = appServerService.pLoanTyp(token, map);
            if (StringUtils.isEmpty(map1)) {
                logger.info("查询贷款品种详情，返回信息为空");
                return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
            }
            Map head3 = (Map<String, Object>) map1.get("head");
            String retFlagone = (String) head3.get("retFlag");
            if (!"00000".equals(retFlagone)) {
                String msg = (String) head3.get("retMsg");
                return fail("0006", msg);
            }
            Map bodyJs = (Map<String, Object>) map1.get("body");
            double minAmt = Double.parseDouble((String) bodyJs.get("minAmt"));// token
            double maxAmt = Double.parseDouble((String) bodyJs.get("maxAmt"));
            double apprvAmt = Double.parseDouble(applyAmt);
            if (apprvAmt < minAmt) {
                return fail("0006", "借款金额不可少于" + minAmt + "元");
            } else if (apprvAmt > maxAmt) {
                return fail("0006", "借款金额最多不可超过" + maxAmt + "元");
            }
            JSONObject jb = new JSONObject();
            jb.put("merchNo", merchNo);// 商户编号
            jb.put("cooprCde", StoreNo);// 门店编号
            jb.put("crtUsr", crtUsr);// 销售代表用户ID（）
            jb.put("typGrp", "01");// 贷款类型
            jb.put("custNo", custNo);// 客户编号
            jb.put("typCde", typCde);// 贷款品种代码
            jb.put("applyAmt", applyAmt);// 借款总额
            jb.put("applyTnr", applyTnr);// 借款期限
            jb.put("applyTnrTyp", applyTnr);// 借款期限
            jb.put("totalnormint", totalnormint);// 总利息金额
            jb.put("totalfeeamt", totalfeeamt);// 费用总额
            jb.put("source", channel);// 订单来源 (TODO!!!!)
            jb.put("WhiteType", "SHH");// 白名单类型(TODO!!!!)
            jb.put("goodsBrand", vipabcConfig.getGoodsBrand());// 商品品牌
            jb.put("goodsKind", vipabcConfig.getGoodsKind());// 商品类型
            jb.put("goodsName", topLevel + "," + goodsName);// 商品名称
            jb.put("goodsModel", topLevel + "," + goodsName);// 商品型号
            jb.put("goodsNum", 1);// 商品数量
            jb.put("goodsPrice", applyAmt);// 商品单价
            jb.put("token", token);
            jb.put("channel", channel);
            jb.put("channelNo", channelNo);
            jb.put("mallOrderNo", orderSn);//第三方商城号
            jb.put("acqCreditType", "00");//00：单纯贷款        01：额度申请加支用        02：支用      99: 未知
            // 订单保存
            Map<String, Object> result3 = appServerService.saveAppOrderInfo(token, jb);
            System.out.println("订单保存=====>" + result3);
            if (StringUtils.isEmpty(result3)) {
                logger.info("订单保存接口，返回信息为空");
                return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
            }
            Map<String, Object> map3 = (Map<String, Object>) result3.get("head");
            String retFlag3 = (String) map3.get("retFlag");
            if (!"00000".equals(retFlag3)) {
                String msg = (String) map3.get("retMsg");
                return fail(retFlag3, msg);
            }
            Map<String, Object> map4 = (Map<String, Object>) result3.get("body");
            orderNo = (String) map4.get("orderNo");// 编号订单
            String applSeq = (String) map4.get("applSeq");// 申请流水号
            String applCde = (String) map4.get("applCde");// 申请编号
            VipAbcAppOrderGoods vipAbcAppOrderGoods = new VipAbcAppOrderGoods();
            vipAbcAppOrderGoods.setApplseq(applSeq);// 流水号
            vipAbcAppOrderGoods.setOrderNo(orderNo);// 订单号
            vipAbcAppOrderGoods.setOrderSn(orderSn);// 第三方订单号
            vipAbcAppOrderGoods.setIdCard(idCard);//身份证号
            vipAbcDao.updatevipapplseq(vipAbcAppOrderGoods);// 把生成的流水号与订单号存入数据库
            Vipmessage vipmessage = new Vipmessage();
            vipmessage.setUuid(uuid);
            vipmessage.setOrdersn(orderNo);
            vipAbcDao.updateordersn(vipmessage);//把生成的订单号根据uuid保存到vipabc_info（控制二维码失效表）表中
            redisMap.put("orderNo", orderNo);
            redisMap.put("applSeq", applSeq);
            redisMap.put("applCde", applCde);
            redisMap.put("applyAmt", applyAmt);
            redisMap.put("typCde", typCde);
            RedisUtils.set(token, redisMap, 30, TimeUnit.DAYS);
            //订单保存成功推送成功消息
            String url = publishDao.selectChannelNoUrl(channelNo);
            logger.info("HaiercashStateApply,渠道编码" + channelNo + "的推送地址为：" + url);
            if (StringUtils.isEmpty(url)) {
                return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
            }
            String productKey = DesUtil.productKey();
            String key = productKey;
            String iv = productKey;
            HashMap<String, Object> mapone = new HashMap<>();
            HashMap<String, Object> map2 = new HashMap<>();
            SimpleDateFormat my = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date dateone = new Date();
            String loantime = my.format(dateone);
            mapone.put("orderSn", orderSn);
            map2.put("applSeq", applSeq);
            map2.put("idNo", "");
            map2.put("outSts", "05");
            map2.put("orderNo", orderNo);
            map2.put("appOutAdvice", "");
            map2.put("loantime", loantime);
            mapone.put("body", map2);
            logger.info("加密前的推送数据是" + mapone);
            reqData = com.alibaba.fastjson.JSONObject.toJSONString(mapone);
            String publicKey = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAKJH9SKW/ZNJjll0ZKTsxdsPB+r+EjDS8XP/d2EmgncrR8xVbckp9iksuHM0ckw5bk84P+5YH2mIf8cDRoBSJykCAwEAAQ==";//公钥
            String sKey = key;
            String desData = EncryptUtil.DesEncrypt(reqData, sKey, iv);
            //3.加密des的key
            String password_ = Base64Utils.encode(RSAUtils.encryptByPublicKey((productKey + productKey).getBytes(), publicKey));
            JSONObject reqjson = new JSONObject();
            reqjson.put("applyNo", UUID.randomUUID().toString().replace("-", ""));
            reqjson.put("channelNo", "53");
            reqjson.put("tradeCode", "vipabc-10002");
            reqjson.put("data", desData);
            reqjson.put("key", password_);
            reqData = com.alibaba.fastjson.JSONObject.toJSONString(reqjson);
            logger.info("推送的报文是：" + reqData);
            JSONObject jsonObject = JSONObject.parseObject(reqData);
            Map<String, Object> map5 = HttpUtil.restPostMap(url, jsonObject);
            logger.info("订单保存推送返回结果" + jsonObject);
        } else {
            return fail("1111", "数据库中已存在相同的单子，请重新生一个新的ordersn,重新录单" + orderSn);
        }
        return success();
    }

    /**
     * 分期详情
     *
     * @param token
     * @param channel
     * @param channelNo
     * @param params
     * @return
     */
    public Map<String, Object> vipAbcpayApplyInfo(String token, String channel, String channelNo, Map<String, Object> params) {
        logger.info("VIPABC分期详情||订单列表开始");
        Map<String, Object> redisMap = new HashMap<String, Object>();
        logger.info("VIPABC分期详情的token=" + token);
        if (token == null || "".equals(token)) {
            logger.info("token为空");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        redisMap = (Map<String, Object>) RedisUtils.getExpireMap(token);
        if (StringUtils.isEmpty(redisMap)) {
            logger.info("分期详情加载，redis缓存数据获取失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        String bankName = (String) redisMap.get("bankName");
        String cardNo = (String) redisMap.get("cardNo");
        String applSeq0 = (String) params.get("applSeq");
        // 进入分期详情
        if (!StringUtils.isEmpty(applSeq0)) {//
            logger.info("进入分期详情");
            Map<String, Object> req = new HashMap<>();
            req.put("channelNo", channelNo);
            req.put("channel", channel);
            req.put("applSeq", applSeq0);
            logger.info("查询贷款详情接口，请求数据：" + req.toString());
            Map<String, Object> order = appServerService.queryApplLoanDetail(token, req);// 查询贷款详情
            if (StringUtils.isEmpty(order)) {
                logger.info("查询订单详情接口返回为空！");
                return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
            }
            Map<String, Object> map = (Map<String, Object>) order.get("head");
            String retFlag2 = (String) map.get("retFlag");
            if (!"00000".equals(retFlag2)) {
                String retMsg = (String) map.get("retMsg");
                return fail(retFlag2, retMsg);
            }
            Map<String, Object> map1 = (Map<String, Object>) order.get("body");
            String applyDt = (String) map1.get("applyDt");// 申请日期
            String applyAmt = (String) map1.get("applyAmt"); // 申请金额
            String typCde = (String) map1.get("loanTyp");// 贷款品种代码
            String loanTypName = (String) map1.get("loanTypName");// 贷款品种名称
            String cooprName = (String) map1.get("cooprName");// 门店名称
            String payMtdDesc = (String) map1.get("payMtdDesc");// 分期详情
            String applyTnr = (String) map1.get("applyTnr");// 借款期限
            String merchNo = (String) map1.get("superCoopr");// 商户编码
            String cooprCde = (String) map1.get("cooprCde");// 门店编码
            List array = (List) map1.get("goods");// 门店编码
            String goodsKind = "";
            String goodsBrand = "";
            String goodsName = "";
            for (int j = 0; j < array.size(); j++) {
                Map<String, Object> jsonObj = (Map<String, Object>) array.get(j);
                // goodsCode = jsonObj.get("goodsCode").toString();//商品编号
                goodsName = (String) jsonObj.get("goodsName");
                goodsKind = (String) jsonObj.get("goodsKind");
                goodsBrand = (String) jsonObj.get("goodsBrand");
            }
            String[] split = goodsName.split(",");
            String topLevel = split[0];
            String goodsNameone = split[1];
            Map m = new HashMap<>();
            m.put("goodsName", goodsNameone);
            m.put("merchNo", merchNo);
            m.put("cooprCde", cooprCde);
            m.put("cooprName", cooprName);
            m.put("payMtdDesc", payMtdDesc);
            m.put("typCde", typCde);
            m.put("applyAmt", applyAmt);
            m.put("applyTnr", applyTnr);
            m.put("goodsKind", goodsKind);
            m.put("goodsBrand", goodsBrand);
            m.put("applyDt", applyDt);
            m.put("loanName", loanTypName);
            m.put("bankName", bankName);// 银行名称
            m.put("cardNo", cardNo);// 卡号
            m.put("topLevel", topLevel);//
            return success(m);
        }
        logger.info("进入我的订单");
        // 进入我的订单
        List orderlist = new ArrayList<>();// 存放订单列表
        List<Map<String, Object>> orderArr = new ArrayList<Map<String, Object>>();// 存放OrderNo
        // 已提交订单
        List orderArray = (List) redisMap.get("orderArray");//
        // 待提交订单
        List waitOrderArray = (List) redisMap.get("waitOrderArray");//
        logger.info("待提交订单===>" + waitOrderArray);
        // 遍历待提交订单编号
        for (int i = 0; i < waitOrderArray.size(); i++) {
            Object object = waitOrderArray.get(i);
            if (object == null) {
                break;
            }
            Map<String, Object> jsonObject = (Map<String, Object>) object;
            String orderNo = (String) jsonObject.get("orderNo");// 订单编号
            String outSts = "待提交";
            // 查询订单详情
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("orderNo", orderNo);
            map.put("channel", channel);
            map.put("channelNo", channelNo);
            Map<String, Object> orderInfo = appServerService.queryOrderInfo(token, map);
            if (StringUtils.isEmpty(orderInfo)) {
                logger.info("查询订单详情接口返回信息为空！");
                return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
            }
            Map<String, Object> map1 = (Map<String, Object>) orderInfo.get("head");
            String retFlag2 = (String) map1.get("retFlag");
            if (!"00000".equals(retFlag2)) {
                String retMsg = (String) map1.get("retMsg");
                return fail(retFlag2, retMsg);
            }
            Map<String, Object> body = (Map<String, Object>) map1.get("body");
            String merchNo = (String) body.get("merchNo");// 商户编码
            String cooprCde = (String) body.get("cooprCde");// 门店编码
            String cooprName = (String) body.get("cooprName");// 门店名称
            String payMtdDesc = (String) body.get("payMtdDesc");// 分期方式
            String typCde = (String) body.get("typCde");// 贷款品种
            String applyAmt = (String) body.get("applyAmt");// 申请金额
            String applyTnr = (String) body.get("applyTnr");// 期限
            String applyDt = (String) body.get("applyDt");// 申请日期
            String applSeq = (String) body.get("applseq");//流水号
            List array = (List) (body.get("goods"));
            String goodsKind = "";
            String goodsBrand = "";
            String goodsName = "";
            for (int j = 0; j < array.size(); j++) {
                Map<String, Object> jsonObj = (Map<String, Object>) array.get(j);
                goodsName = jsonObj.get("goodsName").toString();
                goodsKind = (String) jsonObj.get("goodsKind");
                goodsBrand = (String) jsonObj.get("goodsBrand");
            }
            String[] strs = goodsName.split(",");
            String topLevel = strs[0];
            String goodsNameone = strs[1];
            Map m = new HashMap<>();
            m.put("orderNo", orderNo);
            m.put("goodsName", goodsNameone);
            m.put("applSeq", applSeq);
            m.put("outSts", outSts);
            m.put("merchNo", merchNo);
            m.put("cooprCde", cooprCde);
            m.put("cooprName", cooprName);
            m.put("payMtdDesc", payMtdDesc);
            m.put("typCde", typCde);
            m.put("applyAmt", applyAmt);
            m.put("applyTnr", applyTnr);
            m.put("goodsKind", goodsKind);
            m.put("goodsBrand", goodsBrand);
            m.put("applyDt", applyDt);
            m.put("topLevel", topLevel);
            orderlist.add(m);
        }
        // 查询贷款详情
        for (int i = 0; i < orderArray.size(); i++) {
            Map<String, Object> jsonObject = (Map<String, Object>) orderArray.get(i);
            String orderNo = (String) jsonObject.get("orderNo");// 订单编号
            String outSts = (String) jsonObject.get("outSts");// 贷款状态
            String applSeq = jsonObject.get("applSeq").toString();// 申请流水号
            outSts = getState(outSts);
            Map<String, Object> req = new HashMap<>();
            req.put("channelNo", channelNo);
            req.put("channel", channel);
            req.put("applSeq", applSeq0);
            logger.info("查询贷款详情接口，请求数据：" + req.toString());
            Map<String, Object> order = appServerService.queryApplLoanDetail(token, req);// 查询贷款详情
            if (StringUtils.isEmpty(order)) {
                logger.info("查询贷款详情返回信息为空");
                return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
            }
            Map<String, Object> map = (Map<String, Object>) order.get("head");
            String retFlag2 = (String) map.get("retFlag");
            if (!"00000".equals(retFlag2)) {
                String retMsg = (String) map.get("retMsg");
                return fail(retFlag2, retMsg);
            }
            // 屏蔽VIPABC
            Map<String, Object> map1 = (Map<String, Object>) order.get("body");
            String loanTyp = (String) map1.get("loanTyp");//贷款品种
            if (!("17013a".equals(loanTyp) || "17014a".equals(loanTyp) || "16076a".equals(loanTyp)
                    || "16077a".equals(loanTyp) || "17051a".equals(loanTyp) || "17068a".equals(loanTyp)
                    || "17069a".equals(loanTyp) || "17129a".equals(loanTyp) || "17145a".equals(loanTyp))) {
                logger.info("非VIPABC产品进行屏蔽：loanType：" + loanTyp);
                continue;
            }
            String applyDt = (String) map1.get("applyDt");// 申请日期
            String applyAmt = (String) map1.get("applyAmt");// 申请金额
            String typCde = (String) map1.get("loanTyp");// 贷款品种代码
            String loanTypName = (String) map1.get("loanTypName");// 贷款品种名称
            String cooprName = (String) map1.get("cooprName");// 门店名称
            String payMtdDesc = (String) map1.get("payMtdDesc");// 分期详情
            String applyTnr = (String) map1.get("applyTnr");// 借款期限
            String merchNo = (String) map1.get("superCoopr");// 商户编码
            String cooprCde = (String) map1.get("cooprCde");// 门店编码
            String goods = (String) map1.get("goods");// 门店编码
            List array = (List) map1.get("goods");// 门店编码
            String goodsKind = "";
            String goodsBrand = "";
            String goodsName = "";
            for (int j = 0; j < array.size(); j++) {
                Map<String, Object> jsonObj = (Map<String, Object>) array.get(j);
                // goodsCode = jsonObj.get("goodsCode").toString();
                goodsName = jsonObj.get("goodsName").toString();
                goodsKind = (String) jsonObj.get("goodsKind");
                goodsBrand = (String) jsonObj.get("goodsBrand");
            }
            String[] split = goodsName.split(",");
            String topLevel = split[0];
            String goodsNameone = split[1];
            Map m = new HashMap<>();
            m.put("orderNo", orderNo);
            m.put("goodsName", goodsNameone);
            m.put("applSeq", applSeq);
            m.put("outSts", outSts);
            m.put("merchNo", merchNo);
            m.put("cooprCde", cooprCde);
            m.put("cooprName", cooprName);
            m.put("payMtdDesc", payMtdDesc);
            m.put("typCde", typCde);
            m.put("applyAmt", applyAmt);
            m.put("applyTnr", applyTnr);
            // m.put("typLvlCde", typLvlCde);
            // m.put("goodsCode", goodsCode);
            m.put("goodsKind", goodsKind);
            m.put("goodsBrand", goodsBrand);
            m.put("applyDt", applyDt);
            m.put("loanName", loanTypName);
            m.put("bankName", bankName);// 银行名称
            m.put("cardNo", cardNo);// 卡号
            m.put("topLevel", topLevel);
            orderlist.add(m);
        }
        logger.info("最终返回数据：" + orderlist);
        return success(orderlist);
    }

    public String getState(String outSts) {
        String state = "";
        if (outSts.equals("1")) {
            state = "待提交";
        } else if (outSts.equals("2")) {
            state = "待确认";
        } else if (outSts.equals("3")) {
            state = "商户退回";
        } else if (outSts.equals("01")) {
            state = "审批中";
        } else if (outSts.equals("02")) {
            state = "贷款被拒绝";
        } else if (outSts.equals("03")) {
            state = "贷款已取消";
        } else if (outSts.equals("04")) {
            state = "等待放款";
        } else if (outSts.equals("05")) {
            state = "审批通过，等待放款";
        } else if (outSts.equals("06")) {
            state = "已放款";
        } else if (outSts.equals("20")) {
            state = "待放款";
        } else if (outSts.equals("22")) {
            state = "审批退回";
        } else if (outSts.equals("23")) {
            state = "等待放款";
        } else if (outSts.equals("24")) {
            state = "放款审核中";
        } else if (outSts.equals("25")) {
            state = "额度申请被拒";
        } else if (outSts.equals("26")) {
            state = "额度申请已取消";
        } else if (outSts.equals("27")) {
            state = "已通过";
        } else if (outSts.equals("AA")) {
            state = "取消放款";
        } else if (outSts.equals("0D")) {
            state = "已逾期";
        }
        return state;
    }

    /**
     * 第三方数据入口
     *
     * @param token
     * @param channel
     * @param channelNo
     * @param param
     * @return
     * @throws Exception
     */
    public Map<String, Object> vipAbcThirdPartyData(String token, String channel, String channelNo, Map<String, Object> param) throws Exception {
        Map<String, Object> redisMap = new HashMap<String, Object>();
        String channleNo = "";
        String applyNo = "";
        String tradeCode = "";
        String retFlag = "";
        String url = "";
        String password_ = "";
        applyNo = (String) param.get("applyNo");
        channleNo = param.get("channelNo") + "";
        String jsonStr = (String) param.get("data");
        password_ = (String) param.get("key");
        tradeCode = (String) param.get("tradeCode");
        logger.info(
                "applyNo:" + applyNo + "||tradeCode:" + tradeCode + "||channleNo:" + channleNo + "||json:" + jsonStr);
        if (StringUtils.isEmpty(jsonStr)) {
            logger.info("第三方发送的请求报文信息不能为空！！！");
            return fail(ConstUtil.ERROR_CODE, "请确认发送的报文信息是否符合条件！");
        }
        String privatekey = "MIIBVQIBADANBgkqhkiG9w0BAQEFAASCAT8wggE7AgEAAkEAokf1Ipb9k0mOWXRkpOzF2w8H6v4SMNLxc/93YSaCdytHzFVtySn2KSy4czRyTDluTzg/7lgfaYh/xwNGgFInKQIDAQABAkEAiQL/8KhSh5w+1z/yCVzu37idEsZYTWiL+0fhXuDInhtLmU7Tmt6VQwn4rq7tmrYIb+Q15E6jtIbyHU3rszVP0QIhAPDC56TTK+knob2fzBPtN9+VSi8bBok5Y4wO84EWqot3AiEArI1snWhHOYspGbELAI3ieHj83Nag/3TrrkgUK8kISl8CIBcsa2dt+/gBHIxH6TixyIL4t585FrP2liJQ/hcau2eZAiAnJgsPh3opZxZTGuTpIkfQl3qfTB7I9qkGKJpS+NBltwIhAJobifVL7NAsWnyTq2jvT+boQttAGZGO87U/lRPXL1PS";//私钥
        byte[] decode = Base64Utils.decode(password_);
        byte[] decryptByPublicKey = RSAUtils.decryptByPrivateKey(decode, privatekey);
        byte[] decrypt = DesUtilvip.decrypt(Base64Utils.decode(jsonStr), new String(decryptByPublicKey));
        String params = new String(decrypt);
        JSONObject fromObject = JSONObject.parseObject(params);
        logger.info("解析的第三方的数据为：" + fromObject);
        if (StringUtils.isEmpty(fromObject.get("uuid")) || StringUtils.isEmpty(fromObject.get("body"))) {
            logger.info("从vipabc获取的参数为空：" + fromObject.toString());
            return fail(ConstUtil.ERROR_CODE, "从vipabc获取的参数为空" + fromObject.toString());
        }
        String uuid = (String) fromObject.get("uuid");//调支用接口VIPABC传过来的唯一标识，与token绑定
        JSONObject jsonObject = fromObject.getJSONObject("body");
        String orderSn = (String) jsonObject.get("orderSn");//商城订单号
        String loanType = (String) jsonObject.get("loanType");//贷款品种编码
        String payAmt = jsonObject.get("payAmt") + "";//订单实付金额
        String province = (String) jsonObject.get("province");//省
        String city = (String) jsonObject.get("city");//市
        String country = (String) jsonObject.get("country");//区
        String detailAddress = (String) jsonObject.get("detailAddress");//详细地址
        String orderDate = (String) jsonObject.getString("orderDate");//下单时间
        String applyTnr = (String) jsonObject.getString("applyTnr");//贷款期数
        Object object = jsonObject.get("orderMessage");//网单信息
        Object object2 = jsonObject.get("externalmessage");//扩展信息
        if (StringUtils.isEmpty(object2)) {
            logger.info("获取的合同扩展信息内容为空" + object2 + "商城订单号为：" + orderSn);
            return fail(ConstUtil.ERROR_CODE, "从vipabc获取的合同扩展信息内容为空" + object2);
        }
        List fromObject4 = (List) object2;//扩展信息
        List fromObject2 = (List) (object);
        ArrayList<String> arrayList = new ArrayList<>();
        String cOrderSn = null;
        String topLevel = null;
        String model = null;
        String sku = null;
        String price = null;
        String num = null;
        String cOrderAmt = null;
        String cOrderPayAmt = null;
        for (int i = 0; i < fromObject2.size(); i++) {
            Map jsonm = (Map) (fromObject2.get(i));
            cOrderSn = (String) jsonm.get("cOrderSn");//网单编号
            topLevel = (String) jsonm.get("topLevel");//一级类目
            model = (String) jsonm.get("model");//商品类型
            sku = (String) jsonm.get("sku");//商品品类编码
            price = (String) jsonm.get("price");//价格
            num = (String) jsonm.get("num");//数量
            cOrderAmt = (String) jsonm.get("cOrderAmt");//网单金额
            cOrderPayAmt = (String) jsonm.get("cOrderPayAmt");//网单实付金额
        }
        String serviceContent = null;//服务内容
        String amt = null;//服务费用
        String startYear = null;//起始年
        String startMonth = null;//起始月
        String startDay = null;//起始日
        String endYear = null;//终止年
        String endMonth = null;//终止月
        String endDay = null;//终止日
        String vipSign = null;//会员名
        for (int i = 0; i < fromObject4.size(); i++) {
            Map fromObject5 = (Map) (fromObject4.get(i));
            serviceContent = (String) fromObject5.get("serviceContent");
            amt = (String) fromObject5.get("amt") + "";
            startYear = (String) fromObject5.get("startYear");
            startDay = (String) fromObject5.get("startDay");
            endYear = (String) fromObject5.get("endYear");
            endMonth = (String) fromObject5.get("endMonth");
            endDay = (String) fromObject5.get("endDay");
            vipSign = (String) fromObject5.get("vipSign");
        }
        HashMap<String, Object> hashMap2 = new HashMap<>();
        HashMap<String, Object> hashMap3 = new HashMap<>();
        hashMap3.put("serviceContent", serviceContent);
        hashMap3.put("amt", amt);
        hashMap3.put("startYear", startYear);
        hashMap3.put("startMonth", startMonth);
        hashMap3.put("startDay", startDay);
        hashMap3.put("endYear", endYear);
        hashMap3.put("endMonth", endMonth);
        hashMap3.put("endDay", endDay);
        hashMap3.put("vipSign", vipSign);
        hashMap2.put("externalmessage", hashMap3);
        VipAbcAppOrderGoods appOrderGoods = new VipAbcAppOrderGoods();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String format = simpleDateFormat.format(new Date());
        appOrderGoods.setOrderid(UUID.randomUUID().toString().replace("-", ""));
        appOrderGoods.setOrderSn(orderSn);
        appOrderGoods.setLoanType(loanType);
        appOrderGoods.setPayAmt(payAmt);
        appOrderGoods.setProvince(province);
        appOrderGoods.setCity(city);
        appOrderGoods.setCountry(country);
        appOrderGoods.setDetailAddress(detailAddress);
        appOrderGoods.setOrderDate(orderDate);
        appOrderGoods.setApplyTnr(applyTnr);
        appOrderGoods.setCOrderSn(cOrderSn);
        appOrderGoods.setTopLevel(topLevel);
        appOrderGoods.setModel(model);
        appOrderGoods.setSku(sku);
        appOrderGoods.setPrice(price);
        appOrderGoods.setNum(num);
        appOrderGoods.setCOrderAmt(cOrderAmt);
        appOrderGoods.setCOrderPayAmt(cOrderPayAmt);
        appOrderGoods.setVipuuid(uuid);
        appOrderGoods.setInputtime(format);
        logger.info("保存的数据为：" + appOrderGoods);
        String queryvipordersn = vipAbcDao.queryvipordersn(orderSn);//查询第三方订单号
        logger.info("查询第三方订单号:" + queryvipordersn);
        if ("0".equals(queryvipordersn)) {
            vipAbcDao.saveDataInterfaceLog(appOrderGoods);//为空就保存
        }

        logger.info("数据保存完成");
        hashMap2.put("appOrderGoods", appOrderGoods);
        logger.info("map中保存完成");
        RedisUtils.set(uuid, hashMap2, 30, TimeUnit.DAYS);
        RedisUtils.set("orderSn", orderSn, 30, TimeUnit.DAYS);//第三方商城订单号
        logger.info("redis中保存完成" + hashMap2);
//        	url=BusinessConstance.DataConfigMap.get("vipabc_url").toString()+"PaymentPlatform/page/webapp/vipABC/#qrcodePhone.html?uuid="+uuid+"&utm_source=eduabc&utm_medium=eduabc&utm_campaign=eduabc&utm_content=eduabc&utm_term=eduabc";
        url = "https://testpm.haiercash.com:9002/" + "PaymentPlatform/page/webapp/vipABC/#qrcodePhone.html?uuid=" + uuid;
        logger.info("获取的URL为：" + url);
        JSONObject jsonObject2 = new JSONObject();
        jsonObject2.put("url", url);
        String reqData = jsonObject2.toString();
        String productKey = Base64UtilsVIP.productKey();
        String key = productKey;
        String iv = productKey;
        String publicKey = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAKJH9SKW/ZNJjll0ZKTsxdsPB+r+EjDS8XP/d2EmgncrR8xVbckp9iksuHM0ckw5bk84P+5YH2mIf8cDRoBSJykCAwEAAQ==";//公钥
        String sKey = key;
        String desData = Base64UtilsVIP.DesEncrypt(reqData, sKey, iv);
        //3.加密des的key
        password_ = Base64Utils.encode(RSAUtils.encryptByPublicKey((productKey + productKey).getBytes(), publicKey));
        JSONObject reqjson = new JSONObject();
        reqjson.put("applyNo", UUID.randomUUID().toString().replace("-", ""));
        reqjson.put("channelNo", "53");
        reqjson.put("tradeCode", "vipabc-10002");
        reqjson.put("data", desData);
        reqjson.put("key", password_);
        return success(reqjson);
    }

    /**
     * 二维码生成
     *
     * @param token
     * @param channel
     * @param channelNo
     * @param params
     * @return
     */
    public Map<String, Object> vipAbcPcStore(String token, String channel, String channelNo, Map<String, Object> params) {
        String uuid = null;
        uuid = (String) params.get("uuid");
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");//设置日期格式
        Date date = new Date();
        String start = df.format(date);
        Calendar instance = Calendar.getInstance();
        instance.setTime(date);
        instance.add(Calendar.DAY_OF_MONTH, 1);
        Date time = instance.getTime();
        String end = df.format(time);
        Timer timer = new Timer();
        Integer selectuuid = vipAbcDao.selectuuid(uuid);//库里有信息就跳过，没信息就保存
        int parseInt2 = Integer.valueOf(selectuuid);
        if (parseInt2 == 0) {
            Vipmessage store2 = new Vipmessage();
            store2.setUuid(uuid);
            store2.setStarts(start);
            store2.setEnd(end);
            store2.setNowtime(start);
            store2.setFlag("n");
            store2.setOrdersn("");
            store2.setRemark("");
            vipAbcDao.savedate(store2);
            logger.info("新订单保存成功uuid为：" + uuid);
        }
        timer.schedule(new Task(timer), 3 * 1000, 600000);//10分钟查询一次
//	        // 第二个参数为首次执行的延时时间，第三个参数为定时执行的间隔时间
//	        service.scheduleAtFixedRate(runnable, 10, 1, TimeUnit.SECONDS);
        logger.info("从前端获取的UUID是：" + uuid);
        if (StringUtils.isEmpty(uuid)) {
            logger.info("从前端获取的UUID为空" + uuid);
            return fail(ConstUtil.ERROR_CODE, "从前端获取的UUID为空");
        }
        Object object = RedisUtils.getExpireMap(uuid);
        if (StringUtils.isEmpty(object)) {
            logger.info("redis中获取的数据为空" + object.toString());
            return fail(ConstUtil.ERROR_CODE, "redis中获取的数据为空");
        }
        Map fromObjectone = (Map) object;
        Map fromObject = (Map) (fromObjectone.get("appOrderGoods"));
        String orderSn = (String) fromObject.get("orderSn");//订单号
        String education = (String) fromObject.get("topLevel");//课程分类
        String goodsName = (String) fromObject.get("model");//课程名称
        String payAmt = (String) fromObject.get("payAmt");//总价
        String orderDate = (String) fromObject.get("orderDate");//总价
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("orderSn", orderSn);//订单号
        hashMap.put("education", education);//课程分类
        hashMap.put("goodsName", goodsName);//课程名称
        hashMap.put("payAmt", payAmt);//订单金额
        hashMap.put("orderDate", orderDate);//下单时间
        return success(hashMap);
    }

    class Task extends TimerTask {
        private Timer timer;

        public Task(Timer timer) {
            this.timer = timer;
        }

        public void run() {
            logger.info("程序执行");
            try {
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");//设置日期格式
                Date date = new Date();
                String start = df.format(date);//最新事件
                List<Vipmessage> selectarray = vipAbcDao.selectarray();  //没有订单号  flag  n   end time > notime
                for (Vipmessage vipmessage : selectarray) {
                    String uuid2 = vipmessage.getUuid();
                    Vipmessage store2 = new Vipmessage();
                    store2.setUuid(uuid2);
                    store2.setNowtime(start);
                    vipAbcDao.updatetime(store2);//更新时间
                }
                List<Vipmessage> queryvip = vipAbcDao.queryvip();//没有产生订单号  且flag  为n 现在的时间大于end时间
                for (Vipmessage storeone : queryvip) {
                    String uuid2 = storeone.getUuid();
                    RedisUtils.set(uuid2, "", 1, TimeUnit.MINUTES);
                    vipAbcDao.updateflag(uuid2);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
