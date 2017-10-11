package com.haiercash.payplatform.pc.qiaorong.service;

import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * Created by yuanli on 2017/9/12.
 */
public interface QiaorongService {
    /*
    合同初始化
     */
    Map<String, Object> contractInit(Map<String, Object> map);

    /*
    四要素验证
     */
    Map<String, Object> checkFourKeys(Map<String, Object> map);

    /*
    注册
     */
    Map<String, Object> register(Map<String, Object> map);

    /*
    是否需要魔蝎验证
     */
    Map<String, Object> isNeedMoxie(Map<String, Object> map);

    /*
    根据流水号查询魔蝎验证
     */
    Map<String, Object> getMoxieByApplseq(Map<String, Object> map);

    /*
    查询往魔蝎传送的信息
     */
    Map<String, Object> getMoxieInfo(Map<String, Object> map);

    /*
    合同签订
     */
    Map<String, Object> loanContract(Map<String, Object> map);

    /*
    CA签章
     */
    Map<String, Object> cacontract(Map<String, Object> map) throws Exception;
}
