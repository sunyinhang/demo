package com.haiercash.payplatform.rest.cmisacq;

import lombok.Data;

/**
 * Created by 许崇雷 on 2017-10-09.
 */
@Data
public class CmisAcqResponseRoot<TBody> {
    CmisAcqResponseHead head;
    TBody body;
}
