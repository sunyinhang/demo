package com.haiercash.common.data;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by zhouwushuang on 2017.04.15.
 */
@Repository
public interface AppOrdernoTypgrpRelationRepository extends CrudRepository<AppOrdernoTypgrpRelation, String> {

    @Query(value = "from AppOrdernoTypgrpRelation where applSeq = ?")
    AppOrdernoTypgrpRelation findByApplSeq(String applSeq);

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
    @Query(value = "update APP_ORDERNO_TYPGRP_RELATION t  set t.face_typ_cde=?2, t.face_value=?3, t.apply_face_succ=?4, t.apply_face_count=?5, t.apply_face_value=?6, t.common_cust_no=?7, t.com_apply_face_succ=?8, t.com_apply_face_count=?9, t.com_apply_face_value=?10 where t.order_no=?1", nativeQuery = true)
    @Transactional
    int updateAppOrderFaceInfo(String orderNo, String faceTypCde, String faceValue, String applyFaceSucc, String applyFaceCount, String applyFaceValue,
                               String commonCustNo, String comApplyFaceSucc, String comApplyFaceCount, String comApplyFaceValue);


    /**
     * 是否有指定申请流水号的relation记录
     * @param applSeq
     * @return
     */
    @Query(value = "SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM AppOrdernoTypgrpRelation a WHERE a.applSeq = ?1")
    boolean existsByApplSeq(String applSeq);
}
