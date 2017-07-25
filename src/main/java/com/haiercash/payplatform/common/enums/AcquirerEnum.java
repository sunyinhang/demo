package com.haiercash.payplatform.common.enums;

/**
 * acquirer enum, 映射收单与订单属性字段.
 * @author Liu qingxiang
 * @since v2.0.0
 */
public enum AcquirerEnum {

    ORDER_NO            ("orderNo",         "formId"),          // 订单编号
    ID_TYPE             ("idTyp",           "id_typ"),          // 证件类型
    SOURCE              ("source",          "channel"),         // 系统标识
    CHANNEL_NO          ("channelNo",       "channel_no"),      // 渠道号
    APPLSEQ             ("applSeq",         "appl_seq"),        // 流水号
    STATUS              ("status",          "outSts"),          // 订单状态
    ID_NO               ("idNo",            "id_no"),           // 证件号码
    CUST_NAME           ("custName",        "cust_name"),       // 申请人姓名
    COOPR_NAME          ("cooprName",       "coopr_name"),      // 门店名称
    COOPR_CDE           ("cooprCde",        "coopr_cde"),       // 门店代码
    CONT_ZONE           ("contZone",        "cont_zone"),       // 门店联系电话区号
    CONT_TEL            ("contTel",         "cont_tel"),        // 门店联系电话
    CONT_SUB            ("contSub",         "cont_sub"),        // 门店联系电话分机
    TYP_GRP             ("typGrp",          "typ_grp"),         // 贷款类型
    PROM_CDE            ("promCde",         "prom_cde"),        // 营销专案代码
    PROM_DESC           ("promDesc",        "prom_desc"),       // 营销专案名称
    TYP_SEQ             ("typSeq",          "typ_seq"),         // 贷款品种流水号
    TYP_CDE             ("typCde",          "typ_cde"),         // 贷款品种代码
    TYP_DESC            ("typDesc",         "typ_desc"),        // 贷款品种名称
    APPLY_DT            ("applyDt",         "apply_dt"),        // 申请日期
    CRT_USR             ("crtUsr",          "crt_usr"),         // 登记人代码
    INDIV_MOBIE         ("indivMobile",     "indiv_mobile"),    // 移动电话
    PRO_PUR_AMT         ("proPurAmt",       "pro_pur_amt"),     // 购买总额
    FST_PCT             ("fstPct",          "fst_pct"),         // 首付比例
    FST_PAY             ("fstPay",          "fst_pay"),         // 首付金额
    APPLY_AMT           ("applyAmt",        "apply_amt"),       // 申请金额（元）
    APPLY_TNR           ("applyTnr",        "apply_tnr"),       // 申请期限
    APPLY_TNR_TYP       ("applyTnrTyp",     "apply_tnr_typ"),    // 期限类型
    PURPOSE             ("purpose",         "purpose"),         // 贷款用途
    OTHER_PURPOSE       ("otherPurpose",    "other_purpose"),   // 其他用途
    MONTH_REPAY         ("monthRepay",      "month_repay"),     // 可接受月还款
    OPER_GOODS_TYP      ("operGoodsTyp",    "oper_goods_typ"),  // 采购产品种类
    PAY_MTD             ("payMtd",          "pay_mtd"),         // 还款方式种类代码
    MTD_CDE             ("mtdCde",          "mtd_cde"),         // 还款方式代码
    MTD_DESC            ("payMtdDesc",      "pay_mtd_desc"),    // 还款方式详情
    LOAN_FREQ           ("loanFreq",        "loan_freq"),       // 还款间隔
    DUE_DAY_OPT         ("dueDayOpt",       "due_day_opt"),     // 每期还款日
    DUE_DAY             ("dueDay",          "due_day"),             // 还款日
    DOC_CHANNEL         ("docChannel",      "doc_channel"),         // 进件同路
    APPL_AC_TYP         ("applAcTyp",       "appl_ac_typ"),         // 账号类型
    APPL_AC_NAM         ("applAcNam",       "appl_ac_nam"),         // 账号户名
    APPL_CARD_NO        ("applCardNo",      "appl_card_no"),        // 卡号
    ACC_BANK_CDE        ("accBankCde",      "acc_bank_cde"),        // 开户银行代码
    ACC_BANK_NAME       ("accBankName",     "acc_bank_name"),       // 开户银行名
    AC_PROVINCE         ("accAcProvince",   "ac_province"),         // (放款账户)账户所在省
    AC_CITY             ("accAcCity",       "ac_city"),             // (放款账户）账户所在市
    APPL_AC_BCH         ("accAcBchCde",     "appl_ac_bch"),         // （放款账户）开户机构（12位联行号）
    REPAY_APPL_AC_NAM   ("repayApplAcNam",  "repay_appl_ac_nam"),   // 还款账号户名
    REPAY_APPL_CARD_NO  ("repayApplCardNo", "repay_appl_card_no"),  // 还款卡卡号
    REPAY_ACC_BANK_CDE  ("repayAccBankCde", "repay_acc_bank_cde"),  // 开户银行代码
    REPAY_ACC_BANK_NAME ("repayAccBankName","repay_acc_bank_name"), // 开户银行名
    REPAY_AC_PROVINCE   ("repayAcProvince", "repay_ac_province"),   // 账户所在省
    REPAY_AC_CITY       ("repayAcCity",     "repay_ac_city"),       // 账户所在市
    SALER_CDE           ("crtUsr",          "saler_cde"),           // 销售代表代码
    APPRV_AMT           ("apprvAmt",        "apprv_amt"),           // 审批金额
    SALER_NAME          ("salerName",       "saler_name"),          // 销售代表姓名
    SALER_MOBILE        ("salerMobile",     "saler_mobile"),        // 销售代表电话
    APP_IN_ADVICE       ("appInAdvice",     "app_in_advice"),       // 备注
    OPERATOR_NAME       ("operatorName",    "operator_name"),       // 客户经理名称
    OPERATOR_CDE        ("operatorCde",     "operator_cde"),        // 客户经理代码
    OPERATOR_TEL        ("operatorTel",     "operator_tel"),        // 客户经理电话
    GRT_COOPR_CDE       ("merchNo",         "super_coopr"),         // 商户编码
    EXPECTCREDIT        ("expectCredit",    "expectCredit"),        // 期望额度
    BACKREASON          ("backReason",      "app_out_advice"),      // 订单退回原因
    TOTAL_NOR_MINT      ("totalnormint",    "totalnormint"),        // 总利息金额
    TOTAL_FEE_AMT       ("totalfeeamt",     "totalfeeamt"),         // 总费用金额
    REPAY_APPL_AC_BCH_NAME ("repayAccBchName", "repay_appl_ac_bch_name"),  //还款账号开户机构名称
    REPAY_APPL_AC_BCH   ("repayAccBchCde",  "repay_appl_ac_bch"),          // 还款账号开户机构代码
    ACC_AC_BCH_NAME     ("accAcBchName",    "acc_ac_bch_name");            //放款卡支行名称


    private String orderAttr;
    private String acquirerAttr;

    AcquirerEnum(String orderName, String acquirerName) {
        this.orderAttr = orderName;
        this.acquirerAttr = acquirerName;
    }

    public static String getAcquirerAttr(String orderAttr){
        for (AcquirerEnum acquirerEnum : AcquirerEnum.values()) {
            if (acquirerEnum.getOrderAttr().equals(orderAttr)) {
                return acquirerEnum.getAcquirerAttr();
            }
        }
        return null;
    }


    public static String getOrderAttr(String acquirerAttr){
        for (AcquirerEnum acquirerEnum : AcquirerEnum.values()) {
            if (acquirerEnum.getAcquirerAttr().equals(acquirerAttr)) {
                return acquirerEnum.getOrderAttr();
            }
        }
        return null;
    }

    public String getOrderAttr() {
        return orderAttr;
    }

    public String getAcquirerAttr() {
        return acquirerAttr;
    }

}
