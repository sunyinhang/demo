package com.haiercash.common.data;

import java.util.ArrayList;
import java.util.List;

/**
 * 描述：贷款申请实体bean类
 * 
 * @author 尹君
 *
 */
@Deprecated
public class DkInfoBean {
	private String autoFlag;// 是否自动提交
	private String flag;// 操作标识
	private String applSeq;// 申请流水号
	private String id_typ;// 证件类型
	private String id_no;// 证件号码
	private String cust_name;// 申请人姓名
	private String coopr_name;// 门店名称
	private String coopr_cde;// 门店代码
	private String cont_zone;// 门店联系电话区号
	private String cont_tel;// 门店联系电话
	private String cont_sub;// 门店联系电话分机
	private String typ_grp;// 贷款类型
	private String prom_cde;// 营销专案代码
	private String prom_desc;// 营销专案名称
	private String typ_ver;// 版本号
	private String typ_seq;// 贷款品种流水号
	private String typ_cde;// 贷款品种代码
	private String apply_dt;// 申请日期
	private String indiv_mobile;// 移动电话
	private Double pro_pur_amt;// 购买总额
	private Double fst_pct;// 首付比例
	private Double fst_pay;// 首付金额
	private Double apply_amt;// 申请金额（元）
	private String apply_tnr;// 申请期限
	private String apply_tnr_typ;// 期限类型
	private String purpose;// 贷款用途
	private String other_purpose;// 贷款其他用途
	private Double month_repay;// 可接受月还款（元）
	private String oper_goods_typ;// 采购产品种类
	private String mtd_cde;// 还款方式
	private String loan_freq;// 还款间隔
	private String due_day_opt;// 每期还款日
	private String due_day;// 还款日
	private String doc_channel;// 进件通路
	private String appl_ac_typ;// 放款账号类型
	private String appl_ac_nam;// 放款账号户名
	private String appl_card_no;// 放款卡号
	private String acc_bank_cde;// 放款开户银行代码
	private String acc_bank_name;// 放款开户银行名
	private String acc_ac_bch_cde;// 放款开户银行分支行代码
	private String acc_ac_bch_name;// 放款开户银行分支行名
	private String acc_ac_province;// 放款开户行所在省
	private String acc_ac_city;// 放款开户行所在市
	private String repay_appl_ac_nam;// 还款账号户名
	private String repay_appl_card_no;// 还款卡号
	private String repay_acc_bank_cde;// 还款开户银行代码
	private String repay_acc_bank_name;// 还款开户银行名
	private String repay_acc_bch_cde;// 还款开户银行分支行代码
	private String repay_acc_bch_name;// 还款开户银行分支行名
	private String repay_ac_province;// 还款账户所在省
	private String repay_ac_city;// 还款账户所在市
	private String crt_usr;// 销售代表代码
	private String saler_name;// 销售代表姓名
	private String saler_mobile;// 销售代表电话
	private String app_in_advice;// 备注
	private String operator_name;// 客户经理名称
	private String operator_cde;// 客户经理代码
	private String operator_tel;// 客户经理联系电话

	private List<GoodsBean> goodsList = new ArrayList<GoodsBean>();

	public List<GoodsBean> getGoodsList() {
		return goodsList;
	}

	public void setGoodsList(List<GoodsBean> goodsList) {
		this.goodsList = goodsList;
	}

	public String getAutoFlag() {
		return autoFlag;
	}

	public void setAutoFlag(String autoFlag) {
		this.autoFlag = autoFlag;
	}

	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}

	public String getApplSeq() {
		return applSeq;
	}

	public void setApplSeq(String applSeq) {
		this.applSeq = applSeq;
	}

	public String getId_typ() {
		return id_typ;
	}

	public void setId_typ(String id_typ) {
		this.id_typ = id_typ;
	}

	public String getId_no() {
		return id_no;
	}

	public void setId_no(String id_no) {
		this.id_no = id_no;
	}

	public String getCust_name() {
		return cust_name;
	}

	public void setCust_name(String cust_name) {
		this.cust_name = cust_name;
	}

	public String getCoopr_name() {
		return coopr_name;
	}

	public void setCoopr_name(String coopr_name) {
		this.coopr_name = coopr_name;
	}

	public String getCoopr_cde() {
		return coopr_cde;
	}

	public void setCoopr_cde(String coopr_cde) {
		this.coopr_cde = coopr_cde;
	}

	public String getCont_zone() {
		return cont_zone;
	}

	public void setCont_zone(String cont_zone) {
		this.cont_zone = cont_zone;
	}

	public String getCont_tel() {
		return cont_tel;
	}

	public void setCont_tel(String cont_tel) {
		this.cont_tel = cont_tel;
	}

	public String getCont_sub() {
		return cont_sub;
	}

	public void setCont_sub(String cont_sub) {
		this.cont_sub = cont_sub;
	}

	public String getTyp_grp() {
		return typ_grp;
	}

	public void setTyp_grp(String typ_grp) {
		this.typ_grp = typ_grp;
	}

	public String getProm_cde() {
		return prom_cde;
	}

	public void setProm_cde(String prom_cde) {
		this.prom_cde = prom_cde;
	}

	public String getProm_desc() {
		return prom_desc;
	}

	public void setProm_desc(String prom_desc) {
		this.prom_desc = prom_desc;
	}

	public String getTyp_ver() {
		return typ_ver;
	}

	public void setTyp_ver(String typ_ver) {
		this.typ_ver = typ_ver;
	}

	public String getTyp_seq() {
		return typ_seq;
	}

	public void setTyp_seq(String typ_seq) {
		this.typ_seq = typ_seq;
	}

	public String getTyp_cde() {
		return typ_cde;
	}

	public void setTyp_cde(String typ_cde) {
		this.typ_cde = typ_cde;
	}

	public String getApply_dt() {
		return apply_dt;
	}

	public void setApply_dt(String apply_dt) {
		this.apply_dt = apply_dt;
	}

	public String getIndiv_mobile() {
		return indiv_mobile;
	}

	public void setIndiv_mobile(String indiv_mobile) {
		this.indiv_mobile = indiv_mobile;
	}

	public Double getPro_pur_amt() {
		return pro_pur_amt;
	}

	public void setPro_pur_amt(Double pro_pur_amt) {
		this.pro_pur_amt = pro_pur_amt;
	}

	public Double getFst_pct() {
		return fst_pct;
	}

	public void setFst_pct(Double fst_pct) {
		this.fst_pct = fst_pct;
	}

	public Double getFst_pay() {
		return fst_pay;
	}

	public void setFst_pay(Double fst_pay) {
		this.fst_pay = fst_pay;
	}

	public Double getApply_amt() {
		return apply_amt;
	}

	public void setApply_amt(Double apply_amt) {
		this.apply_amt = apply_amt;
	}

	public String getApply_tnr() {
		return apply_tnr;
	}

	public void setApply_tnr(String apply_tnr) {
		this.apply_tnr = apply_tnr;
	}

	public String getApply_tnr_typ() {
		return apply_tnr_typ;
	}

	public void setApply_tnr_typ(String apply_tnr_typ) {
		this.apply_tnr_typ = apply_tnr_typ;
	}

	public String getPurpose() {
		return purpose;
	}

	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}

	public String getOther_purpose() {
		return other_purpose;
	}

	public void setOther_purpose(String other_purpose) {
		this.other_purpose = other_purpose;
	}

	public Double getMonth_repay() {
		return month_repay;
	}

	public void setMonth_repay(Double month_repay) {
		this.month_repay = month_repay;
	}

	public String getOper_goods_typ() {
		return oper_goods_typ;
	}

	public void setOper_goods_typ(String oper_goods_typ) {
		this.oper_goods_typ = oper_goods_typ;
	}

	public String getMtd_cde() {
		return mtd_cde;
	}

	public void setMtd_cde(String mtd_cde) {
		this.mtd_cde = mtd_cde;
	}

	public String getLoan_freq() {
		return loan_freq;
	}

	public void setLoan_freq(String loan_freq) {
		this.loan_freq = loan_freq;
	}

	public String getDue_day_opt() {
		return due_day_opt;
	}

	public void setDue_day_opt(String due_day_opt) {
		this.due_day_opt = due_day_opt;
	}

	public String getDue_day() {
		return due_day;
	}

	public void setDue_day(String due_day) {
		this.due_day = due_day;
	}

	public String getDoc_channel() {
		return doc_channel;
	}

	public void setDoc_channel(String doc_channel) {
		this.doc_channel = doc_channel;
	}

	public String getAppl_ac_typ() {
		return appl_ac_typ;
	}

	public void setAppl_ac_typ(String appl_ac_typ) {
		this.appl_ac_typ = appl_ac_typ;
	}

	public String getAppl_ac_nam() {
		return appl_ac_nam;
	}

	public void setAppl_ac_nam(String appl_ac_nam) {
		this.appl_ac_nam = appl_ac_nam;
	}

	public String getAppl_card_no() {
		return appl_card_no;
	}

	public void setAppl_card_no(String appl_card_no) {
		this.appl_card_no = appl_card_no;
	}

	public String getAcc_bank_cde() {
		return acc_bank_cde;
	}

	public void setAcc_bank_cde(String acc_bank_cde) {
		this.acc_bank_cde = acc_bank_cde;
	}

	public String getAcc_bank_name() {
		return acc_bank_name;
	}

	public void setAcc_bank_name(String acc_bank_name) {
		this.acc_bank_name = acc_bank_name;
	}

	public String getAcc_ac_bch_cde() {
		return acc_ac_bch_cde;
	}

	public void setAcc_ac_bch_cde(String acc_ac_bch_cde) {
		this.acc_ac_bch_cde = acc_ac_bch_cde;
	}

	public String getAcc_ac_bch_name() {
		return acc_ac_bch_name;
	}

	public void setAcc_ac_bch_name(String acc_ac_bch_name) {
		this.acc_ac_bch_name = acc_ac_bch_name;
	}

	public String getAcc_ac_province() {
		return acc_ac_province;
	}

	public void setAcc_ac_province(String acc_ac_province) {
		this.acc_ac_province = acc_ac_province;
	}

	public String getAcc_ac_city() {
		return acc_ac_city;
	}

	public void setAcc_ac_city(String acc_ac_city) {
		this.acc_ac_city = acc_ac_city;
	}

	public String getRepay_appl_ac_nam() {
		return repay_appl_ac_nam;
	}

	public void setRepay_appl_ac_nam(String repay_appl_ac_nam) {
		this.repay_appl_ac_nam = repay_appl_ac_nam;
	}

	public String getRepay_appl_card_no() {
		return repay_appl_card_no;
	}

	public void setRepay_appl_card_no(String repay_appl_card_no) {
		this.repay_appl_card_no = repay_appl_card_no;
	}

	public String getRepay_acc_bank_cde() {
		return repay_acc_bank_cde;
	}

	public void setRepay_acc_bank_cde(String repay_acc_bank_cde) {
		this.repay_acc_bank_cde = repay_acc_bank_cde;
	}

	public String getRepay_acc_bank_name() {
		return repay_acc_bank_name;
	}

	public void setRepay_acc_bank_name(String repay_acc_bank_name) {
		this.repay_acc_bank_name = repay_acc_bank_name;
	}

	public String getRepay_acc_bch_cde() {
		return repay_acc_bch_cde;
	}

	public void setRepay_acc_bch_cde(String repay_acc_bch_cde) {
		this.repay_acc_bch_cde = repay_acc_bch_cde;
	}

	public String getRepay_acc_bch_name() {
		return repay_acc_bch_name;
	}

	public void setRepay_acc_bch_name(String repay_acc_bch_name) {
		this.repay_acc_bch_name = repay_acc_bch_name;
	}

	public String getRepay_ac_province() {
		return repay_ac_province;
	}

	public void setRepay_ac_province(String repay_ac_province) {
		this.repay_ac_province = repay_ac_province;
	}

	public String getRepay_ac_city() {
		return repay_ac_city;
	}

	public void setRepay_ac_city(String repay_ac_city) {
		this.repay_ac_city = repay_ac_city;
	}

	public String getCrt_usr() {
		return crt_usr;
	}

	public void setCrt_usr(String crt_usr) {
		this.crt_usr = crt_usr;
	}

	public String getSaler_name() {
		return saler_name;
	}

	public void setSaler_name(String saler_name) {
		this.saler_name = saler_name;
	}

	public String getSaler_mobile() {
		return saler_mobile;
	}

	public void setSaler_mobile(String saler_mobile) {
		this.saler_mobile = saler_mobile;
	}

	public String getApp_in_advice() {
		return app_in_advice;
	}

	public void setApp_in_advice(String app_in_advice) {
		this.app_in_advice = app_in_advice;
	}

	public String getOperator_name() {
		return operator_name;
	}

	public void setOperator_name(String operator_name) {
		this.operator_name = operator_name;
	}

	public String getOperator_cde() {
		return operator_cde;
	}

	public void setOperator_cde(String operator_cde) {
		this.operator_cde = operator_cde;
	}

	public String getOperator_tel() {
		return operator_tel;
	}

	public void setOperator_tel(String operator_tel) {
		this.operator_tel = operator_tel;
	}

}
