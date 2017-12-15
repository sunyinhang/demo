package com.haiercash.payplatform.common.data;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by yuanli on 2017/12/13.
 */
@Data
@Entity
@Table(name = "S_AREA")
public class SArea {
    @Id
    private String areaCode;//编码
    private String areaName;//名称
    private String areaParentCode;//上级编码
    private String areaType;//类型（province   city    area）
}
