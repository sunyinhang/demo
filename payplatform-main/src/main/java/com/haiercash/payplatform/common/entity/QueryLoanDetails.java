/*
 * 功  能：简单说明该类的功能
 *
 * 文件名：QueryLoanDetail.java
 *
 * 描  述：
 *
 * [变更履历]
 * Version   变更日         部署              作者           变更内容
 * -----------------------------------------------------------------------------
 * V1.00     2015年12月29日   Haiercash    suyang      CREATE
 * -----------------------------------------------------------------------------
 *
 *
 * Copyright (c) 2015 Haiercash All Rights Reserved.
 *┌─────────────────────────────────────────────────—────┐
 *│ 版权声明                               Haiercash       │
 *└──────────────────────────────—————————————————————───┘
 */

package com.haiercash.payplatform.common.entity;

import lombok.Data;

/**
 * DESCRIPTION:
 * <p>
 * <p>
 * <a href="QueryLoanDetail.java"><i>View Source</i></a>
 * </p>
 *
 * @author <a href="mailto:suyang@haiercash.com">suyang</a>
 * @version Revision: 1.0  Date: 2015年12月29日 上午10:06:53
 */
@Data
public class QueryLoanDetails {
    private String applSeq;

    public QueryLoanDetails() {
        this.applSeq = "";
    }
}
