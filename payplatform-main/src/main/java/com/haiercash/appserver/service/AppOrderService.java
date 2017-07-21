package com.haiercash.appserver.service;

import com.haiercash.common.data.AppOrder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author liuhongbin
 * @date 2016/5/20
 * @description:
 **/
@Service
public interface AppOrderService {

    /**
     * 把门店信息写入订单
     *
     * @param order 订单对象
     */
    void updateStoreInfo(AppOrder order, String token);

    /**
     * 把销售代表信息写入订单
     *
     * @param order 订单对象
     */
    void updateSalesInfo(AppOrder order, String token);

    /**
     * 把实名认证信息写入订单
     *
     * @param order 订单对象
     */
    Map<String, Object> updateCustRealInfo(AppOrder order, String token);

    /**
     * 把贷款品种信息写入订单
     *
     * @param order 订单对象
     */
    void updateTypInfo(AppOrder order, String token);

    /**
     * 计算首付比例
     *
     * @param order
     */
    void calcFstPct(AppOrder order);

    /**
     * 获取还款卡信息
     *
     * @param order
     * @param token
     */
    void setHkNo(AppOrder order, String token);


    /**
     * 更新订单信息。
     * <p>
     * 如果为现金贷，则走收单系统，
     * 如果为商品贷，则走订单系统.
     * </p>
     *
     * @param src
     * @return
     */
    Map<String, Object> updateOrder(AppOrder src);

    /**
     * 使用绑定手机号（个人版订单保存使用 6.43）
     *
     * @param order
     * @param token
     */
    void updateBindMobile(AppOrder order, String token);

    /**
     * 通过用户Id查询统一认证手机号
     *
     * @param userId
     * @param token
     * @return
     */
    String getBindMobileByUserId(String userId, String token);

    /**
     * 根据客户姓名及身份证号查询统一认证手机号
     *
     * @param custName
     * @param idNo
     * @param token
     * @return
     */
    String getBindMobileByCustNameAndIdNo(String custName, String idNo, String token);

    /**
     * 获取实名认证的手机号
     *
     * @param userId
     * @param custName
     * @param idNo
     * @param token
     * @return
     */
    String getMobileBySmrz(String userId, String custName, String idNo, String token);


    /**
     * 合同签约提交调用渠道进件接口
     */
    Map<String, Object> subSignContractQdjj(AppOrder order);

    //根据原始订单的客户信息，更新订单手机号为绑定手机号，并保存数据库
    void updateAppOrderMobile(AppOrder order, String token);

    Map<String, Object> getCustIsPassFromCrm(String custName, String idNo, String mobile);

    Map<String, Object> getCustLevel(String custNo);

    /**
     * 从信贷获取完整的订单信息Map
     *
     * @param applSeq 流水号
     * @return
     */
    Map<String, Object> getAppOrderMapFromCmis(String applSeq);

    /**
     * 从信贷获取订单信息.仅限签章服务使用，订单信息未完全获取。
     *
     * @param applSeq 流水号
     * @param version 版本号
     * @return
     */
    AppOrder getAppOrderFromCmis(String applSeq, String version);

    /**
     * 从收单系统获取订单信息.
     *
     * @param applSeq 流水号
     * @param version 版本号
     * @return
     */
    AppOrder getAppOrderFromACQ(String applSeq, String version);

    /**
     * 从信贷获取订单信息，所有客户信息都从cmis获取，用于互动金融
     *
     * @param applSeq 流水号
     * @param version 版本号
     * @return
     */
    AppOrder getAppOrderAllFromCmis(String applSeq, String version);

    /**
     * 获取合同确认页信息
     *
     * @param signCode 签章编码
     * @return
     */
    Map<String, Object> getContractConfirmData(String signCode);

    /**
     * 校验业务所需个人信息是否完整.
     *
     * @param tag          标签
     * @param businessType 业务类型
     * @param params       请求参数
     * @return Map
     */
    Map checkIfMsgComplete(String tag, String businessType, Map<String, Object> params) throws Exception;

    /**
     * 分别调用订单与收单系统的删除待提交订单相关银行卡信息接口。
     * 调用接口前，不进行订单存在性校验。
     *
     * @param cardNo
     */
    void updateDeleteCardToEmpty(String cardNo);

    /**
     * 从CRM获取userId
     *
     * @param custName
     * @param idNo
     * @return
     */
    String getUserIdByCustNameAndIdNo(String custName, String idNo);


    Map<String, Object> getDateAppOrderPerson(String crtUsr, String idNo, Integer page, Integer size, String channel);

    Map<String, Object> getDateAppOrderNew(Map<String, Object> map);

    /**
     * 订单系统批量查询订单状态
     *
     * @param url         请求地址
     * @param requestJson 请求内容
     */
    Map<String, Object> batchQueryOrderState(String url, String requestJson);

    /***
     * 待还款金额查询(月度、近七日)
     *
     * @param idNo
     * @param flag
     * @return
     */
    Map<String, Object> queryApplAmountByIdNo(String idNo, String flag);

    /**
     * 校验身份证是否有效
     *
     * @param params
     * @return
     */
    Map checkIfCertValid(Map<String, Object> params);

    /**
     * 校验单笔还款金额是否超过银行卡单笔扣款限额.
     *
     * @param order 顶部孤单信息
     * @return boolean
     */
    boolean ifAccessEd(AppOrder order);

    /**
     * 根据用户编号获取白名单以及渠道号.
     *
     * @param custNo
     * @param appOrder
     * @return
     */
    Map<String, Object> getChannelNoAndWhiteType(String custNo, AppOrder appOrder);

    /**
     * 商户版创建订单
     *
     * @param appOrder
     * @return
     */
    Map<String, Object> saveMerchAppOrder(AppOrder appOrder);

    /**
     * 个人版创建订单.
     *
     * @param appOrder
     * @return
     */
    Map<String, Object> saveAppOrderInfo(AppOrder appOrder);


    /**
     * 将订单中数据影射为收单系统渠道进件格式map.
     * 不包括list(联系人、申请人信息、商品信息)相关数据.
     *
     * @param order 订单信息
     * @param map   映射结果
     * @return Map
     */
    Map<String, Object> order2AcquirerMap(AppOrder order, Map<String, Object> map);

    /**
     * 根据订单信息获取系统标识和渠道号.
     *
     * @param appOrder 订单信息：custName&idNo属性必须存在才可调用
     * @return
     */
    Map<String, Object> getSysFlagAndChannelNo(AppOrder appOrder);

    /**
     * 提交订单.
     *
     * @param orderNo      订单编号
     * @param applSeq      流水号
     * @param opType       提交类型：1-提交到信贷系统；2-提交给商户（个人版扫码分期用）
     * @param msgCode      验证码
     * @param expectCredit 期望额度
     * @param typGrp       01：耐用消费品   02:一般消费品
     * @return
     */
    Map<String, Object> commitAppOrder(String orderNo, String applSeq, String opType,
                                       String msgCode, String expectCredit, String typGrp);

    /**
     * 取消订单.
     *
     * @param orderNo 订单编号
     * @return
     */
    Map<String, Object> cancelAppOrder(String orderNo);

    /**
     * 根据订单编号获取订单和商品列表.
     *
     * @param orderNo 订单编号
     * @return Map
     */
    Map<String, Object> getAppOrderAndGoods(String orderNo);

    /**
     * 申请退货接口
     *
     * @param params applSeq 申请流水号
     *               returnReason 退货原因
     * @return
     */
    Map<String, Object> returnGoods(Map<String, Object> params);

    /**
     * 退回订单给商户.
     *
     * @param orderNo 订单编号
     * @param reason  退回原因
     * @return
     */
    Map<String, Object> backOrderToCust(String orderNo, String reason);


    /**
     * 按照状态排列
     *
     * @param list
     */
    void sortLoanList(List list);


    /**
     * 待发货列表查询
     *
     * @param map
     * @return
     * @throws Exception
     */
    public List<Map<String, Object>> queryWaitSendOrder(Map<String, Object> map) throws Exception;

    /**
     * 清空订单中的商品信息
     * @param appOrder
     */
    void cleanGoodsInfo(AppOrder appOrder);
}
