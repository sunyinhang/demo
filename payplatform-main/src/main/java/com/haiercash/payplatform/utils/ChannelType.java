package com.haiercash.payplatform.utils;

import com.haiercash.core.lang.Convert;
import org.apache.commons.lang.StringUtils;

/**
 * 渠道类型
 */
public enum ChannelType {
    /**
     * 其他/未知
     */
    Unknown(null, null, null),
    /**
     * 商户版
     */
    Merchant("13", null, "1"),
    /**
     * 个人版
     */
    Personal("14", null, "2"),
    /**
     * 其他支付平台
     */
    OtherPayPlat(null, null, "3"),
    /**
     * 美凯龙/星巢贷
     */
    Micron("16", "31", null),
    /**
     * 大数据
     */
    BigData(null, "34", null),
    /**
     * 美分期
     */
    LoveByStage(null, "35", null),
    /**
     * 顺逛H5
     */
    Shunguang(null, "46", null),
    /**
     * 现金贷
     */
    CashLoan(null, "49", null),
    /**
     * 够花
     */
    EnoughSpend("18", "42", null);

    private String channel;
    private String channelNo;
    private String source;

    /**
     * 构造函数
     *
     * @param channel
     * @param channelNo
     * @param source
     */
    ChannelType(String channel, String channelNo, String source) {
        this.channel = channel;
        this.channelNo = channelNo;
        this.source = source;
    }

    /**
     * 获取渠道类型,优先级 channel>channelNo>source
     * source 1商户版 2个人版 3支付平台
     *
     * @param channel   sysFlag
     * @param channelNo 渠道号
     * @param source    旧版本标记(如果source 为两位并且channel 为空则将
     * @return
     */
    public static ChannelType forName(String channel, String channelNo, String source) {
        //根据 channel 判断
        if (StringUtils.isNotEmpty(channel)) {
            for (ChannelType channelType : ChannelType.values()) {
                if (StringUtils.equals(channelType.channel, channel))
                    return channelType;
            }
        }
        //根据 channelNo 判断
        if (StringUtils.isNotEmpty(channelNo)) {
            for (ChannelType channelType : ChannelType.values()) {
                if (StringUtils.equals(channelType.channelNo, channelNo))
                    return channelType;
            }
        }
        //根据 source 判断
        switch (Convert.toString(source).length()) {
            case 1:
                for (ChannelType channelType : ChannelType.values()) {
                    if (StringUtils.equals(channelType.source, source))
                        return channelType;
                }
                break;
            case 2:
                for (ChannelType channelType : ChannelType.values()) {
                    if (StringUtils.equals(channelType.channel, source))
                        return channelType;
                }
                break;
            default:
                break;
        }

        //默认
        return Unknown;
    }
}
