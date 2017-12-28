package com.haiercash.payplatform.common.enums;

/**
 * acquirer goods and order goods relation enum.
 * @author Liu qingxiang.
 * @since v1.0
 */
public enum AcquirerGoodsEnum {

    GOODS_CODE      ("goodsCode",   "goods_code"),  // 商品编号
    GOODS_BRAND     ("goodsBrand",  "goods_brand"), // 产品/服务品牌
    GOODS_KIND      ("goodsKind",   "goods_kind"),  // 产品/服务类型
    GOODS_NAME      ("goodsName",   "goods_name"),  // 商品名称
    GOODS_MODEL     ("goodsModel",  "goods_model"), // 商品型号
    GOODS_NUM       ("goodsNum",    "goods_num"),   // 商品数量
    GOODS_PRICE     ("goodsPrice",  "goods_price"); // 商品单价(元)

    private final String orderAttr;
    private final String acquirerAttr;
    AcquirerGoodsEnum(String orderAttr, String acquirerAttr) {
        this.orderAttr = orderAttr;
        this.acquirerAttr = acquirerAttr;
    }

    public static String getOrderAttr(String acquirerAttr) {
           for (AcquirerGoodsEnum acquirerEnum : AcquirerGoodsEnum.values()) {
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
