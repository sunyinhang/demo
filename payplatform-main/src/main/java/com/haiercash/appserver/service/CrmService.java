package com.haiercash.appserver.service;


import com.haiercash.common.data.CustomerInfoBean;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Crm service interface.
 * @author Liu qingxiang
 * @since v1.4.0
 */
@Service
public interface CrmService {

    /**
     *
     * @param custNo 客户编号
     * @param flag   是否需要传联系人 Y 需要  N 不需要
     * @return
     */
    Map<String, Object> getCustExtInfo(String custNo, String flag);

    /**
     * 调用crm /app/crm/cust/fCiCustRealThreeInfo 接口
     *
     *
     * @param customerInfoBean
     * apptCustName 客户姓名 必输
     * apptIdNo 身份证号 必输
     * repayApplCardNo 银行卡号 设置为默认还款卡号 必输
     * indivMobile 手机号 必输
     * appInAdvice 数据来源 必输 （app_person:个人版；app_merch:商户版；rrs:日日顺；ca:签章平台）
     * reserved1 是否需要验证三要素 （1：需要；-1：不需要） 非必输
     * userId 登录用户名 非必输
     * repayAcProvince 开户行省代码 非必输
     * repayAcCity 开户行市代码 非必输
     * cooprCde 所属门店 非必输
     * @return
     */
    Map<String,Object> checkAndAddFourKeysRealInfo(CustomerInfoBean customerInfoBean);

    Map<String,Object> getAllCustExtInfo(String custNo, String flag);

    /**
     * 整合crm85接口  修改保存客户所有扩展信息
     *
     * @param requestMap
     * @return
     */
    Map<String, Object> saveAllCustExtInfo(Map requestMap);

    /**
     * 整合crm1接口  保存/修改 单位(个人、房产)信息
     * @param requestMap
     * @return
     */
    Map<String, Object> saveCustExtInfo(Map requestMap);

    /**
     *  整合crm6接口  新增/修改 联系人
     * @param requestMap
     * @return
     */
    Map<String, Object> saveCustFCiCustContact(Map requestMap);

    /**
     * 整合crm4接口  查询个人(单位、房产)信息
     * @param custNo
     * @param pageName
     * @return
     */
    Map<String, Object> getCrm4CustExtInfo(String custNo, String pageName);

    /**
     * 整合crm64接口  查询指定银行卡的所有信息
     * @param cardNo
     * @return
     */
    Map<String, Object> getBankInfo(String cardNo);

    /**
     *  整合crm12接口  查询所有支持的银行列表
     * @return
     */
    Map<String, Object> getBankList();

    /**
     *  整合crm66接口  验证并新增实名认证信息
     * @param requestMap
     * @return
     */
    Map<String, Object> fCiCustRealThreeInfo(Map requestMap, String channelNo);

    /**
     * 整合crm26接口  查询实名认证信息
     * @param custNo
     * @return
     */
    Map<String, Object> queryCustRealInfoByCustNo(String custNo);

    /**
     * 整合crm13接口  查询实名认证客户信息
     * @param custName
     * @param certNo
     * @return
     */
    Map<String, Object> queryMerchCustInfo(String custName, String certNo);

    /**
     * 整合crm50接口  查询商户对应门店列表
     * @param merchNo
     * @param userId
     * @return
     */
    Map<String, Object> findMerchStore(String merchNo, String userId);

    /**
     *  整合crm24接口  查询商户列表
     * @param userId
     * @return
     */
    Map<String, Object> getMerchs(String userId);

    /***
     * 整合crm90接口  查询客户当前集团总积分
     * @param custNo
     * @return
     */
    Map<String, Object> queryPointByCustNo(String custNo);

    Map<String, Object> getLoanCdeByTagId(String tagId);

    Map<String, Object> getIfShhSmrz(String custName, String idNo, String mobile, String idTyp, String cardNo, String provinceCode, String cityCode);

    Map<String, Object> getCustISExistsInvitedCauseTag(String custName, String certNo, String phonenumber, String cardNo);

    Map<String, Object> findAreaCodes(String areaCode);

    Map<String, Object> findUserIdByStoreNo(String storeNo);

    Map<String, Object> findDmAreaInfo(String areaCode, String flag);

    Map<String, Object> getMsgByScan(String userId, String scanCode);

    Map<String, Object> getCommonApplType(String userId, String goodsCode);

    Map<String, Object> coopMerchs(String deptId, Integer page, Integer pageNum);

    /**
     * 查询用户准入资格
     * @param params custName certNo
     * @return
     */
    Map<String, Object> getCustIsPass(Map<String, Object> params);

    /**
     * 获取指定卡信息
     * @param custNo 客户编号
     * @param cardNo 指定卡号
     * @return  bankName
     *          bankCode
     *          accBchCde
     *          accBchName
     *          cardNo
     *          acctProvince
     *          acctCity
     *          mobile
     */
    Map<String, Object> getCardInfo(String custNo, String cardNo);

    /**
     * 获取银行卡信息
     * @param custNo 客户编号
     * @return
     */
    Map<String, Object> getBankCard(String custNo);

    /**
     * 获取银行卡信息
     * @param cardNo 卡号
     * @return  bankName
     *          bankCode
     *          accBchCde
     *          accBchName
     *          cardNo
     *          acctProvince
     *          acctCity
     *          mobile
     */
    Map<String, Object> getCustBankCardByCardNo(String cardNo);
}
