package com.haiercash.appserver.web;

import com.haiercash.appserver.service.FileSignService;
import com.haiercash.common.data.UAuthCASignRequest;
import com.haiercash.common.data.UAuthCASignRequestRepository;
import com.haiercash.commons.util.EncryptUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ManualController extends BaseController {
    public ManualController() {
        super("FF");
    }
    @Autowired
    UAuthCASignRequestRepository uAuthCASignRequestRepository;

    @Autowired
    private FileSignService fileSignService;

    // 注册协议文件签名并上传到指定目录、合同
    @RequestMapping(value = "/app/appserver/manual/signAgreement", method = RequestMethod.GET)
    @ResponseBody
    // signType: HCFC-CKD-HAIER HCFC-CWD-TX
    public Map<String, Object> signAgreement(@RequestParam("signCode") String signCode) {
//        FileSignUtil.deleteFiles = false;
        UAuthCASignRequest uAuthCASignRequest = uAuthCASignRequestRepository.findBySignCode(signCode);
        fileSignService.signAgreement(uAuthCASignRequest.getOrderJson(),
                uAuthCASignRequest.getSignType(), "1", true ,signCode);
//        FileSignUtil.deleteFiles = true;
        return success();
    }

    // 征信协议、注册协议模板转换及签章
    @RequestMapping(value = "/app/appserver/manual/signCreditAgreement", method = RequestMethod.GET)
    @ResponseBody
    // type:credit,register
    public Map<String, Object> signCreditAgreement(@RequestParam("signCode") String signCode,
                                                   @RequestParam(name="type", required=false) String commonFlag,
                                                   @RequestParam(name="commonCustName", required=false) String commonCustName,
                                                   @RequestParam(name="commonCustCertNo", required=false) String commonCustCertNo) {
//        FileSignUtil.deleteFiles = false;
        UAuthCASignRequest uAuthCASignRequest = uAuthCASignRequestRepository.findBySignCode(signCode);
        fileSignService.signCreditAgreement(uAuthCASignRequest.getOrderJson(), uAuthCASignRequest.getSignType(),
                commonFlag, commonCustName, commonCustCertNo, signCode);
//        FileSignUtil.deleteFiles = true;
        return success();
    }

    // 共同还款人协议模板转换及签章
    @RequestMapping(value = "/app/appserver/manual/signCommonAgreement", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> signCommonAgreement(@RequestParam("signCode") String signCode) {
        UAuthCASignRequest uAuthCASignRequest = uAuthCASignRequestRepository.findBySignCode(signCode);
        fileSignService.signCommonAgreement(uAuthCASignRequest);
        return success();
    }


    @RequestMapping(value = "/app/appserver/manual/decrypt", method = RequestMethod.GET)
    @ResponseBody
    public String decrypt(@RequestParam("src") String src) {
        logger.info("src=" + src);
        String result = EncryptUtil.simpleDecrypt(src);
        logger.info("result=" + result);
        return result;
    }
    @RequestMapping(value = "/app/appserver/manual/encrypt", method = RequestMethod.GET)
    @ResponseBody
    public String encrypt(@RequestParam("src") String src) {
        logger.info("src=" + src);
        String result = EncryptUtil.simpleEncrypt(src);
        logger.info("result=" + result);
        return result;
    }
}
