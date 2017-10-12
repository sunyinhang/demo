package com.haiercash.payplatform.pc.cashloan.service;

import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

/**
 * Created by 许崇雷 on 2017-10-10.
 */
public interface CashLoanService {
    ModelAndView getActivityUrl( );

    Map<String, Object> joinActivity();
}
