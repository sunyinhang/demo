package com.haiercash.payplatform.common.utils;

/**
 * 收单系统交易码
 * Created by zhouwushuang on 2017.04.14.
 * since 1.6.3
 */
public class AcqTradeCode {
    /**
     * 2.1.1	(POST)ACQ-1101 新增贷款申请
     */
    public static final String SAVE_APPL = "ACQ-1101";

    /**
     * 2.1.2	(POST) ACQ-1105保存共同还款人
     */
    public static final String SAVE_APPT = "ACQ-1105";

    /**
     * 2.1.3	(POST) ACQ-1106删除共同还款人
     */
    public static final String DELETE_APPT = "ACQ-1106";

    /**
     * 2.1.4	(POST) ACQ-1131贷款申请提交
     */
    public static final String COMMIT_APPL = "ACQ-1131";

    /**
     * 2.1.5	(POST) ACQ-1133贷款申请取消
     */
    public static final String CANCEL_APPL = "ACQ-1133";

    /**
     * 2.1.6	(POST) ACQ-1141贷款列表查询
     */
    public static final String SELECT_APPL_LIST = "ACQ-1141";

    /**
     * 2.1.7	(POST) ACQ-1142被退回申请查询
     */
    public static final String SELECT_BACK_APPL = "ACQ-1142";

    /**
     * 2.1.8	(POST) ACQ-1143贷款详情查询
     */
    public static final String SELECT_APPL_INFO = "ACQ-1143";

    /**
     * 2.1.9	(POST) ACQ-1144贷款数量查询
     */
    public static final String SELECT_APPL_COUNT = "ACQ-1144";

    /**
     * 2.1.10   (POST) ACQ-1145 APP贷款详情查询(渠道进件格式)
     */
    public static final String SELECT_APP_APPL_INFO = "ACQ-1145";

    /**
     * (POST) ACQ-1147 查询共同还款人信息
     */
    public static final String SELECT_COMMON_PERSON = "ACQ-1147";

    /**
     * (POST) ACQ-1150删除待提交银行卡信息.
     */
    public static final String DELETE_UNSUBMIT_CARD_INFO = "ACQ-1150";

    /**
     * 2.1.11	(GET) ACQ-1151审批进度查询
     */
    public static final String SELECT_APPL_PROCESS = "ACQ-1151";

    /**
     * 2.1.20	（ACQ-1151）根据申请流水号列表批量查询贷款状态
     */
    public static final String BATCH_QUERY_APPL_STATE = "ACQ-1151";

    /**
     * 2.1.12	(POST)ACQ-1201消息推送
     */
    public static final String PULL_MESSAGE = "ACQ-1201";

    /**
     * 2.1.13	（POST）ACQ-1202贷款申请已同步
     */
    public static final String SELECT_APPL_SYNC_STS = "ACQ-1202";

    /**
     * (POST) 商户退回.
     */
    public static final String BACK_ORDER_TO_CUST = "ACQ-1205";

    /**
     * (POST) 提交还款请求
     */
    public static final String SAVE_REPAY_INFO = "ACQ-2101";

}
