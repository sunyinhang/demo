package com.haiercash.payplatform.service.impl;

import com.bestvike.linq.exception.InvalidOperationException;
import com.haiercash.core.collection.ArrayUtils;
import com.haiercash.core.collection.MapUtils;
import com.haiercash.core.io.Path;
import com.haiercash.core.lang.Base64Utils;
import com.haiercash.core.lang.BeanUtils;
import com.haiercash.core.lang.Convert;
import com.haiercash.core.lang.DateUtils;
import com.haiercash.core.lang.Environment;
import com.haiercash.core.lang.ObjectUtils;
import com.haiercash.core.lang.StringUtils;
import com.haiercash.core.lang.TimeSpan;
import com.haiercash.core.reflect.GenericType;
import com.haiercash.core.serialization.URLSerializer;
import com.haiercash.core.vfs.VFSType;
import com.haiercash.core.vfs.VFSUserAuthenticator;
import com.haiercash.core.vfs.VFSUtils;
import com.haiercash.payplatform.config.CashloanConfig;
import com.haiercash.payplatform.config.OutreachConfig;
import com.haiercash.payplatform.config.ShunguangConfig;
import com.haiercash.payplatform.config.StorageConfig;
import com.haiercash.payplatform.service.AppServerService;
import com.haiercash.payplatform.service.CrmManageService;
import com.haiercash.payplatform.service.CustExtInfoService;
import com.haiercash.payplatform.service.OCRIdentityService;
import com.haiercash.payplatform.utils.EncryptUtil;
import com.haiercash.payplatform.utils.ocr.OCRIdentityTC;
import com.haiercash.spring.config.EurekaServer;
import com.haiercash.spring.redis.RedisUtils;
import com.haiercash.spring.rest.IResponse;
import com.haiercash.spring.rest.common.CommonResponse;
import com.haiercash.spring.rest.common.CommonRestUtils;
import com.haiercash.spring.service.BaseService;
import com.haiercash.spring.util.ConstUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.FileObject;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by yuanli on 2017/7/31.
 */
@Service
public class OCRIdentityServiceImpl extends BaseService implements OCRIdentityService {
    public static final String CERT_IMAGE_PATH_A = "certImagePathA";
    public static final String CERT_IMAGE_PATH_B = "certImagePathB";
    public final Log logger = LogFactory.getLog(getClass());
    @Autowired
    private AppServerService appServerService;
    @Autowired
    private CustExtInfoService custExtInfoService;
    @Autowired
    private CrmManageService crmManageService;
    @Autowired
    private ShunguangConfig shunguangConfig;
    @Autowired
    private StorageConfig storageConfig;
    @Autowired
    private OutreachConfig outreachConfig;
    @Autowired
    private CashloanConfig cashloanConfig;

    //ocr 图片上传
    public IResponse<Map> ocrIdentity(OcrPathType ocrPathType, MultipartFile ocrImg) throws Exception {
        Assert.notNull(ocrPathType, "ocrPathType can not be null");

        logger.info("OCR身份信息获取*************开始");
        //图片非空判断
        if (ocrImg == null || ocrImg.isEmpty()) {
            logger.info("图片为空");
            return CommonResponse.fail("01", "图片为空");
        }
        byte[] buffer = ocrImg.getBytes();
        if (ArrayUtils.isEmpty(buffer)) {
            return CommonResponse.fail("01", "图片为空");
        }
        //token非空判断
        String token = this.getToken();
        if (StringUtils.isEmpty(token)) {
            logger.info("获取token为空");
            return CommonResponse.fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        //缓存数据获取
        Map<String, Object> sessionMap = RedisUtils.getExpireMap(token);
        if (MapUtils.isEmpty(sessionMap)) {
            logger.info("Jedis数据获取失败");
            return CommonResponse.fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        //按 userId 存储的时候 验证 userId
        String userId = (String) sessionMap.get("userId");
        if (ocrPathType == OcrPathType.ByUserId && StringUtils.isEmpty(userId)) {
            logger.info("jedis获取数据失效");
            return CommonResponse.fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        //调用OCR
        IResponse<Map> returnMessage = new OCRIdentityTC().ocrIdCard(buffer);
        returnMessage.assertSuccessNeedBody();
        //获取OCR返回信息进行redis存储
        Map<String, Object> cardInfo = returnMessage.getBody();
        if (!ObjectUtils.isEmpty(cardInfo.get("name"))) {
            sessionMap.put("name", cardInfo.get("name"));//姓名
        }
        if (!ObjectUtils.isEmpty(cardInfo.get("gender"))) {
            sessionMap.put("gender", cardInfo.get("gender"));//性别
        }
        if (!ObjectUtils.isEmpty(cardInfo.get("birthday"))) {
            sessionMap.put("birthday", cardInfo.get("birthday"));//出生年月日
        }
        if (!ObjectUtils.isEmpty(cardInfo.get("race"))) {
            sessionMap.put("race", cardInfo.get("race"));//民族
        }
        if (!ObjectUtils.isEmpty(cardInfo.get("address"))) {
            sessionMap.put("address", cardInfo.get("address"));//地址
        }
        if (!ObjectUtils.isEmpty(cardInfo.get("id_card_number"))) {
            sessionMap.put("idCard", cardInfo.get("id_card_number"));//身份证号
        }
        if (!ObjectUtils.isEmpty(cardInfo.get("issued_by"))) {
            sessionMap.put("issued", cardInfo.get("issued_by"));//签发机关
        }
        if (!ObjectUtils.isEmpty(cardInfo.get("valid_date"))) {
            sessionMap.put("validDate", cardInfo.get("valid_date"));//有效期
        }

        //OCR调用成功，进行图片本地上传
        String cardSide = (String) cardInfo.get("side");
        String idCard = Convert.toString(sessionMap.get("idCard"));
        if (StringUtils.isEmpty(idCard))
            return CommonResponse.fail(ConstUtil.ERROR_CODE, "请先上传身份证正面");
        String certImagePath = saveImage2Disk(ocrPathType, userId, idCard, buffer);
        if ("front".equals(cardSide)) {
            logger.info("身份证正面存储路径为------------------：" + certImagePath);
            sessionMap.put(CERT_IMAGE_PATH_A, certImagePath);
        } else {
            logger.info("身份证反面存储路径为------------------：" + certImagePath);
            sessionMap.put(CERT_IMAGE_PATH_B, certImagePath);
        }

        RedisUtils.setExpire(token, sessionMap);
        logger.info("OCR身份信息获取*************结束");
        return CommonResponse.success(cardInfo);
    }

    private VFSUserAuthenticator ocrAuth() {
        return new VFSUserAuthenticator(VFSType.LOCAL);
    }

    private String saveImage2Disk(OcrPathType ocrPathType, String userId, String idNo, byte[] buffer) {
        Assert.notNull(ocrPathType, "ocrPathType can not be null");

        String path;
        String uuid = StringUtils.remove(UUID.randomUUID().toString(), Environment.MinusChar);
        switch (ocrPathType) {
            case ByUserId:
                if (StringUtils.isEmpty(userId))
                    throw new InvalidOperationException("按 userId 存储时, userId 为空");
                path = new Path(storageConfig.getOcrPath()).combine("certImage").combine(userId).combine(uuid + ".jpg").toString();
                break;
            case ByIdNo:
                if (StringUtils.isEmpty(idNo))
                    throw new InvalidOperationException("按 idNo 存储时, idNo 为空");
                path = new Path(storageConfig.getOcrPath()).combine("certImageDate").combine(DateUtils.nowDateString()).combine(idNo).combine(uuid + ".jpg").toString();
                break;
            default:
                throw new InvalidOperationException("不支持的 orcPathType");
        }
        logger.info("身份证照片路径: " + path);
        try (FileObject fileObject = VFSUtils.resolveFile(ocrAuth(), path);
             OutputStream outputStream = fileObject.getContent().getOutputStream()) {
            outputStream.write(buffer);
        } catch (Exception e) {
            logger.info("身份证照片保存失败", e);
        }
        return path;
    }

    @Override
    public Map<String, Object> savaIdentityInfo(Map<String, Object> map) {
        logger.info("OCR信息保存（下一步）***********开始");
        String token = Convert.toString(map.get("token"));
        String name = Convert.toString(map.get("name"));

        if (StringUtils.isEmpty(token) || StringUtils.isEmpty(name)) {
            logger.info("token:" + token + "  name:" + name);
            logger.info("前台获取请求参数有误");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }

        Map<String, Object> cacheMap = RedisUtils.getExpireMap(token);
        if (MapUtils.isEmpty(cacheMap)) {
            logger.info("Jedis数据获取失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        cacheMap.put("name", name);
        if ("60".equals(this.getChannelNo())) {
            String authPhone = Convert.toString(map.get("authPhone"));//支付宝授权时用的手机号
            if (StringUtils.isEmpty(authPhone))
                return fail(ConstUtil.ERROR_CODE, "授权手机号不能为空");
            cacheMap.put("authPhone", authPhone);
        }
        RedisUtils.setExpire(token, cacheMap);
        logger.info("OCR信息保存（下一步）***********结束");
        return success();
    }

    //获取省市区
    public Map<String, Object> getArea(Map<String, Object> map) {
        logger.info("获取省市区*****************开始");
        String token = (String) map.get("token");
        String areaCode = (String) map.get("areaCode");
        String channel = (String) map.get("channel");
        String channelNo = (String) map.get("channelNo");

        if (StringUtils.isEmpty(token) || StringUtils.isEmpty(channel) || StringUtils.isEmpty(channelNo)) {
            logger.info("token:" + token + "   channel:" + channel + "   channelNo:" + channelNo);
            logger.info("前台获取请求参数有误");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }

        String flag = "children"; //查询标志
        if (areaCode == null || "".equals(areaCode)) {
            areaCode = "";
            flag = "province";
        }

        Map<String, Object> reqmap = new HashMap<>();
        reqmap.put("areaCode", areaCode);
        reqmap.put("flag", flag);
        reqmap.put("channel", channel);
        reqmap.put("channelNo", channelNo);

        Map<String, Object> resultmap = appServerService.getAreaInfo(token, reqmap);
        logger.info("获取省市区*****************结束");
        return resultmap;
    }

    //获取卡信息
    public Map<String, Object> getCardInfo(String cardNo) {
        logger.info("获取卡信息*****************开始");
        if (StringUtils.isEmpty(cardNo)) {
            logger.info("cardNo:" + cardNo);
            logger.info("前台获取请求参数有误");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }

        Map<String, Object> resultmap = appServerService.getBankInfo(cardNo);
        logger.info("获取卡信息*****************结束");
        return resultmap;
    }

    //发送短信验证码
    @Override
    public Map<String, Object> sendMessage(Map<String, Object> params) {
        logger.info("发送短信验证码***************开始");
        String token = (String) params.get("token");
        String channel = (String) params.get("channel");
        String channelNo = (String) params.get("channelNo");
        if (StringUtils.isEmpty(token) || StringUtils.isEmpty(channel) || StringUtils.isEmpty(channelNo)) {
            logger.info("token:" + token);
            logger.info("前台获取请求参数有误");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }

        Map<String, Object> cacheMap = RedisUtils.getExpireMap(token);
        if (cacheMap == null) {
            logger.info("Jedis数据获取失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }

        String phone = (String) cacheMap.get("phoneNo");////得到绑定手机号
        String phoneEncrypt = EncryptUtil.simpleEncrypt(phone);
        Map<String, Object> map = new HashMap<>();
        map.put("channel", channel);
        map.put("channelNo", channelNo);
        map.put("phone", phoneEncrypt);
        map.put("token", token);

        Map<String, Object> resultmap = appServerService.sendMessage(token, map);
        logger.info("发送短信验证码*******************结束");
        return resultmap;
    }

    //发送短信验证码
    @Override
    public Map<String, Object> sendMsg(Map<String, Object> params) {
        String phone = (String) params.get("phone");
        String channel = (String) params.get("channel");
        String channelNo = (String) params.get("channelNo");
        logger.info("发送短信验证码***************开始");
        if (StringUtils.isEmpty(phone)) {
            logger.info("phone:" + phone);
            logger.info("前台获取请求参数有误");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }

        String phoneEncrypt = EncryptUtil.simpleEncrypt(phone);
        Map<String, Object> map = new HashMap<>();
        map.put("channel", channel);
        map.put("channelNo", channelNo);
        map.put("phone", phoneEncrypt);

        Map<String, Object> resultmap = appServerService.sendMessage(null, map);
        logger.info("发送短信验证码***************开始");
        return resultmap;
    }

    //实名认证
    public IResponse<Map> realAuthentication(Map<String, Object> map) throws IOException {
        logger.info("实名认证*********************开始");

        //1.前台参数获取
        String token = (String) map.get("token");
        String verifyNo = (String) map.get("verifyNo");
        String cityCode = (String) map.get("cityCode");//省市区编码
        String cardnumber = (String) map.get("cardnumber");//卡号
        String mobile = (String) map.get("mobile");//手机号
        String channel = (String) map.get("channel");
        String channelNo = (String) map.get("channelNo");
        //2.前台参数非空判断
        if (StringUtils.isEmpty(token) || StringUtils.isEmpty(verifyNo) || StringUtils.isEmpty(cityCode) ||
                StringUtils.isEmpty(cardnumber) || StringUtils.isEmpty(mobile) ||
                StringUtils.isEmpty(channel) || StringUtils.isEmpty(channelNo)) {
            logger.info("token:" + token + "  verifyNo:" + verifyNo + "  cityCode:" + cityCode +
                    "  cardnumber:" + cardnumber + "  mobile:" + mobile + "   channel:" + channel + "   channelNo:" + channelNo);
            logger.info("前台获取请求参数有误");
            return CommonResponse.fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        //3.jedis缓存数据获取
        Map<String, Object> cacheMap = RedisUtils.getExpireMap(token);
        if (MapUtils.isEmpty(cacheMap)) {
            logger.info("Jedis数据获取失败");
            return CommonResponse.fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        String name = (String) cacheMap.get("name");//扫描客户姓名
        String birthDt = (String) cacheMap.get("birthday");//扫描出生年月日
        String gender = (String) cacheMap.get("gender");//扫描性别
        String regAddr = (String) cacheMap.get("address");//扫描详细地址
        String validDate = (String) cacheMap.get("validDate");//有效期
        String certOrga = (String) cacheMap.get("issued");//扫描身份证签发机关
        String ethnic = (String) cacheMap.get("race");//扫描民族
        String idCard = (String) cacheMap.get("idCard");//扫描身份证号
        String userId = (String) cacheMap.get("userId");//userId
        if (!StringUtils.isEmpty(idCard)) {
            idCard = idCard.toUpperCase();
            logger.info("扫描的身份证号码是" + idCard);
        } else {
            logger.info("扫描身份证号为空");
            return CommonResponse.fail(ConstUtil.ERROR_CODE, "扫描身份证号为空");
        }
        //顺逛传送身份证与客户实名身份证不一致
        String idNoHaier = (String) cacheMap.get("idNoHaier");//
        if (!StringUtils.isEmpty(idNoHaier)) {
            idNoHaier = idNoHaier.toUpperCase();
            logger.info("顺逛传送身份证号是：" + idNoHaier);
        }
        if (!StringUtils.isEmpty(idNoHaier) && !idCard.equals(idNoHaier)) {
            logger.info("顺逛传送身份证与客户实名身份证不一致");
            return CommonResponse.fail(ConstUtil.ERROR_CODE, "顺逛白条实名认证必须和顺逛实名认证一致！");
        }
        //4.缓存数据非空判断
        if (StringUtils.isEmpty(name) || StringUtils.isEmpty(birthDt) || StringUtils.isEmpty(gender)
                || StringUtils.isEmpty(regAddr) || StringUtils.isEmpty(validDate) || StringUtils.isEmpty(certOrga)
                || StringUtils.isEmpty(ethnic) || StringUtils.isEmpty(idCard) || StringUtils.isEmpty(userId)) {
            logger.info("name:" + name + "  birthDt:" + birthDt + "  gender:" + gender + "  validDate:" + validDate + "  certOrga:" + certOrga
                    + "   ethnic:" + ethnic + "    idCard:" + idCard + "   userId:" + userId);
            logger.info("Jedis缓存获取信息失败");
            return CommonResponse.fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        //5.校验短信验证码
        Map<String, Object> verifyNoMap = new HashMap<>();
        verifyNoMap.put("phone", mobile);
        verifyNoMap.put("verifyNo", verifyNo);
        verifyNoMap.put("token", token);
        verifyNoMap.put("channel", channel);
        verifyNoMap.put("channelNo", channelNo);
        Map<String, Object> verifyresultmap = appServerService.smsVerify(token, verifyNoMap);
        Map verifyheadjson = (Map<String, Object>) verifyresultmap.get("head");
        String verifyretFlag = (String) verifyheadjson.get("retFlag");
        if (!"00000".equals(verifyretFlag)) {//校验短信验证码失败
            String retMsg = (String) verifyheadjson.get("retMsg");
            return CommonResponse.fail(ConstUtil.ERROR_CODE, retMsg);
        }
        //6.OCR信息保存
        String certStrDt = "";//扫描身份证有效期限开始日期
        String certEndDt = "";//扫描身份证有效期限结束日期
        String vDate = validDate.replaceAll("[-.]", "");
        if (vDate.length() >= 8) {
            certStrDt = vDate.substring(0, 8); //取前8位作为起始日期
            certEndDt = vDate.substring(8);  //剩余的作为截止日期
        }

        if ("男".equals(gender)) {//10男
            gender = "10";
        }
        if ("女".equals(gender)) {//20女
            gender = "20";
        }

        Map<String, Object> ocrMap = new HashMap<>();
        ocrMap.put("custName", name);
        ocrMap.put("afterCustName", name);
        ocrMap.put("birthDt", birthDt);
        ocrMap.put("gender", gender);
        ocrMap.put("regAddr", regAddr);
        ocrMap.put("validDate", validDate);
        ocrMap.put("certStrDt", certStrDt);
        ocrMap.put("certEndDt", certEndDt);
        ocrMap.put("certOrga", certOrga);
        ocrMap.put("ethnic", ethnic);
        ocrMap.put("certNo", idCard);
        ocrMap.put("token", token);
        ocrMap.put("channel", channel);
        ocrMap.put("channelNo", channelNo);
        Map<String, Object> ocrresultmap = appServerService.saveCardMsg(token, ocrMap);
        Map ocrheadjson = (Map<String, Object>) ocrresultmap.get("head");
        String ocrretFlag = (String) ocrheadjson.get("retFlag");
        if (!"00000".equals(ocrretFlag)) {//身份证信息保存失败
            String retMsg = (String) ocrheadjson.get("retMsg");
            return CommonResponse.fail(ConstUtil.ERROR_CODE, retMsg);
        }

        //绑定手机号修改为实名认证手机号
        String phone = cacheMap.get("phoneNo").toString();//得到绑定手机号
        //7.验证并新增实名认证信息
        String acctProvince;//省代码
        String acctCity;//市代码
        if ("60".equals(this.getChannelNo())) {
            acctProvince = StringUtils.EMPTY;
            acctCity = StringUtils.EMPTY;
        } else {
            String[] officeArea_split = cityCode.split(",");
            acctProvince = officeArea_split[0];
            acctCity = officeArea_split[1];
        }
        Map<String, Object> identityMap = new HashMap<>();
        identityMap.put("token", token);
        identityMap.put("channel", channel);
        identityMap.put("channelNo", channelNo);
        identityMap.put("custName", name); //客户姓名 √
        identityMap.put("certNo", idCard); //身份证号 √
        identityMap.put("cardNo", cardnumber); //银行卡号 √
        identityMap.put("mobile", mobile); //手机号 √
        identityMap.put("dataFrom", channelNo); //数据来源 √
        identityMap.put("threeParamVal", ConstUtil.THREE_PARAM_VAL_N); //是否需要三要素验证
        identityMap.put("userId", userId); //客户userId
        identityMap.put("acctProvince", acctProvince); //开户行省代码
        identityMap.put("acctCity", acctCity); //开户行市代码
        identityMap.put("bindMobile", phone);
        Map<String, Object> identityresultmap = appServerService.fCiCustRealThreeInfo(token, identityMap);
        Map identityheadjson = (Map<String, Object>) identityresultmap.get("head");
        String identityretFlag = (String) identityheadjson.get("retFlag");
        if (!"00000".equals(identityretFlag)) {
            String retMsg = (String) identityheadjson.get("retMsg");
            return CommonResponse.fail(ConstUtil.ERROR_CODE, retMsg);
        }
        Map identitybodyjson = (Map<String, Object>) identityresultmap.get("body");
        //信息保存
        String custNo = (String) identitybodyjson.get("custNo");
        String cardNo = (String) identitybodyjson.get("cardNo");
        String bankCode = (String) identitybodyjson.get("acctBankNo");
        String bankName = (String) identitybodyjson.get("acctBankName");
        String idNo = (String) identitybodyjson.get("certNo");
        String idType = (String) identitybodyjson.get("certType");
        String cardPhone = (String) identitybodyjson.get("mobile");

        cacheMap.put("custNo", custNo);
        cacheMap.put("cardNo", cardNo);
        cacheMap.put("bankCode", bankCode);
        cacheMap.put("bankName", bankName);
        cacheMap.put("idNo", idNo);
        cacheMap.put("idType", idType);
        cacheMap.put("cardPhone", cardPhone);
        RedisUtils.setExpire(token, cacheMap);

        //获取客户标签
        Map tagmap = new HashMap<>();
        tagmap.put("custName", name);//姓名
        tagmap.put("idTyp", "20");//证件类型
        tagmap.put("idNo", idCard);//证件号码
        IResponse<List> tagmapresult = BeanUtils.mapToBean(crmManageService.getCustTag("", tagmap), new GenericType<CommonResponse<List>>() {
        });
        tagmapresult.assertSuccess();
        //
        String userType = (String) cacheMap.get("userType");
        String tagId = "";
        if ("01".equals(userType)) {//微店主
            tagId = shunguangConfig.getShopKeeper();
        }
        if ("02".equals(userType)) {//消费者
            tagId = shunguangConfig.getConsumer();
        }
        //
        Boolean b = false;
        List<Map<String, Object>> body = (List<Map<String, Object>>) tagmapresult.getBody();
        for (Map<String, Object> m : body) {
            String tagid = m.get("tagId").toString();
            if (tagid.equals(tagId)) {
                b = true;
            }
        }
        //若不存在进行添加  /app/crm/cust/setCustTag
        if (!b) {
            logger.info("打标签");
            Map addtagmap = new HashMap<>();
            addtagmap.put("certNo", idCard);//身份证号
            addtagmap.put("tagId", tagId);//自定义标签ID
            IResponse<Map> addtagmapresult = BeanUtils.mapToBean(crmManageService.setCustTag("", addtagmap), new GenericType<CommonResponse<Map>>() {
            });
            addtagmapresult.assertSuccess();
        }

        cacheMap.put("phoneNo", mobile);
        RedisUtils.setExpire(token, cacheMap);
        //OCR图片路径上送
        Map<String, String> pathMap = new HashMap<>();
        pathMap.put(CERT_IMAGE_PATH_A, (String) cacheMap.get(CERT_IMAGE_PATH_A));//正面共享盘位置
        pathMap.put(CERT_IMAGE_PATH_B, (String) cacheMap.get(CERT_IMAGE_PATH_B));//反面共享盘位置
        for (Map.Entry<String, String> entry : pathMap.entrySet()) {
            Map<String, Object> paramMap = new HashMap<>();
            if (CERT_IMAGE_PATH_A.equals(entry.getKey())) {//正面
                paramMap.put("attachType", ConstUtil.CERT_FILE_TYPE_A);//影像类型
                paramMap.put("attachName", ConstUtil.CERT_FILE_NAME_A);//影像名称
            } else if (CERT_IMAGE_PATH_B.equals(entry.getKey())) {//反面
                paramMap.put("attachType", ConstUtil.CERT_FILE_TYPE_B);
                paramMap.put("attachName", ConstUtil.CERT_FILE_NAME_B);
            }
            String filePath = entry.getValue();//文件路径
            InputStream is = new BufferedInputStream(new FileInputStream(String.valueOf(filePath)));
            //获取MD5码
            boolean isA = CERT_IMAGE_PATH_A.equals(entry.getKey());
            String md5Code = DigestUtils.md5Hex(is);
            paramMap.put("token", token);
            paramMap.put("md5", md5Code);//文件md5码
            paramMap.put("custNo", custNo);
            paramMap.put("filePath", filePath);//文件路径
            paramMap.put("idNo", idCard);//身份证号
            paramMap.put("commonCustNo", null);//共同还款人编号，传null
            logger.info("实名绑卡，上传身份证" + (isA ? "正" : "反") + "面，请求参数：" + paramMap);
            Map<String, Object> uploadresultmap = appServerService.attachUploadPersonByFilePath(token, paramMap);
            JSONObject uploadresult = new JSONObject(uploadresultmap);
            JSONObject head = uploadresult.getJSONObject("head");
            String retflag = head.getString("retFlag");
            if ("00000".equals(retflag)) {
                logger.info("实名绑卡，上传身份证" + (isA ? "正" : "反") + "面成功");
            } else {
                logger.info("实名绑卡，上传身份证" + (isA ? "正" : "反") + "面失败");
            }
        }
        logger.info("实名认证***********************结束");
        return CommonResponse.success();
    }

    //支付密码设置
    public Map<String, Object> resetPayPasswd(String token, String payPasswd) {
        logger.info("支付密码设置************开始");
        if (StringUtils.isEmpty(token)) {
            logger.info("token:" + token);
            logger.info("从前端获取的的token为空");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        if (StringUtils.isEmpty(payPasswd)) {
            logger.info("payPasswd:" + payPasswd);
            logger.info("从前端获取的支付密码为空");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        Map<String, Object> cacheMap = RedisUtils.getExpireMap(token);
        if (MapUtils.isEmpty(cacheMap)) {
            logger.info("Jedis获取失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        cacheMap.put("pageFlag", "0");
        cacheMap.put("payPasswd", payPasswd);
        RedisUtils.setExpire(token, cacheMap);
        logger.info("支付密码设置************结束");
        return success();
    }

    /**
     * 协议展示：(1)展示注册协议(2)个人征信(3)借款合同
     *
     * @param token
     * @param params
     * @return
     */
    public Map<String, Object> treatyShowServlet(String token, Map<String, Object> params) throws Exception {
        String realmName;
        String flag = (String) params.get("flag");
        HashMap<String, Object> map = new HashMap<>();
        if (StringUtils.isEmpty(flag)) {
            logger.info("从前端获取的flag:" + flag);
            logger.info("从前端h获取的flag为空");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        if ("register".equals(flag)) {
            if (StringUtils.isEmpty(token)) {
                String custName = "";
                custName = URLSerializer.encode(Base64Utils.encodeString(custName));
                realmName = "/app/appserver/register?custName=" + custName;
                logger.info("------------注册协议------------" + realmName);
                map.put("realmName", realmName);
            } else {
                Map<String, Object> cacheMap = RedisUtils.getExpireMap(token);
                if (MapUtils.isEmpty(cacheMap)) {
                    logger.info("Jedis获取失败");
                    return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
                }
                String orderNo = (String) cacheMap.get("orderNo");
                String custName = (String) cacheMap.get("name");
                if (!StringUtils.isEmpty(orderNo) || !StringUtils.isEmpty(custName)) {
                    if (orderNo == null) {
                        orderNo = "";
                    }
                    realmName = "/app/appserver/register?orderNo=" + orderNo + "&custName=" + URLSerializer.encode(Base64Utils.encodeString(custName));
                    logger.info("------------注册协议------------" + realmName);
                    map.put("realmName", realmName);
                }
            }
        } else {
            if (StringUtils.isEmpty(token)) {
                logger.info("从前端获取的token：" + token);
                logger.info("从前端获取的token为空");
                return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
            }

            Map<String, Object> cacheMap = RedisUtils.getExpireMap(token);
            if (MapUtils.isEmpty(cacheMap)) {
                logger.info("Jedis获取失败");
                return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
            }
            switch (flag) {
                case "contract":
                    String custNo = cacheMap.get("custNo") + "";
                    String applSeq = cacheMap.get("applSeq") + "";
                    if (!StringUtils.isEmpty(custNo) && !StringUtils.isEmpty(applSeq)) {
                        realmName = "/app/appserver/contract?custNo=" + custNo + "&applseq=" + applSeq;
                        logger.info("------------个人借款合同地址---------" + realmName);
                        map.put("realmName", realmName);
                    }
                    break;
                case "credit": {
                    String custName = (String) cacheMap.get("name");
                    String certNo = (String) cacheMap.get("idNo");
                    if (!StringUtils.isEmpty(custName) && !StringUtils.isEmpty(certNo)) {
                        String custNameB = URLSerializer.encode(Base64Utils.encodeString(custName));
                        /// String custNameB = URLSerializer.encode(new String(Base64.encode(custName), "UTF-8"), "UTF-8");
                        realmName = "/app/appserver/edCredit?custName=" + custNameB + "&certNo=" + certNo;
                        logger.info("--------------征信查询------------" + realmName);
                        map.put("realmName", realmName);
                    }
                    break;
                }
                case "person":
                    realmName = "/app/ht/agreement/PersonInfo.html";
//            realmName = "/static/agreement/PersonInfo.html";
                    logger.info("----------个人信息协议-----------" + realmName);
                    map.put("realmName", realmName);
                    break;
                case "sesame": {
                    String custName = (String) cacheMap.get("name");
                    String custNameB = URLSerializer.encode(Base64Utils.encodeString(custName));
                    realmName = "/app/appserver/seSameCredit?custName=" + custNameB;
                    logger.info("------------芝麻信用授权书展示地址---------" + realmName);
                    map.put("realmName", realmName);
                    break;
                }
            }

        }


        return success(map);
    }

    //获取绑定手机号
    @Override
    public Map<String, Object> getPhoneNo(String token) {
        Map<String, Object> cacheMap = RedisUtils.getExpireMap(token);
        if (MapUtils.isEmpty(cacheMap)) {
            logger.info("Jedis获取失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        String phoneNo = (String) cacheMap.get("phoneNo");
        if (StringUtils.isEmpty(phoneNo)) {
            logger.info("获取手机号为空");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        Map m = new HashMap();
        m.put("phoneNo", phoneNo);
        return success(m);
    }

    //实名认证(标准现金贷)
    @Override
    public IResponse<Map> realAuthenticationForXjd(Map<String, Object> map) throws IOException {
        logger.info("实名认证*********************开始");

        //1.前台参数获取
        String token = (String) map.get("token");
        String verifyNo = (String) map.get("verifyNo");
//        String cityCode = (String) map.get("cityCode");//省市区编码
        String cardnumber = (String) map.get("cardnumber");//卡号
        String mobile = (String) map.get("mobile");//手机号
        String channel = (String) map.get("channel");
        String channelNo = (String) map.get("channelNo");
        //2.前台参数非空判断
        if (StringUtils.isEmpty(token) || StringUtils.isEmpty(verifyNo) ||
                StringUtils.isEmpty(cardnumber) || StringUtils.isEmpty(mobile) ||
                StringUtils.isEmpty(channel) || StringUtils.isEmpty(channelNo)) {
            logger.info("token:" + token + "  verifyNo:" + verifyNo +
                    "  cardnumber:" + cardnumber + "  mobile:" + mobile + "   channel:" + channel + "   channelNo:" + channelNo);
            logger.info("前台获取请求参数有误");
            return CommonResponse.fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        //3.jedis缓存数据获取
        Map<String, Object> cacheMap = RedisUtils.getExpireMap(token);
        if (MapUtils.isEmpty(cacheMap)) {
            logger.info("Jedis数据获取失败");
            return CommonResponse.fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        String name = (String) cacheMap.get("name");//扫描客户姓名
        String birthDt = (String) cacheMap.get("birthday");//扫描出生年月日
        String gender = (String) cacheMap.get("gender");//扫描性别
        String regAddr = (String) cacheMap.get("address");//扫描详细地址
        String validDate = (String) cacheMap.get("validDate");//有效期
        String certOrga = (String) cacheMap.get("issued");//扫描身份证签发机关
        String ethnic = (String) cacheMap.get("race");//扫描民族
        String idCard = (String) cacheMap.get("idCard");//扫描身份证号
        String userId = (String) cacheMap.get("userId");//userId
        if (!StringUtils.isEmpty(idCard)) {
            idCard = idCard.toUpperCase();
            logger.info("扫描的身份证号码是" + idCard);
        } else {
            logger.info("扫描身份证号为空");
            return CommonResponse.fail(ConstUtil.ERROR_CODE, "扫描身份证号为空");
        }
        //4.缓存数据非空判断
        if (StringUtils.isEmpty(name) || StringUtils.isEmpty(birthDt) || StringUtils.isEmpty(gender)
                || StringUtils.isEmpty(regAddr) || StringUtils.isEmpty(validDate) || StringUtils.isEmpty(certOrga)
                || StringUtils.isEmpty(ethnic) || StringUtils.isEmpty(idCard) || StringUtils.isEmpty(userId)) {
            logger.info("name:" + name + "  birthDt:" + birthDt + "  gender:" + gender + "  validDate:" + validDate + "  certOrga:" + certOrga
                    + "   ethnic:" + ethnic + "    idCard:" + idCard + "   userId:" + userId);
            logger.info("Jedis缓存获取信息失败");
            return CommonResponse.fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        //5.校验短信验证码
        Map<String, Object> verifyNoMap = new HashMap<>();
        verifyNoMap.put("phone", mobile);
        verifyNoMap.put("verifyNo", verifyNo);
        verifyNoMap.put("token", token);
        verifyNoMap.put("channel", channel);
        verifyNoMap.put("channelNo", channelNo);
        Map<String, Object> verifyresultmap = appServerService.smsVerify(token, verifyNoMap);
        Map verifyheadjson = (Map<String, Object>) verifyresultmap.get("head");
        String verifyretFlag = (String) verifyheadjson.get("retFlag");
        if (!"00000".equals(verifyretFlag)) {//校验短信验证码失败
            String retMsg = (String) verifyheadjson.get("retMsg");
            return CommonResponse.fail(ConstUtil.ERROR_CODE, retMsg);
        }


        //6.OCR信息保存
        String certStrDt = "";//扫描身份证有效期限开始日期
        String certEndDt = "";//扫描身份证有效期限结束日期
        String vDate = validDate.replaceAll("[-.]", "");
        if (vDate.length() >= 8) {
            certStrDt = vDate.substring(0, 8); //取前8位作为起始日期
            certEndDt = vDate.substring(8);  //剩余的作为截止日期
        }

        if ("男".equals(gender)) {//10男
            gender = "10";
        }
        if ("女".equals(gender)) {//20女
            gender = "20";
        }

        Map<String, Object> ocrMap = new HashMap<>();
        ocrMap.put("custName", name);
        ocrMap.put("afterCustName", name);
        ocrMap.put("birthDt", birthDt);
        ocrMap.put("gender", gender);
        ocrMap.put("regAddr", regAddr);
        ocrMap.put("validDate", validDate);
        ocrMap.put("certStrDt", certStrDt);
        ocrMap.put("certEndDt", certEndDt);
        ocrMap.put("certOrga", certOrga);
        ocrMap.put("ethnic", ethnic);
        ocrMap.put("certNo", idCard);
        ocrMap.put("token", token);
        ocrMap.put("channel", channel);
        ocrMap.put("channelNo", channelNo);
        Map<String, Object> ocrresultmap = appServerService.saveCardMsg(token, ocrMap);
        Map ocrheadjson = (Map<String, Object>) ocrresultmap.get("head");
        String ocrretFlag = (String) ocrheadjson.get("retFlag");
        if (!"00000".equals(ocrretFlag)) {//身份证信息保存失败
            String retMsg = (String) ocrheadjson.get("retMsg");
            return CommonResponse.fail(ConstUtil.ERROR_CODE, retMsg);
        }

        //绑定手机号修改为实名认证手机号
        String phone = cacheMap.get("phoneNo").toString();//得到绑定手机号

        cacheMap.put("phoneNo", mobile);
        RedisUtils.setExpire(token, cacheMap);


        //iservice  根据身份证号码查询人员信息 查询到人员信息进行打标   查询不到则终止流程
        if ("59".equals(channelNo)) {
            //1.调用外联查询人员信息  getPersonnelInformation
            String url = EurekaServer.OUTREACHPLATFORM + "/Outreachplatform/api/iservice/getPersonnelInformation";
            Map<String, Object> param = new HashedMap();
            param.put("channelNo", "pay");
            param.put("businessChannelNo", channelNo);
            param.put("idCard", idCard);
            param.put("days", cashloanConfig.getDays());
            IResponse<Map> resultMap = CommonRestUtils.postForMap(url, param);
            String retFlag = resultMap.getRetFlag();
            if ("00096".equals(retFlag)) {
                return CommonResponse.fail(retFlag, "您暂不符合准入要求，请下载嗨付APP，享受更多金融服务！");
            }
            resultMap.assertSuccessNeedBody();
            Map bodyMap = resultMap.getBody();
            List data = (List) bodyMap.get("data");
            if (data.size() != 0) {
                Map object = (Map) data.get(0);
                String postDate = object.get("postDate").toString();
                TimeSpan time = new TimeSpan(DateUtils.now(), DateUtils.fromDateString(postDate));
                if (time.getDays() < 180) {
                    return CommonResponse.fail(ConstUtil.ERROR_CODE, "您暂不符合准入要求，请下载嗨付APP，享受更多金融服务！");
                }
            } else {
                return CommonResponse.fail(ConstUtil.ERROR_CODE, "您暂不符合准入要求，请下载嗨付APP，享受更多金融服务！");
            }

        }

        //7.验证并新增实名认证信息
        Map<String, Object> identityMap = new HashMap<>();
        identityMap.put("token", token);
        identityMap.put("channel", channel);
        identityMap.put("channelNo", channelNo);
        identityMap.put("custName", name); //客户姓名 √
        identityMap.put("certNo", idCard); //身份证号 √
        identityMap.put("cardNo", cardnumber); //银行卡号 √
        identityMap.put("mobile", mobile); //手机号 √
        identityMap.put("dataFrom", channelNo); //数据来源 √
        identityMap.put("threeParamVal", ConstUtil.THREE_PARAM_VAL_N); //是否需要三要素验证
        identityMap.put("userId", userId); //客户userId
        identityMap.put("bindMobile", phone);
        Map<String, Object> identityresultmap = appServerService.fCiCustRealThreeInfo(token, identityMap);
        Map identityheadjson = (Map<String, Object>) identityresultmap.get("head");
        String identityretFlag = (String) identityheadjson.get("retFlag");
        if (!"00000".equals(identityretFlag)) {
            String retMsg = (String) identityheadjson.get("retMsg");
            return CommonResponse.fail(ConstUtil.ERROR_CODE, retMsg);
        }


        Map identitybodyjson = (Map<String, Object>) identityresultmap.get("body");
        //信息保存
        String custNo = (String) identitybodyjson.get("custNo");
        String custName = (String) identitybodyjson.get("custName");
        String cardNo = (String) identitybodyjson.get("cardNo");
        String bankCode = (String) identitybodyjson.get("acctBankNo");
        String bankName = (String) identitybodyjson.get("acctBankName");
        String idNo = (String) identitybodyjson.get("certNo");
        String idType = (String) identitybodyjson.get("certType");
        String cardPhone = (String) identitybodyjson.get("mobile");

        cacheMap.put("custNo", custNo);
        cacheMap.put("custName", custName);
        cacheMap.put("cardNo", cardNo);
        cacheMap.put("bankCode", bankCode);
        cacheMap.put("bankName", bankName);
        cacheMap.put("idNo", idNo);
        cacheMap.put("idType", idType);
        cacheMap.put("cardPhone", cardPhone);
        RedisUtils.setExpire(token, cacheMap);

        //OCR图片路径上送
        Map<String, String> pathMap = new HashMap<>();
        pathMap.put(CERT_IMAGE_PATH_A, (String) cacheMap.get(CERT_IMAGE_PATH_A));//正面共享盘位置
        pathMap.put(CERT_IMAGE_PATH_B, (String) cacheMap.get(CERT_IMAGE_PATH_B));//反面共享盘位置
        for (Map.Entry<String, String> entry : pathMap.entrySet()) {
            Map<String, Object> paramMap = new HashMap<>();
            if (CERT_IMAGE_PATH_A.equals(entry.getKey())) {//正面
                paramMap.put("attachType", ConstUtil.CERT_FILE_TYPE_A);//影像类型
                paramMap.put("attachName", ConstUtil.CERT_FILE_NAME_A);//影像名称
            } else if (CERT_IMAGE_PATH_B.equals(entry.getKey())) {//反面
                paramMap.put("attachType", ConstUtil.CERT_FILE_TYPE_B);
                paramMap.put("attachName", ConstUtil.CERT_FILE_NAME_B);
            }
            String filePath = entry.getValue();//文件路径
            InputStream is = new BufferedInputStream(new FileInputStream(String.valueOf(filePath)));
            //获取MD5码
            boolean isA = CERT_IMAGE_PATH_A.equals(entry.getKey());
            String md5Code = DigestUtils.md5Hex(is);
            paramMap.put("token", token);
            paramMap.put("md5", md5Code);//文件md5码
            paramMap.put("custNo", custNo);
            paramMap.put("filePath", filePath);//文件路径
            paramMap.put("idNo", idCard);//身份证号
            paramMap.put("commonCustNo", null);//共同还款人编号，传null
            logger.info("实名绑卡，上传身份证" + (isA ? "正" : "反") + "面，请求参数：" + paramMap);
            Map<String, Object> uploadresultmap = appServerService.attachUploadPersonByFilePath(token, paramMap);
            JSONObject uploadresult = new JSONObject(uploadresultmap);
            JSONObject head = uploadresult.getJSONObject("head");
            String retflag = head.getString("retFlag");
            if ("00000".equals(retflag)) {
                logger.info("实名绑卡，上传身份证" + (isA ? "正" : "反") + "面成功");
            } else {
                logger.info("实名绑卡，上传身份证" + (isA ? "正" : "反") + "面失败");
            }
        }
        logger.info("实名认证***********************结束");
        cacheMap.put("phoneNo", mobile);
        RedisUtils.setExpire(token, cacheMap);


        logger.info("绑定手机号***********************结束");

        Map<String, Object> hrparamMap = new HashMap<>();
        hrparamMap.put("custName", custName);
        hrparamMap.put("idTyp", idType);
        hrparamMap.put("idNo", idNo);
        Map<String, Object> custWhiteListCmis = custExtInfoService.getCustWhiteListCmis(token, channel, channelNo, hrparamMap);
        Map updmobileheadjson = (Map<String, Object>) custWhiteListCmis.get("head");
        String updmobileretflag = (String) updmobileheadjson.get("retFlag");
        if (!"00000".equals(updmobileretflag)) {
            String retMsg = (String) updmobileheadjson.get("retMsg");
            return CommonResponse.fail(ConstUtil.ERROR_CODE, retMsg);
        }

        Map<String, Object> resultparamMap = new HashMap<>();
        resultparamMap.put("flag", "1");//通过  个人扩展信息
        return CommonResponse.success(resultparamMap);
    }


    public Map<String, Object> realAuthenticationForHF(Map<String, Object> map) {
        logger.info("实名认证**********************开始");

        //1.获取前台参数
        String custName = (String) map.get("custName"); //用户名
        String certNo = (String) map.get("certNo"); //身份证号
        String cardnumber = (String) map.get("cardnumber");//卡号
        String mobile = (String) map.get("mobile");//手机号
        String verifyNo = (String) map.get("verifyNo"); //验证码
        String token = (String) map.get("token");
        String channel = (String) map.get("channel");
        String channelNo = (String) map.get("channelNo");

        if (StringUtils.isEmpty(custName) || StringUtils.isEmpty(certNo) || StringUtils.isEmpty(cardnumber)
                || StringUtils.isEmpty(mobile) || StringUtils.isEmpty(verifyNo) || StringUtils.isEmpty(token)
                || StringUtils.isEmpty(channel) || StringUtils.isEmpty(channelNo)) {
            logger.info("token:" + token + "  verifyNo:" + verifyNo +
                    "  cardnumber:" + cardnumber + "  mobile:" + mobile + "   channel:" + channel + "   channelNo:" + channelNo);
            logger.info("前台获取请求参数有误");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }


        //2.jedis缓存数据获取
        Map<String, Object> cacheMap = RedisUtils.getExpireMap(token);
        if (MapUtils.isEmpty(cacheMap)) {
            logger.info("Jedis数据获取失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        String name = (String) cacheMap.get("name");//扫描客户姓名
        String birthDt = (String) cacheMap.get("birthday");//扫描出生年月日
        String gender = (String) cacheMap.get("gender");//扫描性别
        String regAddr = (String) cacheMap.get("address");//扫描详细地址
        String validDate = (String) cacheMap.get("validDate");//有效期
        String certOrga = (String) cacheMap.get("issued");//扫描身份证签发机关
        String ethnic = (String) cacheMap.get("race");//扫描民族
        String idCard = (String) cacheMap.get("idCard");//扫描身份证号
        String userId = (String) cacheMap.get("userId");//userId
        if (!StringUtils.isEmpty(idCard)) {
            idCard = idCard.toUpperCase();
            logger.info("扫描的身份证号码是" + idCard);
        } else {
            logger.info("扫描身份证号为空");
            return fail(ConstUtil.ERROR_CODE, "扫描身份证号为空");
        }
        //3.缓存数据非空判断
        if (StringUtils.isEmpty(name) || StringUtils.isEmpty(birthDt) || StringUtils.isEmpty(gender)
                || StringUtils.isEmpty(regAddr) || StringUtils.isEmpty(validDate) || StringUtils.isEmpty(certOrga)
                || StringUtils.isEmpty(ethnic) || StringUtils.isEmpty(idCard) || StringUtils.isEmpty(userId)) {
            logger.info("name:" + name + "  birthDt:" + birthDt + "  gender:" + gender + "  validDate:" + validDate + "  certOrga:" + certOrga
                    + "   ethnic:" + ethnic + "    idCard:" + idCard + "   userId:" + userId);
            logger.info("Jedis缓存获取信息失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }


        //4.校验短信验证码
        Map<String, Object> verifyNoMap = new HashMap<>();
        verifyNoMap.put("phone", mobile);
        verifyNoMap.put("verifyNo", verifyNo);
        verifyNoMap.put("token", token);
        verifyNoMap.put("channel", channel);
        verifyNoMap.put("channelNo", channelNo);
        Map<String, Object> verifyresultmap = appServerService.smsVerify(token, verifyNoMap);
        Map verifyheadjson = (Map<String, Object>) verifyresultmap.get("head");
        String verifyretFlag = (String) verifyheadjson.get("retFlag");
        if (!"00000".equals(verifyretFlag)) {//校验短信验证码失败
            String retMsg = (String) verifyheadjson.get("retMsg");
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }
        return null;
    }
}
