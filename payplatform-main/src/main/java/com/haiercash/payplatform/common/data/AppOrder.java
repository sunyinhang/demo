package com.haiercash.payplatform.common.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.util.StringUtils;

import javax.persistence.Transient;
import java.math.BigDecimal;
import java.util.List;

public class AppOrder {
	private String orderNo;// 订单1号
	private String mallOrderNo;//商城订单号
	private String applseq;// 申请流水号（为防混淆废弃，但不能删）
	private String idTyp;// 客户证件类型
	private String idNo;// 客户证件号码
	private String custName;// 客户姓名
	private String indivMobile;// 客户  手机号
	private String merchNo;// 商户编号
	private String cooprCde;// 门店代码
	private String cooprName;// 门店名称
	private String contZone;// 门店联系电话区号
	private String contTel;// 门店联系电话
	private String contSub;// 门店联系电话分机
	private String typGrp;// 贷款类型
	private String purpose;// 贷款用途
	private String promCde;// 营销人员编码
	private String promPhone;// 营销人员电话
	private String promDesc;// 营销人员详情
	private String typCde;// 贷款品种代码
	private String typVer;// 贷款品种版本号
	private String typSeq;// 贷款品种流水号
	private String typLevelTwo;// 贷款品种类别
	private String applyDt;// 申请日期
	private String proPurAmt;//  商品总额
	private String fstPct;//
	private String fstPay;// 首付金额
	private String applyAmt;// 借款总额
	private String applyTnr;// 借款期限
	private String applyTnrTyp;// 借款期限类型
	private String totalnormint;// 总利息金额
	private String totalfeeamt;// 费用总额
	private String deliverAddrTyp;// 送货地址类型
	private String deliverAddr;// 送货地址
	private String apprSts;// 审批状态
	@Deprecated
	private String deliverSts;// 发货状态
	private String setlSts;// 还款状态
	private String otherPurpose;//
	private String monthRepay;//
	private String operGoodsTyp;//
	private String mtdCde;// 还款方式代码
	private String mtdName;// 还款方式名称
	private String payMtd;// 还款方式种类代码
	private String payMtdDesc;// 还款方式种类名称
	private String loanFreq;// 还款间隔
	private String dueDayOpt;//
	private String dueDay;//
	private String docChannel;// 进件通路
	private String applAcTyp;// 放款账号类型
	private String applAcNam;// 放款账号户名
	private String applCardNo;// 放款卡号
	private String accBankCde;// 放款开户银行代码
	private String accBankName;// 放款开户银行名
	private String accAcBchCde;// 放款开户银行分支行代码
	private String accAcBchName;// 放款开户银行分支行名
	private String accAcProvince;// 放款开户行所在省
	private String accAcCity;// 放款开户行所在市
	private String repayApplAcNam;// 还款账号户名
	private String repayApplCardNo;// 还款卡号
	private String repayAccBankCde;// 还款开户银行代码
	private String repayAccBankName;// 还款开户银行名
	private String repayAccBchCde;// 还款开户银行分支行代码
	private String repayAccBchName;// 还款开户银行分支行名
	private String repayAcProvince;// 还款账户所在省
	private String repayAcCity;// 还款账户所在市
	private String crtTyp;// 销售代表类型
	private String crtUsr;// 销售代表代码
	private String salerName;// 销售代表姓名
	private String salerMobile;// 销售代表电话
	private String appInAdvice;//录单备注
	private String operatorName;// 客户经理名称
	private String operatorCde;// 客户经理代码
	private String operatorTel;// 客户经理联系电话
	private String custNo;// 客户编号
	private String isConfirmAgreement;// 是否已确认协议
	private String isConfirmContract;// 是否已确认合同
	private String liveInfo;// 客户居住地址
	private String email;// 邮箱
	private String source;// 订单来源
	private String status;// 订单状态
	private String backReason;// 退回原因
	private String typDesc;// 贷款品种名称
	private String applCde;// 贷款编号
	//@JsonProperty(value = "WhiteType")
	private String whiteType;// 白名单类型
	private String isCustInfoCompleted;// 个人信息是否已完整
	private String apprvAmt;
	private String state; // update-修改，合同签约提交时调用渠道进件接口
	private String version;//版本号
	private String channelNo;// 渠道编号
	private String expectCredit;//期望额度
	@Transient
	private String formType;// 订单类型

	/**
	 * 商品信息相关字段，只提供属性，不提供数据库字段（服务于现金贷使用）
	 **/
	@Transient
	private String goodsCode;// 商品代码
	@Transient
	private String goodsBrand;// 商品品牌
	@Transient
	private String goodsKind;// 商品类型
	@Transient
	private String goodsName;// 商品名称
	@Transient
	private String goodsModel;// 商品型号
	@Transient
	private String goodsNum;// 数量
	@Transient
	private String goodsPrice;// 单价

    @Transient
	List<AppOrderGoods> appOrderGoodsList;

	/**
	 * @author 尹君
	 * @date 7月11补充送货地址相关字段
	 * 
	 */
	private String deliverProvince;//送货地址省
	private String deliverCity;//送货地址市
	private String deliverArea;//送货地址区
	@Transient
	private String adName; //收货人姓名
	@Transient
	private String adPhone; //收货人联系方式
	@Transient
	@JsonProperty(value = "WhiteType")
	private String whiteType1;

	/**
	 * 共同还款人相关字段，个人版现金贷用，只提供属性，不提供数据库字段
	 * @return
     */
	//private String orderNo;// 订单编号
	//private String custNo;// 客户编号
	@Transient
	private String relation;// 关系
	@Transient
	private String maritalStatus;// 婚姻状况
	@Transient
	private String officeName;// 工作单位
	@Transient
//	private Double mthInc;// 月收入
	private BigDecimal mthInc;// 月收入
	@Transient
	private String officeTel;// 单位电话

	private String commonCustNo;// 共同还款人客户编号
	@Transient
	private String smsCode;// 短信验证码
	@Transient
	private String applSeq; // 信贷流水号
    @Transient
    private String userId; // 个人客户的用户账号（个人版）

	/**
	 * @date:2016-09-21
	 * 添加贷款品种详情相关字段，以将贷款品种详情保存到订单中
     */
	private Double pLoanTypFstPct;//贷款品种详情的最低首付比例(fstPct)
	private Double pLoanTypMinAmt; //单笔最小贷款金额(minAmt)
	private Double pLoanTypMaxAmt;       //单笔最大贷款金额(maxAmt)
	private Integer pLoanTypGoodMaxNum;//同笔贷款同型号商品数量上限(goodMaxNum)
    private String pLoanTypTnrOpt;	//借款期限(tnrOpt)
	//private String pLoanTypPayMtd;//还款方式(payMtd)
	//private String pLoanTypLevelTwo;//贷款品种小类(levelTwo)]
	private String repayAccMobile;//还款卡手机号 2016/10/26添加改字段

	/**
	 * @date:2016-10-28
	 * 人脸识别相关字段
	 */
	private String faceTypCde;// 人脸识别贷款品种
	private String faceValue;//贷款品种人脸分值
	private String applyFaceSucc;//申请人人脸识别是否成功
	private String applyFaceCount;//申请人人脸识别次数
	private String applyFaceValue;//申请人人脸识别分值
	private String comApplyFaceSucc;//共同申请人人脸识别是否成功
	private String comApplyFaceCount;//共同申请人人脸识别次数
	private String comApplyFaceValue;//共同申请人人脸识别分值


	public String getChannelNo() {
		return channelNo;
	}

	public void setChannelNo(String channelNo) {
		this.channelNo = channelNo;
	}

	public String getExpectCredit() {
		return expectCredit;
	}

	public void setExpectCredit(String expectCredit) {
		this.expectCredit = expectCredit;
	}

	public String getFaceTypCde() {
		return faceTypCde;
	}

	public void setFaceTypCde(String faceTypCde) {
		this.faceTypCde = faceTypCde;
	}

	public String getFaceValue() {
		return faceValue;
	}

	public void setFaceValue(String faceValue) {
		this.faceValue = faceValue;
	}

	public String getApplyFaceSucc() {
		return applyFaceSucc;
	}

	public void setApplyFaceSucc(String applyFaceSucc) {
		this.applyFaceSucc = applyFaceSucc;
	}

	public String getApplyFaceCount() {
		return applyFaceCount;
	}

	public void setApplyFaceCount(String applyFaceCount) {
		this.applyFaceCount = applyFaceCount;
	}

	public String getApplyFaceValue() {
		return applyFaceValue;
	}

	public void setApplyFaceValue(String applyFaceValue) {
		this.applyFaceValue = applyFaceValue;
	}

	public String getComApplyFaceSucc() {
		return comApplyFaceSucc;
	}

	public void setComApplyFaceSucc(String comApplyFaceSucc) {
		this.comApplyFaceSucc = comApplyFaceSucc;
	}

	public String getComApplyFaceCount() {
		return comApplyFaceCount;
	}

	public void setComApplyFaceCount(String comApplyFaceCount) {
		this.comApplyFaceCount = comApplyFaceCount;
	}

	public String getComApplyFaceValue() {
		return comApplyFaceValue;
	}

	public void setComApplyFaceValue(String comApplyFaceValue) {
		this.comApplyFaceValue = comApplyFaceValue;
	}


	public String getRepayAccMobile() {
		return repayAccMobile;
	}

	public void setRepayAccMobile(String repayAccMobile) {
		this.repayAccMobile = repayAccMobile;
	}


	public Double getpLoanTypFstPct() {
		return pLoanTypFstPct;
	}

	public Double getPLoanTypFstPct() {
		return pLoanTypFstPct;
	}

	public void setpLoanTypFstPct(Double pLoanTypFstPct) {
		this.pLoanTypFstPct = pLoanTypFstPct;
	}

	public void setPLoanTypFstPct(Double pLoanTypFstPct) {
		this.pLoanTypFstPct = pLoanTypFstPct;
	}

	public Integer getpLoanTypGoodMaxNum() {
		return pLoanTypGoodMaxNum;
	}
	public Integer getPLoanTypGoodMaxNum() {
		return pLoanTypGoodMaxNum;
	}

	public void setpLoanTypGoodMaxNum(Integer pLoanTypGoodMaxNum) {
		this.pLoanTypGoodMaxNum = pLoanTypGoodMaxNum;
	}

	public void setPLoanTypGoodMaxNum(Integer pLoanTypGoodMaxNum) {
		this.pLoanTypGoodMaxNum = pLoanTypGoodMaxNum;
	}

//	public String getpLoanTypLevelTwo() {
//		return pLoanTypLevelTwo;
//	}

	//public void setpLoanTypLevelTwo(String pLoanTypLevelTwo) {
	//	this.pLoanTypLevelTwo = pLoanTypLevelTwo;
//	}

	public Double getpLoanTypMaxAmt() {
		return pLoanTypMaxAmt;
	}
	public Double getPLoanTypMaxAmt() {
		return pLoanTypMaxAmt;
	}

	public void setpLoanTypMaxAmt(Double pLoanTypMaxAmt) {
		this.pLoanTypMaxAmt = pLoanTypMaxAmt;
	}

	public void setPLoanTypMaxAmt(Double pLoanTypMaxAmt) {
		this.pLoanTypMaxAmt = pLoanTypMaxAmt;
	}

	public Double getpLoanTypMinAmt() {
		return pLoanTypMinAmt;
	}
	public Double getPLoanTypMinAmt() {
		return pLoanTypMinAmt;
	}

	public void setpLoanTypMinAmt(Double pLoanTypMinAmt) {
		this.pLoanTypMinAmt = pLoanTypMinAmt;
	}

	public void setPLoanTypMinAmt(Double pLoanTypMinAmt) {
		this.pLoanTypMinAmt = pLoanTypMinAmt;
	}

	//public String getpLoanTypPayMtd() {
	//	return pLoanTypPayMtd;
	//}

	//public void setpLoanTypPayMtd(String pLoanTypPayMtd) {
	//	this.pLoanTypPayMtd = pLoanTypPayMtd;
	//}

	public String getpLoanTypTnrOpt() {
		return pLoanTypTnrOpt;
	}
	public String getPLoanTypTnrOpt() {
		return pLoanTypTnrOpt;
	}

	public void setpLoanTypTnrOpt(String pLoanTypTnrOpt) {
		this.pLoanTypTnrOpt = pLoanTypTnrOpt;
	}


	public void setPLoanTypTnrOpt(String pLoanTypTnrOpt) {
		this.pLoanTypTnrOpt = pLoanTypTnrOpt;
	}

	public String getApplSeq() {
		return applSeq;
	}

	public void setApplSeq(String applSeq) {
		this.applSeq = applSeq;
	}

	public String getCommonCustNo() {
		return commonCustNo;
	}

	public void setCommonCustNo(String commonCustNo) {
		this.commonCustNo = commonCustNo;
	}

	public String getMaritalStatus() {
		return maritalStatus;
	}

	public void setMaritalStatus(String maritalStatus) {
		this.maritalStatus = maritalStatus;
	}

//	public Double getMthInc() {
//		return mthInc;
//	}
//
//	public void setMthInc(Double mthInc) {
//		this.mthInc = mthInc;
//	}

	public BigDecimal getMthInc() {
		return mthInc;
	}

	public void setMthInc(BigDecimal mthInc) {
		this.mthInc = mthInc;
	}

	public String getOfficeName() {
		return officeName;
	}

	public void setOfficeName(String officeName) {
		this.officeName = officeName;
	}

	public String getOfficeTel() {
		return officeTel;
	}

	public void setOfficeTel(String officeTel) {
		this.officeTel = officeTel;
	}

	public String getRelation() {
		return relation;
	}

	public void setRelation(String relation) {
		this.relation = relation;
	}

	public String getSmsCode() {
		return smsCode;
	}

	public void setSmsCode(String smsCode) {
		this.smsCode = smsCode;
	}

	public String getWhiteType1() {
		return whiteType;
	}

	public void setWhiteType1(String whiteType1) {
		this.whiteType = whiteType1;
	}

	public String getDeliverProvince() {
		return deliverProvince;
	}

	public void setDeliverProvince(String deliverProvince) {
		this.deliverProvince = deliverProvince;
	}

	public String getDeliverCity() {
		return deliverCity;
	}

	public void setDeliverCity(String deliverCity) {
		this.deliverCity = deliverCity;
	}

	public String getDeliverArea() {
		return deliverArea;
	}

	public void setDeliverArea(String deliverArea) {
		this.deliverArea = deliverArea;
	}

	public String getPayMtd() {
		return payMtd;
	}

	public void setPayMtd(String payMtd) {
		this.payMtd = payMtd;
	}

	public String getPayMtdDesc() {
		return payMtdDesc;
	}

	public void setPayMtdDesc(String payMtdDesc) {
		this.payMtdDesc = payMtdDesc;
	}

	public String getGoodsCode() {
		return goodsCode;
	}

	public void setGoodsCode(String goodsCode) {
		this.goodsCode = goodsCode;
	}

	public String getGoodsBrand() {
		return goodsBrand;
	}

	public void setGoodsBrand(String goodsBrand) {
		this.goodsBrand = goodsBrand;
	}

	public String getGoodsKind() {
		return goodsKind;
	}

	public void setGoodsKind(String goodsKind) {
		this.goodsKind = goodsKind;
	}

	public String getGoodsName() {
		return goodsName;
	}

	public void setGoodsName(String goodsName) {
		this.goodsName = goodsName;
	}

	public String getGoodsModel() {
		return goodsModel;
	}

	public void setGoodsModel(String goodsModel) {
		this.goodsModel = goodsModel;
	}

	public String getGoodsNum() {
		return goodsNum;
	}

	public void setGoodsNum(String goodsNum) {
		this.goodsNum = goodsNum;
	}

	public String getGoodsPrice() {
		return goodsPrice;
	}

	public void setGoodsPrice(String goodsPrice) {
		this.goodsPrice = goodsPrice;
	}

	public String getTypLevelTwo() {
		return typLevelTwo;
	}

	public void setTypLevelTwo(String typLevelTwo) {
		this.typLevelTwo = typLevelTwo;
	}

	public String getMtdName() {
		return mtdName;
	}

	public void setMtdName(String mtdName) {
		this.mtdName = mtdName;
	}

	public String getWhiteType() {
		return whiteType;
	}

	public void setWhiteType(String whiteType) {
		this.whiteType = whiteType;
	}

	public String getIsCustInfoCompleted() {
		return isCustInfoCompleted;
	}

	public void setIsCustInfoCompleted(String isCustInfoCompleted) {
		this.isCustInfoCompleted = isCustInfoCompleted;
	}

	public String getApplCde() {
		return applCde;
	}

	public void setApplCde(String applCde) {
		this.applCde = applCde;
	}

	public String getTypDesc() {
		return typDesc;
	}

	public void setTypDesc(String typDesc) {
		this.typDesc = typDesc;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getBackReason() {
		return backReason;
	}

	public void setBackReason(String backReason) {
		this.backReason = backReason;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getLiveInfo() {
		return liveInfo;
	}

	public void setLiveInfo(String liveInfo) {
		this.liveInfo = liveInfo;
	}

	public String getIsConfirmAgreement() {
		return isConfirmAgreement;
	}

	public void setIsConfirmAgreement(String isConfirmAgreement) {
		if (isConfirmAgreement == null || "".endsWith(isConfirmAgreement)) {
			this.isConfirmAgreement = "否";

		} else {
			this.isConfirmAgreement = isConfirmAgreement;
		}
	}

	public String getIsConfirmContract() {
		return isConfirmContract;
	}

	public void setIsConfirmContract(String isConfirmContract) {

		if (isConfirmContract == null || "".equals(isConfirmContract)) {
			this.isConfirmContract = "否";
		} else {
			this.isConfirmContract = isConfirmContract;

		}
	}

	public String getCustNo() {
		return custNo;
	}

	public void setCustNo(String custNo) {
		this.custNo = custNo;
	}

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	@Deprecated
	public String getApplseq() {
		return applSeq;
	}

	@Deprecated
	public void setApplseq(String applseq) {
		this.applSeq = applseq;
	}

	public String getIdTyp() {
		return idTyp;
	}

	public void setIdTyp(String idTyp) {
		this.idTyp = idTyp;
	}

	public String getIdNo() {
		return idNo;
	}

	public void setIdNo(String idNo) {
		this.idNo = idNo;
	}

	public String getCustName() {
		return custName;
	}

	public void setCustName(String custName) {
		this.custName = custName;
	}

	public String getIndivMobile() {
		return indivMobile;
	}

	public void setIndivMobile(String indivMobile) {
		this.indivMobile = indivMobile;
	}

	public String getMerchNo() {
		return merchNo;
	}

	public void setMerchNo(String merchNo) {
		this.merchNo = merchNo;
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

	public String getContZone() {
		return contZone;
	}

	public void setContZone(String contZone) {
		this.contZone = contZone;
	}

	public String getContTel() {
		return contTel;
	}

	public void setContTel(String contTel) {
		this.contTel = contTel;
	}

	public String getContSub() {
		return contSub;
	}

	public void setContSub(String contSub) {
		this.contSub = contSub;
	}

	public String getTypGrp() {
		return typGrp;
	}

	public void setTypGrp(String typGrp) {
		this.typGrp = typGrp;
	}

	public String getPurpose() {
		return purpose;
	}

	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}

	public String getPromCde() {
		return promCde;
	}

	public void setPromCde(String promCde) {
		this.promCde = promCde;
	}

	public String getPromPhone() {
		return promPhone;
	}

	public void setPromPhone(String promPhone) {
		this.promPhone = promPhone;
	}

	public String getPromDesc() {
		return promDesc;
	}

	public void setPromDesc(String promDesc) {
		this.promDesc = promDesc;
	}

	public String getTypCde() {
		return typCde;
	}

	public void setTypCde(String typCde) {
		this.typCde = typCde;
	}

	public String getTypVer() {
		return typVer;
	}

	public void setTypVer(String typVer) {
		this.typVer = typVer;
	}

	public String getTypSeq() {
		return typSeq;
	}

	public void setTypSeq(String typSeq) {
		this.typSeq = typSeq;
	}

	public String getApplyDt() {
		return applyDt;
	}

	public void setApplyDt(String applyDt) {
		this.applyDt = applyDt;
	}

	public String getProPurAmt() {
		return proPurAmt;
	}

	public void setProPurAmt(String proPurAmt) {
		this.proPurAmt = proPurAmt;
	}

	public String getFstPct() {
		return fstPct;
	}

	public void setFstPct(String fstPct) {
		this.fstPct = fstPct;
	}

	public String getFstPay() {
		return fstPay;
	}

	public void setFstPay(String fstPay) {
		this.fstPay = fstPay;
	}

	public String getApplyAmt() {
		return applyAmt;
	}

	public void setApplyAmt(String applyAmt) {
		this.applyAmt = applyAmt;
	}

	public String getApplyTnr() {
		return applyTnr;
	}

	public void setApplyTnr(String applyTnr) {
		this.applyTnr = applyTnr;
	}

	public String getApplyTnrTyp() {
		return applyTnrTyp;
	}

	public void setApplyTnrTyp(String applyTnrTyp) {
		this.applyTnrTyp = applyTnrTyp;
	}

	public String getTotalnormint() {
		return totalnormint;
	}

	public void setTotalnormint(String totalnormint) {
		this.totalnormint = totalnormint;
	}

	public String getTotalfeeamt() {
		return totalfeeamt;
	}

	public void setTotalfeeamt(String totalfeeamt) {
		this.totalfeeamt = totalfeeamt;
	}

	public String getDeliverAddrTyp() {
		return deliverAddrTyp;
	}

	public void setDeliverAddrTyp(String deliverAddrTyp) {
		this.deliverAddrTyp = deliverAddrTyp;
	}

	public String getDeliverAddr() {
		return deliverAddr;
	}

	public void setDeliverAddr(String deliverAddr) {
		this.deliverAddr = deliverAddr;
	}

	public String getApprSts() {
		return apprSts;
	}

	public void setApprSts(String apprSts) {
		this.apprSts = apprSts;
	}

	public String getDeliverSts() {
		return deliverSts;
	}

	public void setDeliverSts(String deliverSts) {
		this.deliverSts = deliverSts;
	}

	public String getSetlSts() {
		return setlSts;
	}

	public void setSetlSts(String setlSts) {
		this.setlSts = setlSts;
	}

	public String getOtherPurpose() {
		return otherPurpose;
	}

	public void setOtherPurpose(String otherPurpose) {
		this.otherPurpose = otherPurpose;
	}

	public String getMonthRepay() {
		return monthRepay;
	}

	public void setMonthRepay(String monthRepay) {
		this.monthRepay = monthRepay;
	}

	public String getOperGoodsTyp() {
		return operGoodsTyp;
	}

	public void setOperGoodsTyp(String operGoodsTyp) {
		this.operGoodsTyp = operGoodsTyp;
	}

	public String getMtdCde() {
		return mtdCde;
	}

	public void setMtdCde(String mtdCde) {
		this.mtdCde = mtdCde;
	}

	public String getLoanFreq() {
		return loanFreq;
	}

	public void setLoanFreq(String loanFreq) {
		this.loanFreq = loanFreq;
	}

	public String getDueDayOpt() {
		return dueDayOpt;
	}

	public void setDueDayOpt(String dueDayOpt) {
		this.dueDayOpt = dueDayOpt;
	}

	public String getDueDay() {
		return dueDay;
	}

	public void setDueDay(String dueDay) {
		this.dueDay = dueDay;
	}

	public String getDocChannel() {
		return docChannel;
	}

	public void setDocChannel(String docChannel) {
		this.docChannel = docChannel;
	}

	public String getApplAcTyp() {
		return applAcTyp;
	}

	public void setApplAcTyp(String applAcTyp) {
		this.applAcTyp = applAcTyp;
	}

	public String getApplAcNam() {
		return applAcNam;
	}

	public void setApplAcNam(String applAcNam) {
		this.applAcNam = applAcNam;
	}

	public String getApplCardNo() {
		return applCardNo;
	}

	public void setApplCardNo(String applCardNo) {
		this.applCardNo = applCardNo;
	}

	public String getAccBankCde() {
		return accBankCde;
	}

	public void setAccBankCde(String accBankCde) {
		this.accBankCde = accBankCde;
	}

	public String getAccBankName() {
		return accBankName;
	}

	public void setAccBankName(String accBankName) {
		this.accBankName = accBankName;
	}

	public String getAccAcBchCde() {
		return accAcBchCde;
	}

	public void setAccAcBchCde(String accAcBchCde) {
		this.accAcBchCde = accAcBchCde;
	}

	public String getAccAcBchName() {
		return accAcBchName;
	}

	public void setAccAcBchName(String accAcBchName) {
		this.accAcBchName = accAcBchName;
	}

	public String getAccAcProvince() {
		return accAcProvince;
	}

	public void setAccAcProvince(String accAcProvince) {
		this.accAcProvince = accAcProvince;
	}

	public String getAccAcCity() {
		return accAcCity;
	}

	public void setAccAcCity(String accAcCity) {
		this.accAcCity = accAcCity;
	}

	public String getRepayApplAcNam() {
		return repayApplAcNam;
	}

	public void setRepayApplAcNam(String repayApplAcNam) {
		this.repayApplAcNam = repayApplAcNam;
	}

	public String getRepayApplCardNo() {
		return repayApplCardNo;
	}

	public void setRepayApplCardNo(String repayApplCardNo) {
		this.repayApplCardNo = repayApplCardNo;
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

	public String getRepayAccBchCde() {
		return repayAccBchCde;
	}

	public void setRepayAccBchCde(String repayAccBchCde) {
		this.repayAccBchCde = repayAccBchCde;
	}

	public String getRepayAccBchName() {
		return repayAccBchName;
	}

	public void setRepayAccBchName(String repayAccBchName) {
		this.repayAccBchName = repayAccBchName;
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

	public String getCrtTyp() {
		return crtTyp;
	}

	public void setCrtTyp(String crtTyp) {
		this.crtTyp = crtTyp;
	}

	public String getCrtUsr() {
		return crtUsr;
	}

	public void setCrtUsr(String crtUsr) {
		this.crtUsr = crtUsr;
	}

	public String getSalerName() {
		return salerName;
	}

	public void setSalerName(String salerName) {
		this.salerName = salerName;
	}

	public String getSalerMobile() {
		return salerMobile;
	}

	public void setSalerMobile(String salerMobile) {
		this.salerMobile = salerMobile;
	}

	public String getAppInAdvice() {
		return appInAdvice;
	}

	public void setAppInAdvice(String appInAdvice) {
		this.appInAdvice = appInAdvice;
	}

	public String getOperatorName() {
		return operatorName;
	}

	public void setOperatorName(String operatorName) {
		this.operatorName = operatorName;
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

	public String getApprvAmt() {
		return apprvAmt;
	}

	public void setApprvAmt(String apprvAmt) {
		this.apprvAmt = apprvAmt;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		if(StringUtils.isEmpty(version)){
			this.version="0";
		}else {
			this.version = version;
		}
	}

	public String getAdName() {
		return adName;
	}

	public void setAdName(String adName) {
		this.adName = adName;
	}

	public String getAdPhone() {
		return adPhone;
	}

	public void setAdPhone(String adPhone) {
		this.adPhone = adPhone;
	}

	public String getFormType() {
		return formType;
	}

	public void setFormType(String formType) {
		this.formType = formType;
	}

	public List<AppOrderGoods> getAppOrderGoodsList() {
		return appOrderGoodsList;
	}

	public void setAppOrderGoodsList(List<AppOrderGoods> appOrderGoodsList) {
		this.appOrderGoodsList = appOrderGoodsList;
	}

	public String getMallOrderNo() {
		return mallOrderNo;
	}

	public void setMallOrderNo(String mallOrderNo) {
		this.mallOrderNo = mallOrderNo;
	}

	@Override
	public String toString() {
		return "AppOrder{" +
				"orderNo='" + orderNo + '\'' +
                ", mallOrderNo='" + mallOrderNo + '\'' +
                ", applseq='" + applseq + '\'' +
				", idTyp='" + idTyp + '\'' +
				", idNo='" + idNo + '\'' +
				", custName='" + custName + '\'' +
				", indivMobile='" + indivMobile + '\'' +
				", merchNo='" + merchNo + '\'' +
				", cooprCde='" + cooprCde + '\'' +
				", cooprName='" + cooprName + '\'' +
				", contZone='" + contZone + '\'' +
				", contTel='" + contTel + '\'' +
				", contSub='" + contSub + '\'' +
				", typGrp='" + typGrp + '\'' +
				", purpose='" + purpose + '\'' +
				", promCde='" + promCde + '\'' +
				", promPhone='" + promPhone + '\'' +
				", promDesc='" + promDesc + '\'' +
				", typCde='" + typCde + '\'' +
				", typVer='" + typVer + '\'' +
				", typSeq='" + typSeq + '\'' +
				", typLevelTwo='" + typLevelTwo + '\'' +
				", applyDt='" + applyDt + '\'' +
				", proPurAmt='" + proPurAmt + '\'' +
				", fstPct='" + fstPct + '\'' +
				", fstPay='" + fstPay + '\'' +
				", applyAmt='" + applyAmt + '\'' +
				", applyTnr='" + applyTnr + '\'' +
				", applyTnrTyp='" + applyTnrTyp + '\'' +
				", totalnormint='" + totalnormint + '\'' +
				", totalfeeamt='" + totalfeeamt + '\'' +
				", deliverAddrTyp='" + deliverAddrTyp + '\'' +
				", deliverAddr='" + deliverAddr + '\'' +
				", apprSts='" + apprSts + '\'' +
				", deliverSts='" + deliverSts + '\'' +
				", setlSts='" + setlSts + '\'' +
				", otherPurpose='" + otherPurpose + '\'' +
				", monthRepay='" + monthRepay + '\'' +
				", operGoodsTyp='" + operGoodsTyp + '\'' +
				", mtdCde='" + mtdCde + '\'' +
				", mtdName='" + mtdName + '\'' +
				", payMtd='" + payMtd + '\'' +
				", payMtdDesc='" + payMtdDesc + '\'' +
				", loanFreq='" + loanFreq + '\'' +
				", dueDayOpt='" + dueDayOpt + '\'' +
				", dueDay='" + dueDay + '\'' +
				", docChannel='" + docChannel + '\'' +
				", applAcTyp='" + applAcTyp + '\'' +
				", applAcNam='" + applAcNam + '\'' +
				", applCardNo='" + applCardNo + '\'' +
				", accBankCde='" + accBankCde + '\'' +
				", accBankName='" + accBankName + '\'' +
				", accAcBchCde='" + accAcBchCde + '\'' +
				", accAcBchName='" + accAcBchName + '\'' +
				", accAcProvince='" + accAcProvince + '\'' +
				", accAcCity='" + accAcCity + '\'' +
				", repayApplAcNam='" + repayApplAcNam + '\'' +
				", repayApplCardNo='" + repayApplCardNo + '\'' +
				", repayAccBankCde='" + repayAccBankCde + '\'' +
				", repayAccBankName='" + repayAccBankName + '\'' +
				", repayAccBchCde='" + repayAccBchCde + '\'' +
				", repayAccBchName='" + repayAccBchName + '\'' +
				", repayAcProvince='" + repayAcProvince + '\'' +
				", repayAcCity='" + repayAcCity + '\'' +
				", crtTyp='" + crtTyp + '\'' +
				", crtUsr='" + crtUsr + '\'' +
				", salerName='" + salerName + '\'' +
				", salerMobile='" + salerMobile + '\'' +
				", appInAdvice='" + appInAdvice + '\'' +
				", operatorName='" + operatorName + '\'' +
				", operatorCde='" + operatorCde + '\'' +
				", operatorTel='" + operatorTel + '\'' +
				", custNo='" + custNo + '\'' +
				", isConfirmAgreement='" + isConfirmAgreement + '\'' +
				", isConfirmContract='" + isConfirmContract + '\'' +
				", liveInfo='" + liveInfo + '\'' +
				", email='" + email + '\'' +
				", source='" + source + '\'' +
				", status='" + status + '\'' +
				", backReason='" + backReason + '\'' +
				", typDesc='" + typDesc + '\'' +
				", applCde='" + applCde + '\'' +
				", whiteType='" + whiteType + '\'' +
				", isCustInfoCompleted='" + isCustInfoCompleted + '\'' +
				", apprvAmt='" + apprvAmt + '\'' +
				", state='" + state + '\'' +
				", version='" + version + '\'' +
				", channelNo='" + channelNo + '\'' +
				", expectCredit='" + expectCredit + '\'' +
				", formType='" + formType + '\'' +
				", goodsCode='" + goodsCode + '\'' +
				", goodsBrand='" + goodsBrand + '\'' +
				", goodsKind='" + goodsKind + '\'' +
				", goodsName='" + goodsName + '\'' +
				", goodsModel='" + goodsModel + '\'' +
				", goodsNum='" + goodsNum + '\'' +
				", goodsPrice='" + goodsPrice + '\'' +
                ", appOrderGoodsList=" + appOrderGoodsList +
                ", deliverProvince='" + deliverProvince + '\'' +
				", deliverCity='" + deliverCity + '\'' +
				", deliverArea='" + deliverArea + '\'' +
				", adName='" + adName + '\'' +
				", adPhone='" + adPhone + '\'' +
				", whiteType1='" + whiteType1 + '\'' +
				", relation='" + relation + '\'' +
				", maritalStatus='" + maritalStatus + '\'' +
				", officeName='" + officeName + '\'' +
				", mthInc=" + mthInc +
				", officeTel='" + officeTel + '\'' +
				", commonCustNo='" + commonCustNo + '\'' +
				", smsCode='" + smsCode + '\'' +
				", applSeq='" + applSeq + '\'' +
				", userId='" + userId + '\'' +
				", pLoanTypFstPct=" + pLoanTypFstPct +
				", pLoanTypMinAmt=" + pLoanTypMinAmt +
				", pLoanTypMaxAmt=" + pLoanTypMaxAmt +
				", pLoanTypGoodMaxNum=" + pLoanTypGoodMaxNum +
				", pLoanTypTnrOpt='" + pLoanTypTnrOpt + '\'' +
				", repayAccMobile='" + repayAccMobile + '\'' +
				", faceTypCde='" + faceTypCde + '\'' +
				", faceValue='" + faceValue + '\'' +
				", applyFaceSucc='" + applyFaceSucc + '\'' +
				", applyFaceCount='" + applyFaceCount + '\'' +
				", applyFaceValue='" + applyFaceValue + '\'' +
				", comApplyFaceSucc='" + comApplyFaceSucc + '\'' +
				", comApplyFaceCount='" + comApplyFaceCount + '\'' +
				", comApplyFaceValue='" + comApplyFaceValue + '\'' +
				'}';
	}
}
