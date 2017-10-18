package com.haiercash.payplatform.pc.moxie.service;

import java.util.Map;

/**
 * Created by yuanli on 2017/10/9.
 */
public interface MoxieService {
    void moxie(String body, String flag);
    Map getMoxieByApplseq(String applseq);
}
