package com.haiercash.appserver.service;

import com.haiercash.common.data.CommonRepaymentPerson;
import com.haiercash.common.data.UAuthCASignRequest;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface CASignService {

	/**
	 * 添加签章请求.
	 * @param request 签章详情
	 * @return boolean
	 */
	boolean caRequest(UAuthCASignRequest request);

	/**
	 * 互动金融添加签章请求.(state为4)
	 * @param request 签章详情
	 * @return boolean
     */
	boolean hdjrCaRequest(UAuthCASignRequest request);


	/**
	 * 签章
	 * 
	 * @param orderNo
	 * @param clientId
	 * @param flag
	 * @return
	 */
	Map<String, Object> caSignRequest(String orderNo, String clientId, String flag);


	/**
	 * 共同还款人协议签章请求
	 *
	 * @param commonRepaymentPerson 共同还款人信息
	 * @return
	 */
	Map<String, Object> commonRepayPersonCaSignRequest(CommonRepaymentPerson commonRepaymentPerson);


      /**
     * 提额征信授权书签章
     * @param applseq -> 提额流水号
     * @param custName
     * @param certNo
     * @return
     */
    void riseAmountCaSign(String applseq, String custNo, String custName, String certNo);

	/**
	 * 检查用户四要素
	 *
	 * @param code
	 * @param userId
	 * @return
	 */
	Map<String,Object> checkCaFourKeysInfo(String code, String userId);


}
