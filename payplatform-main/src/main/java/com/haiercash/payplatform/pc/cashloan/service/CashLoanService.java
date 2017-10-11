package com.haiercash.payplatform.pc.cashloan.service;

import java.util.Map;

/**
 * Created by 许崇雷 on 2017-10-10.
 */
public interface CashLoanService {
    Map<String, Object>  getActivityUrl( );

    Map<String, Object> joinActivity(Map<String, Object> params);
}
