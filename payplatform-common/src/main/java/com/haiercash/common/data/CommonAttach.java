package com.haiercash.common.data;


import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="APP_COMMON_ATTACH")
public class CommonAttach {
    @Id
    private String docCde; //影像代码
    private String docDesc; //影像名称
    private String docRevInd;//收取要求  01：必收、02：可收、03：免收、04：补件

    public String getDocCde() {
        return docCde;
    }

    public void setDocCde(String docCde) {
        this.docCde = docCde;
    }

    public String getDocDesc() {
        return docDesc;
    }

    public void setDocDesc(String docDesc) {
        this.docDesc = docDesc;
    }

    public String getDocRevInd() {
        return docRevInd;
    }

    public void setDocRevInd(String docRevInd) {
        this.docRevInd = docRevInd;
    }
}
