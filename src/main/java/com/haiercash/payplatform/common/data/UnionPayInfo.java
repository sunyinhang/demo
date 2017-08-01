package com.haiercash.payplatform.common.data;


public class UnionPayInfo {
	private String bank_no; //银行流水号
	private String business_id; //业务ID
	private String debit_no; //扣款账号
	private String receivables_no; //收款账号
	private String amount; //应付金额
	private String pay_status; //支付状态
	private String create_date; //创建时间
	private String updata_date; //修改时间
	private String remark; //备注
	public String getBank_no() {
		return bank_no;
	}
	public void setBank_no(String bank_no) {
		this.bank_no = bank_no;
	}
	public String getBusiness_id() {
		return business_id;
	}
	public void setBusiness_id(String business_id) {
		this.business_id = business_id;
	}
	public String getDebit_no() {
		return debit_no;
	}
	public void setDebit_no(String debit_no) {
		this.debit_no = debit_no;
	}
	public String getReceivables_no() {
		return receivables_no;
	}
	public void setReceivables_no(String receivables_no) {
		this.receivables_no = receivables_no;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public String getPay_status() {
		return pay_status;
	}
	public void setPay_status(String pay_status) {
		this.pay_status = pay_status;
	}
	public String getCreate_date() {
		return create_date;
	}
	public void setCreate_date(String create_date) {
		this.create_date = create_date;
	}
	public String getUpdata_date() {
		return updata_date;
	}
	public void setUpdata_date(String updata_date) {
		this.updata_date = updata_date;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	
}
