package com.haiercash.appserver.web;

import com.haiercash.appserver.service.*;
import com.haiercash.appserver.util.HttpUploadFile;
import com.haiercash.common.data.*;
import com.haiercash.commons.util.CommonProperties;
import com.haiercash.commons.util.EncryptUtil;
import com.haiercash.commons.util.RedisUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import redis.clients.jedis.Jedis;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;


/**
 * Created by yuanli on 2017/4/11.
 */
@RestController
public class QiaoRongController extends BaseController {
    private Log logger = LogFactory.getLog(this.getClass());
    @Autowired
    CmisApplService cmisApplService;
    @Autowired
    CrmService crmService;
    @Autowired
    UAuthCASignRequestRepository uAuthCASignRequestRepository;
    @Autowired
    AppOrderService appOrderService;
    @Autowired
    AppContractRepository appContractRepository;
    @Autowired
    ContractAssInfoRepository contractAssInfoRepository;
    @Autowired
    MoxieInfoRepository moxieInfoRepository;
    @Autowired
    private QiaoRongService qiaoRongService;
    @Autowired
    AttachService attachService;

    public QiaoRongController() {
        super("67");
    }


    @Value("${common.qrdz.back_url}")
    protected String back_url;

    @Value("${common.face.face_api_key}")
    protected String face_api_key;

    @Value("${common.face.face_api_secret}")
    protected String face_api_secret;

    @Value("${common.face.face_getresult_url}")
    protected String face_getresult_url;

    @Value("${common.face.face_DataImg_url}")
    protected String face_DataImg_url;


    /**
     * 查询是否做过魔蝎
     * @param code
     * @return
     */
    @RequestMapping(value = "/app/appserver/isHaveMoxieByCode", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> isHaveMoxieByCode(@RequestParam("code") String code) {
        logger.info("*************************查询是否已魔蝎认证接口**********************");
        UAuthCASignRequest request = uAuthCASignRequestRepository.findOne(code);
        String applseq = request.getApplseq();
        Map<String, Object> map =  qiaoRongService.getMoxieByApplseq(applseq);
        logger.info("查询是否已魔蝎认证接口******返回："+map);
        return success(map);
    }

    /**
     * 根据code查询传往魔蝎的userId
     * @param code
     * @return
     */
    @RequestMapping(value = "/app/appserver/getMoxieUserId", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getMoxieUserId(@RequestParam("code") String code) {
        UAuthCASignRequest request = uAuthCASignRequestRepository.findOne(code);
        String applseq = request.getApplseq();//申请流水号

        Jedis jedis2 = RedisUtil.getJedis();
        Map m2= jedis2.hgetAll(code);
        String name = (String) m2.get("name");
        String phone = (String) m2.get("phone");
        String idNo = (String) m2.get("idNo");
        jedis2.close();

        /*Map<String, Object> map = qiaoRongService.getCustInfo(code);
        String idNo = (String) map.get("idNo");*/

        JSONObject jbo = new JSONObject();
        jbo.put("applseq", applseq);
        jbo.put("idNo", idNo);
        //userId  加密
        String userId = jbo.toString();
        String str = EncryptUtil.simpleEncrypt(userId);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("userId", str);
        map.put("name", name);
        map.put("phone", phone);
        map.put("idNo", idNo);
        map.put("appurl", CommonProperties.get("other.domain").toString());

        return success(map);
    }

    /**
     * face++失败，上传替代影像
     * @param handImg
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/app/appserver/fileUpload", method = RequestMethod.POST)
    public Map<String, Object> fileUpload(@RequestBody MultipartFile handImg, HttpServletRequest request, HttpServletResponse response) {
        // 判断文件是否为空
        logger.info("*************************乔融豆子上传替代影像**********************");
        String code = request.getParameter("code");
        if(code == null || "".equals(code)){
            logger.info("code为空");
            return fail("11", "网络通讯异常");
        }
        Map<String, Object> map = qiaoRongService.fileUpload(handImg, code);
        return map;
    }

    /**
     * 百融注册事件 + 判断是否做过人脸识别
     * @param code
     * @param regNum
     * @param regEvent
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/app/appserver/isNeedFaceCheck", method = RequestMethod.GET)
    public Map<String, Object> isNeedFaceCheck(@RequestParam("code") String code,
                                               @RequestParam("regNum") String regNum,
                                               @RequestParam("regEvent") String regEvent) throws Exception {
        //百融注册事件
        qiaoRongService.brRegister(code, regNum);
        //是否做过人脸识别
        Map<String, Object> map = qiaoRongService.isNeedFaceCheck(code);
        return map;
    }

    /**
     * 根据code获取缓存信息
     * @param code
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/app/appserver/getCustInfo", method = RequestMethod.GET)
    public Map<String, Object> getCustInfo(@RequestParam("code") String code){

        UAuthCASignRequest carequest = uAuthCASignRequestRepository.findOne(code);
        String applseq = carequest.getApplseq();

        Jedis jedis2 = RedisUtil.getJedis();
        Map m2= jedis2.hgetAll(code);
        String name = (String) m2.get("name");
        String phone = (String) m2.get("phone");
        String idNo = (String) m2.get("idNo");
        jedis2.close();

        Map map = new HashMap();
        map.put("name", name);
        map.put("phone", phone);
        map.put("idNo", idNo);
        map.put("applseq", applseq);

        return success(map);
    }


    /**
     * 获取faceID当前结果
     * @param code
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/app/appserver/getFaceIDResult", method = RequestMethod.GET)
    public Map<String, Object> getFaceIDResult(@RequestParam("code") String code) throws Exception {
        Map<String, Object> map = qiaoRongService.getFaceIDResult(code);
        return map;
    }

    //face++回调接口
    @RequestMapping(value = "/app/appserver/faceReturnUrl", method = RequestMethod.POST)
    public void haiercashFaceReturnUrl(HttpServletRequest request, HttpServletResponse response, @RequestBody String code){
        try {
            logger.info("********************face++接口回调开始************************");
            String code0 = request.getParameter("code");
            logger.info("code0:"+code0);
            logger.info("回调返回信息" + code);
            String url = "/app/ht/qrdz/frontPhotograph.html?code=" + code0;
            //String url = CommonProperties.get("other.domain").toString() + "/app/ht/qrdz/frontPhotograph.html?code=" + code0;
            logger.info("face++人脸识别跳转识别中页面，页面地址："+url);
            response.sendRedirect(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //face++回调通知接口
    @RequestMapping(value = "/app/appserver/faceNotifyUrl", method = RequestMethod.POST)
    public void haiercashFaceNotifyUrl(HttpServletRequest request, HttpServletResponse response, @RequestBody String code){
        logger.info("********************face++回调通知接口开始************************");
        logger.info("回调返回信息" + code);
    }

    /**
     * 实名验证（登录页面）
     * @param code
     * @param name
     * @param phone
     * @param idNo
     * @param cardnumber
     * @param totalamount
     * @return
     */
    @RequestMapping(value="/app/appserver/qrdzCheckFourKeys", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object>  checkCaFourKeysInfo(@RequestParam("code")String code,
                                                    @RequestParam("name")String name,
                                                    @RequestParam("phone")String phone,
                                                    @RequestParam("idNo")String idNo,
                                                    @RequestParam("cardnumber")String cardnumber,
                                                    @RequestParam("totalamount")String totalamount){

        logger.info("乔融豆子******实名认证*********开始");
        if(StringUtils.isEmpty(code)){
            logger.info("乔融豆子******实名认证*********返回信息"+fail("09", "code不能为空").toString());
            return fail("09", "code不能为空");
        }
        if(StringUtils.isEmpty(name)){
            logger.info("乔融豆子******实名认证*********返回信息"+fail("09", "姓名不能为空").toString());
            return fail("09", "姓名不能为空");
        }
        if(StringUtils.isEmpty(phone)){
            logger.info("乔融豆子******实名认证*********返回信息"+fail("09", "手机号不能为空").toString());
            return fail("09", "手机号不能为空");
        }
        if(StringUtils.isEmpty(idNo)){
            logger.info("乔融豆子******实名认证*********返回信息"+fail("09", "身份证号不能为空").toString());
            return fail("09", "身份证号不能为空");
        }
        if(StringUtils.isEmpty(cardnumber)){
            logger.info("乔融豆子******实名认证*********返回信息"+fail("09", "银行卡号不能为空").toString());
            return fail("09", "银行卡号不能为空");
        }
        if(StringUtils.isEmpty(totalamount)){
            logger.info("乔融豆子******实名认证*********返回信息"+fail("09", "申请金额不能为空").toString());
            return fail("09", "申请金额不能为空");
        }

        Jedis jedis = RedisUtil.getJedis();
        Map m = new HashMap<>();
        m.put("code", code);
        m.put("name", name);
        m.put("phone", phone);
        m.put("idNo", idNo);
        m.put("cardnumber", cardnumber);
        m.put("totalamount", totalamount);
        jedis.hmset(code, m);
        jedis.close();

        Map<String, Object> map = qiaoRongService.checkCaFourKeysInfo(code, name, phone, idNo, cardnumber, totalamount);
        logger.info("乔融豆子******实名认证*********结束");
        return map;
    }

    @RequestMapping(value = "/app/appserver/getLoanByApplseq", method = RequestMethod.GET)
    @ResponseBody
    public String getLoanByApplseq(@RequestParam("applseq")String applseq) {
        String loan = qiaoRongService.getloanByapplseq(applseq);
        return loan;
    }

    /**
     * 加密数据
     * @param param
     * @return
     */
    @RequestMapping(value = "/app/appserver/encryptParam", method = RequestMethod.GET)
    @ResponseBody
    public String encryptParam(@RequestParam("param")String param) {
        String str = EncryptUtil.simpleEncrypt(param);
        return str;
    }

    /**
     * 合同确认
     * @param map
     * @return
     */
    @RequestMapping(value="/app/appserver/ca/loanconfirm", method = RequestMethod.PUT)
    @ResponseBody
    public Map<String, Object> loanconfirm(@RequestBody Map<String, String> map){
        String code = (String) map.get("code");

        if (StringUtils.isEmpty(code)) {
            return fail("06", "获取唯一码失败");
        }

        Map<String, Object> resultMap = qiaoRongService.loanconfirm(map);
        return resultMap;
    }

    @RequestMapping(value = "/app/appserver/test", method = RequestMethod.GET)
    public Map<String, Object> gettest(@RequestParam("applseq")String applseq) throws Exception {

        //http://139.196.45.193:20011/haier/haierCashEntranceCallback?applseq=1325077
        String backurl = back_url + "/haier/haierCashEntranceCallback?applseq=" + applseq;
        logger.info("乔融豆子*******签章回调地址：" + backurl);
        String resData = HttpUploadFile.sendGet(backurl, "");
        System.out.println("返回信息："+resData);

        String url = back_url;
        String face= face_api_key;
        System.out.println("url:"+url+"    face:"+face);

//        Jedis jedis = RedisUtil.getJedis();
//        Map m = new HashMap<>();
//        m.put("a","aaa");
//        m.put("b","bbb");
//        jedis.hmset("ab", m);
//
//        Map m0 = jedis.hgetAll("ab");
//        String a = (String) m0.get("a");
//        String b = (String) m0.get("b");
//
//        jedis.close();
//
//        Jedis jedis1 = RedisUtil.getJedis();
//        Map m1 = jedis1.hgetAll("ab");;
//        String a1 = (String) m1.get("a");
//        String b1 = (String) m1.get("b");
//
//        jedis1.del("ab");
//
//        Map m00 = jedis1.hgetAll("ab");;
//        String a20 = (String) m00.get("a");
//        String b20 = (String) m00.get("b");
//
//        m1.put("b","cc");
//        m1.put("c","ccc");
//        m1.put("d","ddd");
//        jedis1.hmset("ab", m1);
//        jedis1.close();
//
//        Jedis jedis2 = RedisUtil.getJedis();
//        Map m2= jedis2.hgetAll("ab");
//        String a2 = (String) m2.get("a");
//        String b2 = (String) m2.get("b");
//        String c2 = (String) m2.get("c");
//        String d2 = (String) m2.get("d");
//
//        //jedis2.del("ab");
//        jedis2.close();
//
//        Jedis jedis3 = RedisUtil.getJedis();
//        Map m3= jedis3.hgetAll("ab");
//        int s = m3.size();
//        jedis3.close();
        /*jedis.set("biz_id","asadad");
        String ls = jedis.get("biz_id");
        jedis.del("biz_id");
        String ls1 = jedis.get("biz_id");
        jedis.close();*/


        /*jedis.set("biz_id","asadad");
        String ls = jedis.get("biz_id");
        jedis.del("biz_id");
        String ls1 = jedis.get("biz_id");
        jedis.close();*/

        //Map<String, Object> map = qiaoRongService.getFaceIDResult(code);

        return success(resData);
    }

}