package com.haiercash.payplatform.common.enums;

import java.util.ArrayList;
import java.util.List;

/**
 * app后台order与订单系统order映射枚举.
 * @author Liu qingxiang
 * @since v1.0
 */
public enum OrderEnum {
    VERSION         ("version",         "version"),     // 版本
    FORMID          ("orderNo",         "formId"),      // 订单id
    FORMTYPE        ("formType",        "formTyp"),     // 订单类型
    CUSTNO          ("custNo",          "custNo"),      // 用户编号
    CUSTNAME        ("custName",        "custName"),    // 用户姓名
    IDTYP           ("idTyp",           "idTyp"),       // 客户证件类型
    IDNO            ("idNo",            "idNo"),        // 客户证件号码
    COOPRCDE        ("cooprCde",        "cooprCde"),    // 门店编号
    SALERCDE        ("salerCde",        "salerCde"),    // 销售代表编码
    CRTUSR          ("crtUsr",          "crtUsr"),      // 录单用户ID
    APPLSEQ         ("applSeq",         "applSeq"),     // 流水号
    TYPGRP          ("typGrp",          "typGrp"),      // 贷款类型编号
    PURPOSE         ("purpose",         "purpose"),     // 贷款用途编号
    TYPCDE          ("typCde",          "typCde"),      // 贷款品种代码
    FSTPAY          ("fstPay",          "fstPay"),      // 首付金额
    APPLYTNR        ("applyTnr",        "applyTnr"),    // 借款期限
    APPLYTNRTYP     ("applyTnrTyp",     "applyTnrTyp"), // 借款期限类型
    TOTALNORMINT    ("totalnormint",    "totalNorMint"),// 总利息金额
    TOOTALFEEAMT    ("totalfeeamt",     "totalFeeAmt"), // 费用总额
    APP_IN_ADVICE   ("appInAdvice",     "appInAdvice"), // 贷款备注
    APPLACTYP       ("applAcTyp",       "applAcTyp"),   // 放款账号类型
    APPLCARDNO      ("applCardNo",      "applCardNo"),  // 放款卡号
    REPAYAPPLCARDNO ("repayApplCardNo", "repayApplCardNo"), // 还款卡号
    APPLINADVICE    ("applInAdvice",    "applInAdvice"),    //  贷款备注
    GOODSCODE       ("goodsCode",       "goodsCode"),   // 商品代码
    GOODSNUM        ("goodsNum",        "goodsNum"),    // 数量
    GOODSPRICE      ("goodsPrice",      "goodsPrice"),  // 商品价格
    GOODSNAME       ("goodsName",       "goodsName"),   // 商品名称
    GOODSBRAND      ("goodsBrand",      "brandName"),   // 商品品牌
    GOODSKIND       ("goodsKind",       "kindName"),    // 商品类型
    GOODSMODEL      ("goodsModel",      "goodsModel"),  // 商品型号
    DELIVERTYP      ("deliverAddrTyp",  "adType"),      // 送货地址类型
    ADPROVINCE      ("deliverProvince", "adProvince"),  // 送货地址省
    ADCITY          ("deliverCity",     "adCity"),      // 送货地址市
    ADAREA          ("deliverArea",     "adArea"),      // 送货地址区
    ADADDR          ("deliverAddr",     "adAddr"),      // 送货详细地址
    ADNAME          ("custName",        "adName"),      // 收货人名称
    ADPHONE         ("indivMobile",     "adPhone"),     // 收货人联系方式
    CHANNEL         ("source",          "sysNo"),       // 系统标识
    CHANNEL_NO      ("channelNo",       "channelNo"),   // 渠道号
    GOODSLIST       ("appOrderGoodsList","goodsList");   // 商品信息列表

    private String appOrderAttr;
    private String orderAttr;

    OrderEnum(String appOrderAttr, String orderAttr) {
        this.appOrderAttr = appOrderAttr;
        this.orderAttr = orderAttr;
    }

    public static List<String> getOrderAttrs(String appOrderAttr) {
        List<String> result = new ArrayList<>();
        for (OrderEnum orderEnum : OrderEnum.values()) {
            if (orderEnum.getAppOrderAttr().equals(appOrderAttr)) {
                result.add(orderEnum.getOrderAttr());
            }
        }
        if (result.isEmpty()) {
            return null;
        } else {
            return result;
        }
    }

    public String getAppOrderAttr() {
        return appOrderAttr;
    }

    public String getOrderAttr() {
        return orderAttr;
    }
}
