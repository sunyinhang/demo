package com.haiercash.appserver.service;

import com.haiercash.common.data.UAuthCASignRequest;
import org.springframework.stereotype.Service;

/**
 * Sign Service inerface.
 *
 * @author Liu qingxiang.
 * @since v1.1.0
 */
@Service
public interface FileSignService {

    /**
     * 文件签名统一入口.
     * @param signRequest
     * @return String
     */
     String sign(UAuthCASignRequest signRequest);

    /**
     * 注册协议文件签名并上传到指定目录.
     *
     * @param orderJson    合同信息，JSON字符串
     * @param signType     签章类型
     * @param contractFlag 提交类型：1-申请提交，2-合同提交
     * @param isManual     手工签章标识，用于签章测试，实际业务逻辑始终传false
     * @return 完成签名签章的文件名，含影像系统ftp路径，
     * 如： CA/app_1234_20160411180501.pdf
     */
    String signAgreement(String orderJson, String signType, String contractFlag,
            boolean isManual, String signCode);

    /**
     * 征信协议、注册协议模板转换及签章.
     *
     * @param orderJson
     * @param type             sign type
     * @param commonFlag
     * @param commonCustName
     * @param commonCustCertNo
     * @param signCode
     * @return String
     */
    String signCreditAgreement(String orderJson, String type, String commonFlag,
            String commonCustName, String commonCustCertNo, String signCode);

    /**
     * 共同还款人协议模板转换及签章.
     *
     * @param signRequest
     * @return String
     */
    String signCommonAgreement(UAuthCASignRequest signRequest);

    /**
     * 根据签章请求中订单json信息进行签章.
     * @param orderJson 订单json
     * @param signType 签章类型
     * @param contractFlag
     * @param isManual
     * @param signCode
     * @return
     */
    String signAgreementByOrderJson(String orderJson, String signType, String contractFlag, boolean isManual,
            String signCode);

    String signAgreementByCmis(String Applseq, String signType, String contractFlag, boolean isManual,
                                      String signCode);
    /**
     * 变更银行卡模板签章.
     *
     * @param signRequest
     * @return String
     */
    String signBankCardGrantAgreement(UAuthCASignRequest signRequest);

    /**
     * 提额征信授权书签章.
     * @param signRequest 签章请求信息
     * @return String
     */
    String signRiseEdAgreement(UAuthCASignRequest signRequest);
}
