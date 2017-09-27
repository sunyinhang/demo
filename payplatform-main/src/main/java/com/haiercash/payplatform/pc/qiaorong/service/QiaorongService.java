package com.haiercash.payplatform.pc.qiaorong.service;

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
}
