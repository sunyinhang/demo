package com.haiercash.common.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Cacheable;
import java.util.List;

@Cacheable(false)
public interface AppOrderRepository extends JpaRepository<AppOrder, String> {

    /**
     * 根据商户编号，查询所有订单信息
     */
    List<AppOrder> findAppOrderByCrtUsr(String crtUsr);

    @Query(value = "select * from app_order where order_no = ?1", nativeQuery = true)
//    @Transactional
    AppOrder findByOrderNo(String orderNo);

    @Query(value="select * from app_order where REPAY_APPL_CARD_NO=?1 and STATUS<>4",nativeQuery = true)
    List<AppOrder> findOrdersByRepayApplCardNo(String cardNo);

    @Query(value = "select * from app_order where applseq = ?1 and rownum = 1 order by create_time desc", nativeQuery = true)
    AppOrder findByApplseq(String applseq);

    @Query(value = "select * from app_order where applseq = ?1 order by create_time desc", nativeQuery = true)

    List<AppOrder> findContractByApplseq(String applseq);

    @Query(value = "select * from app_order where order_no = ?1 order by create_time desc ", nativeQuery = true)
    List<AppOrder> findContractByOrderNo(String orderNo);

    @Query(value = "select * from app_order where cust_no = ?1 and applseq = ?2 order by create_time desc", nativeQuery = true)
    List<AppOrder> findByCustNoAndApplseq(String custNo, String applseq);

    @Modifying
    @Query(value = "delete from app_order where applseq= ?1", nativeQuery = true)
    @Transactional
    void deleteByApplseq(String applseq);

    /**
     * 修改订单：
     * 订单号(orderNo)、贷款品种(typCde)、借款期限(applyTnr)、商品总额(proPurAmt)、首付金额(fstPay)、
     * 送货地址类型(deliverAddrTyp)、送货地址(deliverAddr)
     */
    @Modifying
    @Query(value = "update App_Order t  set t.typ_cde=?2 , t.apply_tnr=?3 , t.pro_pur_amt=?4,t.fst_pay=?5,t.deliver_addr_typ=?6,t.deliver_addr=?7 where t.order_no=?1", nativeQuery = true)
    @Transactional
    void updateAppOrder(String orderNo, String typCde, String applyTnr, String proPurAmt, String fstPay,
                        String deliverAddrTyp, String deliverAddr);

    /**
     * 修改订单人脸识别信息
     *
     * @param orderNo
     * @param faceTypCde        人脸识别贷款品种
     * @param faceValue         贷款品种人脸分值
     * @param applyFaceSucc     申请人人脸识别是否成功
     * @param applyFaceCount    申请人人脸识别次数
     * @param applyFaceValue    申请人人脸识别分值
     * @param commonCustNo      共同还款人客户编号
     * @param comApplyFaceSucc  共同申请人人脸识别是否成功
     * @param comApplyFaceCount 共同申请人人脸识别次数
     * @param comApplyFaceValue 共同申请人人脸识别分值
     */
    @Modifying
    @Query(value = "update App_Order t  set t.face_typ_cde=?2, t.face_value=?3, t.apply_face_succ=?4, t.apply_face_count=?5, t.apply_face_value=?6, t.common_cust_no=?7, t.com_apply_face_succ=?8, t.com_apply_face_count=?9, t.com_apply_face_value=?10 where t.order_no=?1", nativeQuery = true)
    @Transactional
    int updateAppOrderFaceInfo(String orderNo, String faceTypCde, String faceValue, String applyFaceSucc, String applyFaceCount, String applyFaceValue,
                               String commonCustNo, String comApplyFaceSucc, String comApplyFaceCount, String comApplyFaceValue);

    /**
     * 设置合同确认状态为：1-已确认
     *
     * @param orderNo
     */
    @Modifying
    @Query(value = "update App_Order t  set t.is_confirm_contract='1'  where t.order_no=?1", nativeQuery = true)
    @Transactional
    void updateOrderContract(String orderNo);

    /**
     * 设置协议确认状态为：1-已确认，同时将订单状态置为：1-待处理
     *
     * @param orderNo
     */
    @Modifying
    @Query(value = "update App_Order t  set t.is_confirm_agreement='1', t.status='1',t.applseq=?2,t.apply_dt=?3,t.appl_cde=?4  where t.order_no=?1", nativeQuery = true)
    @Transactional
    int updateOrderAgreement(String orderNo, String applSeq, String applyDt, String applCde);

    /**
     * 退回订单只客户处
     *
     * @param orderNo 修改订单状态为：3-被退回
     */
    @Modifying
    @Query(value = "update App_Order t  set t.status='3',t.back_reason=?2  where t.order_no=?1", nativeQuery = true)
    @Transactional
    void backOrderToCust(String orderNo, String reason);

    /**
     * 客户提交订单
     *
     * @param orderNo 修改订单状态为：2-待确认
     */
    @Modifying
    @Query(value = "update App_Order t  set t.status='2'  where t.order_no=?1", nativeQuery = true)
    @Transactional
    void submitOrderToMerch(String orderNo);

    /**
     * 更新商品的总额
     *
     * @param orderNo 订单号
     * @param spzj    商品的总额
     */
    @Modifying
    @Query(value = "update App_Order t  set t.pro_Pur_Amt=?2  where t.order_no=?1", nativeQuery = true)
    @Transactional
    void updateAppOrderByProPurAmt(String orderNo, String spzj);

    /**
     * 设置还款卡号
     *
     * @param orderNo  订单号
     * @param bankNo   还款银行卡号
     * @param bankName 还款银行名
     */
    @Modifying
    @Query(value = "update App_Order t  set t.repay_appl_card_no=?2,t.repay_acc_bank_name=?3,t.repay_acc_bank_cde=?4,t.repay_acc_bch_cde=?5,t.repay_acc_bch_name=?6,t.repay_ac_province=?7,t.repay_ac_city=?8  where t.order_no=?1", nativeQuery = true)
    @Transactional
    void updateRepayApplCardInfo(String orderNo, String bankNo, String bankName, String bankCode, String bchCode,
                                 String bchName, String acctProvince, String acctCity);

    /**
     * 设置放款卡号
     *
     * @param orderNo 订单号
     * @param bankNo  放款银行卡号
     * @parm bankName 放款银行名称
     */
    @Modifying
    @Query(value = "update App_Order t  set t.appl_card_no=?2,t.acc_bank_name=?3,t.acc_bank_cde=?4,t.acc_ac_bch_cde=?5,t.acc_ac_bch_name=?6  where t.order_no=?1", nativeQuery = true)
    @Transactional
    void updateAccBankInfo(String orderNo, String bankNo, String bankName, String bankCode, String bchCode,
                           String bchName);

    /**
     * 更新个人资料完善情况
     *
     * @param orderNo 订单号
     * @param flag    更新标志位
     * @parm
     */
    @Modifying
    @Query(value = "update App_Order t  set t.is_Cust_Info_Completed=?2  where t.order_no=?1", nativeQuery = true)
    @Transactional
    void updateIsCustInfoCompleted(String orderNo, String flag);

    /**
     * 更新客户手机号（个人版用）
     *
     * @param orderNo
     * @param mobile
     */
    @Modifying
    @Query(value = "update App_Order t  set t.indiv_mobile=?2  where t.order_no=?1", nativeQuery = true)
    @Transactional
    void updateOrderMobile(String orderNo, String mobile);


    /**
     * 查询指定的影像是否已全部上传
     * 调用示例：Integer count = appOrderRepository.queryAttachCount("1143801", "DOC024,DOC027,DOC050,");
     * 返回值count如果小于传入的影像代码数量，代表上传不完整
     *
     * @param applSeq
     * @param attachTypes
     * @return
     */
    @Query(value = "select count(*) from (select attach_type, count(*) from app_attach_file where appl_seq = ?1 and ?2 like '%' || attach_type || ',%' and common_cust_no is null group by attach_type)", nativeQuery = true)
    Integer queryAttachCount(String applSeq, String attachTypes);

    /**
     * 查询指定的影像是否已全部上传(个人版)
     *
     * @param custNo
     * @param attachTypes
     * @return
     */
    @Query(value = "select count(*) from (select attach_type, count(*) from app_attach_file where cust_no = ?1 and ?2 like '%' || attach_type || ',%' and common_cust_no is null group by attach_type)", nativeQuery = true)
    Integer queryAttachCountPerson(String custNo, String attachTypes);

    @Query(value = "select * from app_attach_file where cust_no = ?1 and ?2 like '%' || attach_type || ',%' and common_cust_no is null", nativeQuery = true)
    List queryAttachCountPerson2(String custNo, String attachTypes);
    /**
     * 查询指定的影像是否已全部上传(共同还款人)
     * 调用示例：Integer count = appOrderRepository.queryAttachCount("1143801", "DOC024,DOC027,DOC050,"C201610300214053219200");
     * 返回值count如果小于传入的影像代码数量，代表上传不完整
     *
     * @param appSeq
     * @param attachTypes
     * @param commonCustNo
     * @return
     */
    @Query(value = "select count(*) from (select attach_type, count(*) from app_attach_file where appl_seq = ?1 and ?2 like '%' || attach_type || ',%' and common_cust_no =?3 group by attach_type)", nativeQuery = true)
    Integer queryCommonAttachCount(String appSeq, String attachTypes, String commonCustNo);

    @Query(value = "select * from app_order where applseq = ?1  order by create_time desc", nativeQuery = true)
    List<AppOrder> findAllByApplseq(String applseq);

    @Query(value="update App_Order o SET o.REPAY_APPL_CARD_NO = '',o.REPAY_ACC_BANK_CDE = '',o.REPAY_ACC_BANK_NAME = '',o.REPAY_AC_PROVINCE = '',o.REPAY_AC_CITY = '',o.REPAY_ACC_BCH_CDE = '',o.REPAY_ACC_BCH_NAME = '' WHERE o.REPAY_APPL_CARD_NO = ?1 and o.STATUS<>4",nativeQuery = true)
    @Modifying
    @Transactional
    Integer deleteRepayMessageByCardNo(String cardNo);


    @Modifying
    @Query(value = "update AppOrder t  set t.expectCredit=?1  where t.orderNo=?2")
    @Transactional(propagation = Propagation.REQUIRED)
    int updateExpectCredit(String expectCredit,String orderNo);
}
