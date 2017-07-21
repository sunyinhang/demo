package com.haiercash.appserver.util.sign;

/**
 * Sign Type Enum.
 * @author Liu qingxiang
 * @since 1.1.0
 * @see SignType
 */
public enum SignType {
    /**
     * 注册.
     */
    register,

    /**
     * 征信查询.
     */
    credit,

    /**
     * 共同还款人协议.
     */
    common,

    /**
     * 银行卡变更授权书.
     */
    grant,

    /**
     * 提额征信授权书.
     */
    risEdCredit,

    /**
     * 百城授权承诺函
     */
    bcGrant,

    /**
     * 优客征信协议授权书.
     */
    ZXXYUOKO,

    /**
     * 优客合同签章.
     */
    LOANUOKO,
    /**
     * 豆子.
     */
    DOUZIPERSONAL,
    /**
     * 豆子商户版.
     */
    DOUZIBUSINESS,
    /***
     *互动金融
     */
    HDJR_JKHT
}
