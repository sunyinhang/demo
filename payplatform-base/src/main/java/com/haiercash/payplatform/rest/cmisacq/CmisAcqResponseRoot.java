package com.haiercash.payplatform.rest.cmisacq;

import lombok.Data;

/**
 * Created by 许崇雷 on 2017-10-09.
 */
@Data
public final class CmisAcqResponseRoot<TBody> {
    private CmisAcqResponseHead head;
    private TBody body;
}
