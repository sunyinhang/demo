package com.haiercash.payplatform.common.entity;

import com.haiercash.payplatform.common.data.AppOrder;

import java.util.Map;

public class AcquirerData {
    private Map<String, Object> acquirerMap;
    private AppOrder appOrder;

    public AcquirerData() {

    }

    public AcquirerData(Map<String, Object> acquirerMap, AppOrder appOrder) {
        this.acquirerMap = acquirerMap;
        this.appOrder = appOrder;
    }

    public Map<String, Object> getAcquirerMap() {
        return acquirerMap;
    }

    public void setAcquirerMap(Map<String, Object> acquirerMap) {
        this.acquirerMap = acquirerMap;
    }

    public AppOrder getAppOrder() {
        return appOrder;
    }

    public void setAppOrder(AppOrder appOrder) {
        this.appOrder = appOrder;
    }
}
