package com.haiercash.common.data;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * <p>
 * 标题： 额度申请/客户信息
 * </p>
 * <p>
 * 功能描述：
 * </p>
 * <p>
 * 创建日期：2016年4月12日 下午1:53:13
 * </p>
 * <p>
 * 作者：尹君
 * </p>
 * <p>
 * 公司：济南百思为科
 * </p>
 */

public class CustomerInfoBean {
	/**
	 * rel 联系人姓名 relName 与申请人关系 relRelation 联系人电话 relMobile 所在单位 relEmpName
	 * 联系人居住地址 relAddr
	 */
	/**
	 * 用户Id userId
	 */
	private String userId;

	private String passWord;
	/**
	 * 是否自动提交 autoFlag
	 */
	private String autoFlag;
	/**
	 * 额度申请流水号 applSeq
	 */
	private String applSeq;
	/**
	 * 标示 flag
	 */
	private String flag;
	/**
	 * 姓名 apptCustName
	 */
	private String apptCustName;
	/**
	 * 性别 apptIndivSex
	 */
	private String apptIndivSex;
	/**
	 * 身份证号 apptIdNo
	 */
	private String apptIdNo;
	/**
	 * 手机号 indivMobile
	 */
	private String indivMobile;

	/**
	 * 最高学历 indivEdu
	 */
	private String indivEdu;
	/**
	 * 婚姻状态 indivMarital
	 */
	private String indivMarital;
	/**
	 * 供养子女数 indivDepNo
	 */
	private String indivDepNo;
	/**
	 * 户籍所在省 apptRegProvince
	 */
	private String apptRegProvince;
	/**
	 * 户籍所在市 apptRegCity
	 */
	private String apptRegCity;
	/**
	 * 现居住房屋所有权 liveInfo
	 */
	private String liveInfo;
	/**
	 * 现居住省 liveProvince
	 */
	private String liveProvince;
	/**
	 * 现居住市 liveCity
	 */
	private String liveCity;
	/**
	 * 现居地址区 liveArea
	 */
	private String liveArea;
	/**
	 * 现居地址 liveAddr
	 */
	private String liveAddr;
	/**
	 * 户口性质 localResid
	 */
	private String localResid;
	/**
	 * 本地居住年限 liveYear
	 */
	private String liveYear;
	/**
	 * 单位名称 indivEmpName
	 */
	private String indivEmpName;
	/**
	 * 行业性质 indivEmpTyp
	 */
	private String indivEmpTyp;
	/**
	 * 所在部门 indivBranch
	 */
	private String indivBranch;
	/**
	 * 职务 indivPosition
	 */
	private String indivPosition;
	/**
	 * 从业性质 positionOpt
	 */
	private String positionOpt;
	/**
	 * 单位地址省 indivEmpProvince
	 */
	private String indivEmpProvince;
	/**
	 * 单位地址市 indivEmpCity
	 */
	private String indivEmpCity;

	/**
	 * 现单位地址区 indivEmpArea
	 */
	private String indivEmpArea;
	/**
	 * 现单位地址 empaddr
	 */
	private String empaddr;
	/**
	 * 月均收入 indivMthInc
	 */
	private String IndivMthInc;
	/**
	 * 税后年收入 annualEarn
	 */
	private String annualEarn;
	/**
	 * 电话 indivEmpTel
	 */
	private String indivEmpTel;
	/**
	 * 总工龄 indivWorkYrs
	 */
	private String indivWorkYrs;
	/**
	 * 申请金额 applyAmt;
	 */
	private String applyAmt;
	/**
	 * 还款卡号 repayApplCardNo
	 */
	private String repayApplCardNo;
	/**
	 * 账户名 repayApplAcNam
	 */
	private String repayApplAcNam;
	/**
	 * 银行号 repayAccBankCde
	 */
	private String repayAccBankCde;
	/**
	 * 开户银行名称 repayAccBankName
	 */
	private String repayAccBankName;
	/**
	 * 开户省 repayAcProvince
	 */
	private String repayAcProvince;
	/**
	 * 开户市 repayAcCity
	 */
	private String repayAcCity;
	/**
	 * 房产地址 pptyLiveOpt
	 */
	private String pptyLiveOpt;
	/**
	 * 房产地址所在省 pptyLiveProvince
	 */
	private String pptyLiveProvince;
	/**
	 * 房产地址所在市 pptyLiveCity
	 */
	private String pptyLiveCity;
	/**
	 * 房产地址所在区 pptyLiveArea
	 */
	private String pptyLiveArea;
	/**
	 * 邮政编码 liveZip
	 */
	private String liveZip;
	/**
	 * 房屋产权人 pptyRighName
	 */
	private String pptyRighName;
	/**
	 * 购买价格 pptyAmt
	 */
	private String pptyAmt;
	/**
	 * 是否按揭 pptyLoanInd
	 */
	private String pptyLoanInd;
	/**
	 * 按揭比例 mortgageRatio
	 */
	private String mortgageRatio;
	/**
	 * 按揭周期（年） pptyLoanYear
	 */
	private String pptyLoanYear;
	/**
	 * 按揭参与人 mortgagePartner
	 */
	private String mortgagePartner;
	/**
	 * 按揭银行 pptyLoanBank
	 */
	private String pptyLoanBank;
	/**
	 * 经办人 operatorCde
	 */
	private String operatorCde;
	/**
	 * 经办人手机号 operatorTel
	 */
	private String operatorTel;
	/**
	 * 门店编号 cooprCde
	 */
	private String cooprCde;
	/**
	 * 门店名称 cooprName
	 */
	private String cooprName;

	/**
	 * 备注 appInAdvice
	 */
	private String appInAdvice;
	/**
	 * 预留1 reserved1
	 */
	private String reserved1;
	/**
	 * 预留2 reserved2
	 */
	private String reserved2;
	/**
	 * 预留3 reserved3
	 */
	private String reserved3;
	/**
	 * 预留4 reserved4
	 */
	private String reserved4;
	/**
	 * 预留5 reserved5
	 */
	private String reserved5;
	/**
	 * 预留6 reserved6
	 */
	private String reserved6;
	/**
	 * 预留7 reserved7
	 */
	private String reserved7;
	/**
	 * 预留8 reserved8
	 */
	private String reserved8;
	/**
	 * 预留9 reserved9
	 */
	private String reserved9;
	/**
	 * 预留10 reserved10
	 */
	private String reserved10;
	/**
	 * rel
	 */
	private List<CustomerFamilyBean> rel = new ArrayList<CustomerFamilyBean>();

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getPassWord() {
		return passWord;
	}

	public void setPassWord(String passWord) {
		this.passWord = passWord;
	}

	public String getAutoFlag() {
		return autoFlag;
	}

	public void setAutoFlag(String autoFlag) {
		this.autoFlag = autoFlag;
	}

	public String getApplSeq() {
		return applSeq;
	}

	public void setApplSeq(String applSeq) {
		this.applSeq = applSeq;
	}

	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}

	public String getApptCustName() {
		return apptCustName;
	}

	public void setApptCustName(String apptCustName) {
		this.apptCustName = apptCustName;
	}

	public String getApptIndivSex() {
		return apptIndivSex;
	}

	public void setApptIndivSex(String apptIndivSex) {
		this.apptIndivSex = apptIndivSex;
	}

	public String getApptIdNo() {
		return apptIdNo;
	}

	public void setApptIdNo(String apptIdNo) {
		this.apptIdNo = apptIdNo;
	}

	public String getIndivMobile() {
		return indivMobile;
	}

	public void setIndivMobile(String indivMobile) {
		this.indivMobile = indivMobile;
	}

	public String getIndivEdu() {
		return indivEdu;
	}

	public void setIndivEdu(String indivEdu) {
		this.indivEdu = indivEdu;
	}

	public String getIndivMarital() {
		return indivMarital;
	}

	public void setIndivMarital(String indivMarital) {
		this.indivMarital = indivMarital;
	}

	public String getIndivDepNo() {
		return indivDepNo;
	}

	public void setIndivDepNo(String indivDepNo) {
		this.indivDepNo = indivDepNo;
	}

	public String getApptRegProvince() {
		return apptRegProvince;
	}

	public void setApptRegProvince(String apptRegProvince) {
		this.apptRegProvince = apptRegProvince;
	}

	public String getApptRegCity() {
		return apptRegCity;
	}

	public void setApptRegCity(String apptRegCity) {
		this.apptRegCity = apptRegCity;
	}

	public String getLiveInfo() {
		return liveInfo;
	}

	public void setLiveInfo(String liveInfo) {
		this.liveInfo = liveInfo;
	}

	public String getLiveProvince() {
		return liveProvince;
	}

	public void setLiveProvince(String liveProvince) {
		this.liveProvince = liveProvince;
	}

	public String getLiveCity() {
		return liveCity;
	}

	public void setLiveCity(String liveCity) {
		this.liveCity = liveCity;
	}

	public String getLiveArea() {
		return liveArea;
	}

	public void setLiveArea(String liveArea) {
		this.liveArea = liveArea;
	}

	public String getLiveAddr() {
		return liveAddr;
	}

	public void setLiveAddr(String liveAddr) {
		this.liveAddr = liveAddr;
	}

	public String getLocalResid() {
		return localResid;
	}

	public void setLocalResid(String localResid) {
		this.localResid = localResid;
	}

	public String getLiveYear() {
		return liveYear;
	}

	public void setLiveYear(String liveYear) {
		this.liveYear = liveYear;
	}

	public String getIndivEmpName() {
		return indivEmpName;
	}

	public void setIndivEmpName(String indivEmpName) {
		this.indivEmpName = indivEmpName;
	}

	public String getIndivEmpTyp() {
		return indivEmpTyp;
	}

	public void setIndivEmpTyp(String indivEmpTyp) {
		this.indivEmpTyp = indivEmpTyp;
	}

	public String getIndivBranch() {
		return indivBranch;
	}

	public void setIndivBranch(String indivBranch) {
		this.indivBranch = indivBranch;
	}

	public String getIndivPosition() {
		return indivPosition;
	}

	public void setIndivPosition(String indivPosition) {
		this.indivPosition = indivPosition;
	}

	public String getPositionOpt() {
		return positionOpt;
	}

	public void setPositionOpt(String positionOpt) {
		this.positionOpt = positionOpt;
	}

	public String getIndivEmpProvince() {
		return indivEmpProvince;
	}

	public void setIndivEmpProvince(String indivEmpProvince) {
		this.indivEmpProvince = indivEmpProvince;
	}

	public String getIndivEmpCity() {
		return indivEmpCity;
	}

	public void setIndivEmpCity(String indivEmpCity) {
		this.indivEmpCity = indivEmpCity;
	}

	public String getIndivEmpArea() {
		return indivEmpArea;
	}

	public void setIndivEmpArea(String indivEmpArea) {
		this.indivEmpArea = indivEmpArea;
	}

	public String getEmpaddr() {
		return empaddr;
	}

	public void setEmpaddr(String empaddr) {
		this.empaddr = empaddr;
	}

	public String getIndivMthInc() {
		return IndivMthInc;
	}

	public void setIndivMthInc(String indivMthInc) {
		IndivMthInc = indivMthInc;
	}

	public String getAnnualEarn() {
		return annualEarn;
	}

	public void setAnnualEarn(String annualEarn) {
		this.annualEarn = annualEarn;
	}

	public String getIndivEmpTel() {
		return indivEmpTel;
	}

	public void setIndivEmpTel(String indivEmpTel) {
		this.indivEmpTel = indivEmpTel;
	}

	public String getIndivWorkYrs() {
		return indivWorkYrs;
	}

	public void setIndivWorkYrs(String indivWorkYrs) {
		this.indivWorkYrs = indivWorkYrs;
	}

	public String getApplyAmt() {
		return applyAmt;
	}

	public void setApplyAmt(String applyAmt) {
		this.applyAmt = applyAmt;
	}

	public String getRepayApplCardNo() {
		return repayApplCardNo;
	}

	public void setRepayApplCardNo(String repayApplCardNo) {
		this.repayApplCardNo = repayApplCardNo;
	}

	public String getRepayApplAcNam() {
		return repayApplAcNam;
	}

	public void setRepayApplAcNam(String repayApplAcNam) {
		this.repayApplAcNam = repayApplAcNam;
	}

	public String getRepayAccBankCde() {
		return repayAccBankCde;
	}

	public void setRepayAccBankCde(String repayAccBankCde) {
		this.repayAccBankCde = repayAccBankCde;
	}

	public String getRepayAccBankName() {
		return repayAccBankName;
	}

	public void setRepayAccBankName(String repayAccBankName) {
		this.repayAccBankName = repayAccBankName;
	}

	public String getRepayAcProvince() {
		return repayAcProvince;
	}

	public void setRepayAcProvince(String repayAcProvince) {
		this.repayAcProvince = repayAcProvince;
	}

	public String getRepayAcCity() {
		return repayAcCity;
	}

	public void setRepayAcCity(String repayAcCity) {
		this.repayAcCity = repayAcCity;
	}

	public String getPptyLiveOpt() {
		return pptyLiveOpt;
	}

	public void setPptyLiveOpt(String pptyLiveOpt) {
		this.pptyLiveOpt = pptyLiveOpt;
	}

	public String getPptyLiveProvince() {
		return pptyLiveProvince;
	}

	public void setPptyLiveProvince(String pptyLiveProvince) {
		this.pptyLiveProvince = pptyLiveProvince;
	}

	public String getPptyLiveCity() {
		return pptyLiveCity;
	}

	public void setPptyLiveCity(String pptyLiveCity) {
		this.pptyLiveCity = pptyLiveCity;
	}

	public String getPptyLiveArea() {
		return pptyLiveArea;
	}

	public void setPptyLiveArea(String pptyLiveArea) {
		this.pptyLiveArea = pptyLiveArea;
	}

	public String getLiveZip() {
		return liveZip;
	}

	public void setLiveZip(String liveZip) {
		this.liveZip = liveZip;
	}

	public String getPptyRighName() {
		return pptyRighName;
	}

	public void setPptyRighName(String pptyRighName) {
		this.pptyRighName = pptyRighName;
	}

	public String getPptyAmt() {
		return pptyAmt;
	}

	public void setPptyAmt(String pptyAmt) {
		this.pptyAmt = pptyAmt;
	}

	public String getPptyLoanInd() {
		return pptyLoanInd;
	}

	public void setPptyLoanInd(String pptyLoanInd) {
		this.pptyLoanInd = pptyLoanInd;
	}

	public String getMortgageRatio() {
		return mortgageRatio;
	}

	public void setMortgageRatio(String mortgageRatio) {
		this.mortgageRatio = mortgageRatio;
	}

	public String getPptyLoanYear() {
		return pptyLoanYear;
	}

	public void setPptyLoanYear(String pptyLoanYear) {
		this.pptyLoanYear = pptyLoanYear;
	}

	public String getMortgagePartner() {
		return mortgagePartner;
	}

	public void setMortgagePartner(String mortgagePartner) {
		this.mortgagePartner = mortgagePartner;
	}

	public String getPptyLoanBank() {
		return pptyLoanBank;
	}

	public void setPptyLoanBank(String pptyLoanBank) {
		this.pptyLoanBank = pptyLoanBank;
	}

	public String getOperatorCde() {
		return operatorCde;
	}

	public void setOperatorCde(String operatorCde) {
		this.operatorCde = operatorCde;
	}

	public String getOperatorTel() {
		return operatorTel;
	}

	public void setOperatorTel(String operatorTel) {
		this.operatorTel = operatorTel;
	}

	public String getCooprCde() {
		return cooprCde;
	}

	public void setCooprCde(String cooprCde) {
		this.cooprCde = cooprCde;
	}

	public String getCooprName() {
		return cooprName;
	}

	public void setCooprName(String cooprName) {
		this.cooprName = cooprName;
	}

	public String getAppInAdvice() {
		return appInAdvice;
	}

	public void setAppInAdvice(String appInAdvice) {
		this.appInAdvice = appInAdvice;
	}

	public String getReserved1() {
		return reserved1;
	}

	public void setReserved1(String reserved1) {
		this.reserved1 = reserved1;
	}

	public String getReserved2() {
		return reserved2;
	}

	public void setReserved2(String reserved2) {
		this.reserved2 = reserved2;
	}

	public String getReserved3() {
		return reserved3;
	}

	public void setReserved3(String reserved3) {
		this.reserved3 = reserved3;
	}

	public String getReserved4() {
		return reserved4;
	}

	public void setReserved4(String reserved4) {
		this.reserved4 = reserved4;
	}

	public String getReserved5() {
		return reserved5;
	}

	public void setReserved5(String reserved5) {
		this.reserved5 = reserved5;
	}

	public String getReserved6() {
		return reserved6;
	}

	public void setReserved6(String reserved6) {
		this.reserved6 = reserved6;
	}

	public String getReserved7() {
		return reserved7;
	}

	public void setReserved7(String reserved7) {
		this.reserved7 = reserved7;
	}

	public String getReserved8() {
		return reserved8;
	}

	public void setReserved8(String reserved8) {
		this.reserved8 = reserved8;
	}

	public String getReserved9() {
		return reserved9;
	}

	public void setReserved9(String reserved9) {
		this.reserved9 = reserved9;
	}

	public String getReserved10() {
		return reserved10;
	}

	public void setReserved10(String reserved10) {
		this.reserved10 = reserved10;
	}

	public List<CustomerFamilyBean> getRel() {
		return rel;
	}

	public void setRel(List<CustomerFamilyBean> rel) {
		this.rel = rel;
	}

}
