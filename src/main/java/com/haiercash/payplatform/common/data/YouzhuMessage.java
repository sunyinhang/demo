package com.haiercash.payplatform.common.data;
/**
 * 有住业务信息
 * @author zh
 *
 */
public class YouzhuMessage {
	private String businessno;//业务编号
	private String memberno;//有住会员编号
	private String name;//姓名 必填
	private String gender;//性别 必填
	private String age;//年龄 必填
	private String idno;//身份证号 必填
	private String phone;//手机号 必填
	private String yearsalary;//年收入 必填
	private String marriage;//婚姻状况 必填
	private String industrynature;//行业性质 必填
	private String education;//学历状况 必填
	private String monrefamount;//月还款额 必填
	private String creditcardnum;//信用卡个数 必填
	private String maxcreditlines;//最大信用卡额度 必填
	private String housemortgage;//是否按揭 必填
	private String houseprice;//房屋房价 必填
	private String housearea;//房屋面积 必填
	private String expdecoratecost;//预计装修所需金额 必填
	private String actdecoratecost;//实际装修花费金额 必填
	private String calculate;//测算金额 必填
	private String phone2;//备用手机号
	private String homephone;//住宅电话
	private String children;//子女状况
	private String house;//房屋状况
	private String houseregister;//户籍所在地
	private String addressprovince;//家庭住址省 必填
	private String addresscity;//家庭住址市
	private String addressdist;//家庭住址区
	private String address;//家庭住址
	private String residencetime;//居住时间
	private String zipcode;//邮编
	private String company;//单位名称
	private String companyaddressprovince;//单位地址省
	private String companyaddresscity;//单位地址市
	private String companyaddressdist;//单位地址区
	private String companyaddress;//单位地址
	private String companyphone;//单位电话
	private String enploynature;//从业性质
	private String salary;//月基本工资
	private String post;//职务
	private String workyear;//工龄
	private String con1name;//紧急联系人姓名
	private String con1relation;//紧急联系人与申请人关系
	private String con1phone;//紧急联系人电话
	private String con1company;//紧急联系人所在单位
	private String con1addressprovince;//紧急联系人住址省
	private String con1addresscity;//紧急联系人住址市
	private String con1addressdist;//紧急联系人住址区
	private String con1address;//紧急联系人住址
	private String con2name;//紧急联系人姓名
	private String con2relation;//紧急联系人与申请人关系
	private String con2phone;//紧急联系人电话
	private String con2company;//紧急联系人所在单位
	private String con2addressprovince;//紧急联系人住址省
	private String con2addresscity;//紧急联系人住址市
	private String con2addressdist;//紧急联系人住址区
	private String con2address;//紧急联系人住址
	private String con3name;//紧急联系人姓名
	private String con3relation;//紧急联系人与申请人关系
	private String con3phone;//紧急联系人电话
	private String con3company;//紧急联系人所在单位
	private String con3addressprovince;//紧急联系人住址省
	private String con3addresscity;//紧急联系人住址市
	private String con3addressdist;//紧急联系人住址区
	private String con3address;//紧急联系人住址
	private String houseaddress;//房产地址
	private String housezipcode;//房产邮编
	private String houseowner;//房产产权人
	private String mortgageproportion;//按揭比例
	private String mortgagetime;//按揭周期
	private String mortgageperson;//按揭参与人
	private String mortgagebank;//按揭银行
	private String money;//申请金额
	private String staging;//分期期数
	private String refundWay;//还款方式
	private String bank;//所属银行
	private String account;//账户名
	private String cardno;//卡号
	private String relname;//共同还款人姓名
	private String relation;//共同还款人与申请人关系
	private String relidno;//共同还款人身份证号
	private String relworkplace;//共同还款人工作单位
	private String relworkphone;//共同还款人工作电话
	private String relphone;//共同还款人手机
	private String relworknature;//共同还款人单位性质
	private String relpost;//共同还款人职务
	private String relsalary;//共同还款人月工资
	private String relworkyear;//共同还款人工作年限
	private String project;//所属项目
	private String handperson;//经办人
	private String handpersonphone;//经办人电话
	private String applcde;//申请编号
	private String applseq;//申请流水号
	private String flag;//推送成功标志
	private Integer pushnum;//推送次数
	private String state;//订单状态
	private String homeareacode;//住宅电话区号
	private String companyareacode;//单位电话区号
	private String relworkareacode;//共同还款人工作电话区号
	private String commonflag;//是否存在共同还款人
	
	public String getBusinessno() {
		return businessno;
	}
	public void setBusinessno(String businessno) {
		this.businessno = businessno;
	}
	public String getMemberno() {
		return memberno;
	}
	public void setMemberno(String memberno) {
		this.memberno = memberno;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	public String getAge() {
		return age;
	}
	public void setAge(String age) {
		this.age = age;
	}
	public String getIdno() {
		return idno;
	}
	public void setIdno(String idno) {
		this.idno = idno;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getYearsalary() {
		return yearsalary;
	}
	public void setYearsalary(String yearsalary) {
		this.yearsalary = yearsalary;
	}
	public String getMarriage() {
		return marriage;
	}
	public void setMarriage(String marriage) {
		this.marriage = marriage;
	}
	public String getIndustrynature() {
		return industrynature;
	}
	public void setIndustrynature(String industrynature) {
		this.industrynature = industrynature;
	}
	public String getEducation() {
		return education;
	}
	public void setEducation(String education) {
		this.education = education;
	}
	public String getMonrefamount() {
		return monrefamount;
	}
	public void setMonrefamount(String monrefamount) {
		this.monrefamount = monrefamount;
	}
	public String getCreditcardnum() {
		return creditcardnum;
	}
	public void setCreditcardnum(String creditcardnum) {
		this.creditcardnum = creditcardnum;
	}
	public String getMaxcreditlines() {
		return maxcreditlines;
	}
	public void setMaxcreditlines(String maxcreditlines) {
		this.maxcreditlines = maxcreditlines;
	}
	public String getHousemortgage() {
		return housemortgage;
	}
	public void setHousemortgage(String housemortgage) {
		this.housemortgage = housemortgage;
	}
	public String getHouseprice() {
		return houseprice;
	}
	public void setHouseprice(String houseprice) {
		this.houseprice = houseprice;
	}
	public String getHousearea() {
		return housearea;
	}
	public void setHousearea(String housearea) {
		this.housearea = housearea;
	}
	public String getExpdecoratecost() {
		return expdecoratecost;
	}
	public void setExpdecoratecost(String expdecoratecost) {
		this.expdecoratecost = expdecoratecost;
	}
	public String getActdecoratecost() {
		return actdecoratecost;
	}
	public void setActdecoratecost(String actdecoratecost) {
		this.actdecoratecost = actdecoratecost;
	}
	public String getCalculate() {
		return calculate;
	}
	public void setCalculate(String calculate) {
		this.calculate = calculate;
	}
	public String getPhone2() {
		return phone2;
	}
	public void setPhone2(String phone2) {
		this.phone2 = phone2;
	}
	public String getHomephone() {
		return homephone;
	}
	public void setHomephone(String homephone) {
		this.homephone = homephone;
	}
	public String getChildren() {
		return children;
	}
	public void setChildren(String children) {
		this.children = children;
	}
	public String getHouse() {
		return house;
	}
	public void setHouse(String house) {
		this.house = house;
	}
	public String getHouseregister() {
		return houseregister;
	}
	public void setHouseregister(String houseregister) {
		this.houseregister = houseregister;
	}
	public String getAddressprovince() {
		return addressprovince;
	}
	public void setAddressprovince(String addressprovince) {
		this.addressprovince = addressprovince;
	}
	public String getAddresscity() {
		return addresscity;
	}
	public void setAddresscity(String addresscity) {
		this.addresscity = addresscity;
	}
	public String getAddressdist() {
		return addressdist;
	}
	public void setAddressdist(String addressdist) {
		this.addressdist = addressdist;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getResidencetime() {
		return residencetime;
	}
	public void setResidencetime(String residencetime) {
		this.residencetime = residencetime;
	}
	public String getZipcode() {
		return zipcode;
	}
	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}
	public String getCompany() {
		return company;
	}
	public void setCompany(String company) {
		this.company = company;
	}
	public String getCompanyaddressprovince() {
		return companyaddressprovince;
	}
	public void setCompanyaddressprovince(String companyaddressprovince) {
		this.companyaddressprovince = companyaddressprovince;
	}
	public String getCompanyaddresscity() {
		return companyaddresscity;
	}
	public void setCompanyaddresscity(String companyaddresscity) {
		this.companyaddresscity = companyaddresscity;
	}
	public String getCompanyaddressdist() {
		return companyaddressdist;
	}
	public void setCompanyaddressdist(String companyaddressdist) {
		this.companyaddressdist = companyaddressdist;
	}
	public String getCompanyaddress() {
		return companyaddress;
	}
	public void setCompanyaddress(String companyaddress) {
		this.companyaddress = companyaddress;
	}
	public String getCompanyphone() {
		return companyphone;
	}
	public void setCompanyphone(String companyphone) {
		this.companyphone = companyphone;
	}
	public String getEnploynature() {
		return enploynature;
	}
	public void setEnploynature(String enploynature) {
		this.enploynature = enploynature;
	}
	public String getSalary() {
		return salary;
	}
	public void setSalary(String salary) {
		this.salary = salary;
	}
	public String getPost() {
		return post;
	}
	public void setPost(String post) {
		this.post = post;
	}
	public String getWorkyear() {
		return workyear;
	}
	public void setWorkyear(String workyear) {
		this.workyear = workyear;
	}
	public String getCon1name() {
		return con1name;
	}
	public void setCon1name(String con1name) {
		this.con1name = con1name;
	}
	public String getCon1relation() {
		return con1relation;
	}
	public void setCon1relation(String con1relation) {
		this.con1relation = con1relation;
	}
	public String getCon1phone() {
		return con1phone;
	}
	public void setCon1phone(String con1phone) {
		this.con1phone = con1phone;
	}
	public String getCon1company() {
		return con1company;
	}
	public void setCon1company(String con1company) {
		this.con1company = con1company;
	}
	public String getCon1addressprovince() {
		return con1addressprovince;
	}
	public void setCon1addressprovince(String con1addressprovince) {
		this.con1addressprovince = con1addressprovince;
	}
	public String getCon1addresscity() {
		return con1addresscity;
	}
	public void setCon1addresscity(String con1addresscity) {
		this.con1addresscity = con1addresscity;
	}
	public String getCon1addressdist() {
		return con1addressdist;
	}
	public void setCon1addressdist(String con1addressdist) {
		this.con1addressdist = con1addressdist;
	}
	public String getCon1address() {
		return con1address;
	}
	public void setCon1address(String con1address) {
		this.con1address = con1address;
	}
	public String getCon2name() {
		return con2name;
	}
	public void setCon2name(String con2name) {
		this.con2name = con2name;
	}
	public String getCon2relation() {
		return con2relation;
	}
	public void setCon2relation(String con2relation) {
		this.con2relation = con2relation;
	}
	public String getCon2phone() {
		return con2phone;
	}
	public void setCon2phone(String con2phone) {
		this.con2phone = con2phone;
	}
	public String getCon2company() {
		return con2company;
	}
	public void setCon2company(String con2company) {
		this.con2company = con2company;
	}
	public String getCon2addressprovince() {
		return con2addressprovince;
	}
	public void setCon2addressprovince(String con2addressprovince) {
		this.con2addressprovince = con2addressprovince;
	}
	public String getCon2addresscity() {
		return con2addresscity;
	}
	public void setCon2addresscity(String con2addresscity) {
		this.con2addresscity = con2addresscity;
	}
	public String getCon2addressdist() {
		return con2addressdist;
	}
	public void setCon2addressdist(String con2addressdist) {
		this.con2addressdist = con2addressdist;
	}
	public String getCon2address() {
		return con2address;
	}
	public void setCon2address(String con2address) {
		this.con2address = con2address;
	}
	public String getCon3name() {
		return con3name;
	}
	public void setCon3name(String con3name) {
		this.con3name = con3name;
	}
	public String getCon3relation() {
		return con3relation;
	}
	public void setCon3relation(String con3relation) {
		this.con3relation = con3relation;
	}
	public String getCon3phone() {
		return con3phone;
	}
	public void setCon3phone(String con3phone) {
		this.con3phone = con3phone;
	}
	public String getCon3company() {
		return con3company;
	}
	public void setCon3company(String con3company) {
		this.con3company = con3company;
	}
	public String getCon3addressprovince() {
		return con3addressprovince;
	}
	public void setCon3addressprovince(String con3addressprovince) {
		this.con3addressprovince = con3addressprovince;
	}
	public String getCon3addresscity() {
		return con3addresscity;
	}
	public void setCon3addresscity(String con3addresscity) {
		this.con3addresscity = con3addresscity;
	}
	public String getCon3addressdist() {
		return con3addressdist;
	}
	public void setCon3addressdist(String con3addressdist) {
		this.con3addressdist = con3addressdist;
	}
	public String getCon3address() {
		return con3address;
	}
	public void setCon3address(String con3address) {
		this.con3address = con3address;
	}
	public String getHouseaddress() {
		return houseaddress;
	}
	public void setHouseaddress(String houseaddress) {
		this.houseaddress = houseaddress;
	}
	public String getHousezipcode() {
		return housezipcode;
	}
	public void setHousezipcode(String housezipcode) {
		this.housezipcode = housezipcode;
	}
	public String getHouseowner() {
		return houseowner;
	}
	public void setHouseowner(String houseowner) {
		this.houseowner = houseowner;
	}
	public String getMortgageproportion() {
		return mortgageproportion;
	}
	public void setMortgageproportion(String mortgageproportion) {
		this.mortgageproportion = mortgageproportion;
	}
	public String getMortgagetime() {
		return mortgagetime;
	}
	public void setMortgagetime(String mortgagetime) {
		this.mortgagetime = mortgagetime;
	}
	public String getMortgageperson() {
		return mortgageperson;
	}
	public void setMortgageperson(String mortgageperson) {
		this.mortgageperson = mortgageperson;
	}
	public String getMortgagebank() {
		return mortgagebank;
	}
	public void setMortgagebank(String mortgagebank) {
		this.mortgagebank = mortgagebank;
	}
	public String getMoney() {
		return money;
	}
	public void setMoney(String money) {
		this.money = money;
	}
	public String getStaging() {
		return staging;
	}
	public void setStaging(String staging) {
		this.staging = staging;
	}
	public String getRefundWay() {
		return refundWay;
	}
	public void setRefundWay(String refundWay) {
		this.refundWay = refundWay;
	}
	public String getBank() {
		return bank;
	}
	public void setBank(String bank) {
		this.bank = bank;
	}
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	public String getCardno() {
		return cardno;
	}
	public void setCardno(String cardno) {
		this.cardno = cardno;
	}
	public String getRelname() {
		return relname;
	}
	public void setRelname(String relname) {
		this.relname = relname;
	}
	public String getRelation() {
		return relation;
	}
	public void setRelation(String relation) {
		this.relation = relation;
	}
	public String getRelidno() {
		return relidno;
	}
	public void setRelidno(String relidno) {
		this.relidno = relidno;
	}
	public String getRelworkplace() {
		return relworkplace;
	}
	public void setRelworkplace(String relworkplace) {
		this.relworkplace = relworkplace;
	}
	public String getRelworkphone() {
		return relworkphone;
	}
	public void setRelworkphone(String relworkphone) {
		this.relworkphone = relworkphone;
	}
	public String getRelphone() {
		return relphone;
	}
	public void setRelphone(String relphone) {
		this.relphone = relphone;
	}
	public String getRelworknature() {
		return relworknature;
	}
	public void setRelworknature(String relworknature) {
		this.relworknature = relworknature;
	}
	public String getRelpost() {
		return relpost;
	}
	public void setRelpost(String relpost) {
		this.relpost = relpost;
	}
	public String getRelsalary() {
		return relsalary;
	}
	public void setRelsalary(String relsalary) {
		this.relsalary = relsalary;
	}
	public String getRelworkyear() {
		return relworkyear;
	}
	public void setRelworkyear(String relworkyear) {
		this.relworkyear = relworkyear;
	}
	public String getProject() {
		return project;
	}
	public void setProject(String project) {
		this.project = project;
	}
	public String getHandperson() {
		return handperson;
	}
	public void setHandperson(String handperson) {
		this.handperson = handperson;
	}
	public String getHandpersonphone() {
		return handpersonphone;
	}
	public void setHandpersonphone(String handpersonphone) {
		this.handpersonphone = handpersonphone;
	}
	public String getApplcde() {
		return applcde;
	}
	public void setApplcde(String applcde) {
		this.applcde = applcde;
	}
	public String getApplseq() {
		return applseq;
	}
	public void setApplseq(String applseq) {
		this.applseq = applseq;
	}
	public String getFlag() {
		return flag;
	}
	public void setFlag(String flag) {
		this.flag = flag;
	}
	public Integer getPushnum() {
		return pushnum;
	}
	public void setPushnum(Integer pushnum) {
		this.pushnum = pushnum;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getHomeareacode() {
		return homeareacode;
	}
	public void setHomeareacode(String homeareacode) {
		this.homeareacode = homeareacode;
	}
	public String getCompanyareacode() {
		return companyareacode;
	}
	public void setCompanyareacode(String companyareacode) {
		this.companyareacode = companyareacode;
	}
	public String getRelworkareacode() {
		return relworkareacode;
	}
	public void setRelworkareacode(String relworkareacode) {
		this.relworkareacode = relworkareacode;
	}
	public String getCommonflag() {
		return commonflag;
	}
	public void setCommonflag(String commonflag) {
		this.commonflag = commonflag;
	}
	@Override
	public String toString() {
		return "YouzhuMessage [businessno=" + businessno + ", memberno=" + memberno + ", name=" + name + ", gender="
				+ gender + ", age=" + age + ", idno=" + idno + ", phone=" + phone + ", yearsalary=" + yearsalary
				+ ", marriage=" + marriage + ", industrynature=" + industrynature + ", education=" + education
				+ ", monrefamount=" + monrefamount + ", creditcardnum=" + creditcardnum + ", maxcreditlines="
				+ maxcreditlines + ", housemortgage=" + housemortgage + ", houseprice=" + houseprice + ", housearea="
				+ housearea + ", expdecoratecost=" + expdecoratecost + ", actdecoratecost=" + actdecoratecost
				+ ", calculate=" + calculate + ", phone2=" + phone2 + ", homephone=" + homephone + ", children="
				+ children + ", house=" + house + ", houseregister=" + houseregister + ", addressprovince="
				+ addressprovince + ", addresscity=" + addresscity + ", addressdist=" + addressdist + ", address="
				+ address + ", residencetime=" + residencetime + ", zipcode=" + zipcode + ", company=" + company
				+ ", companyaddressprovince=" + companyaddressprovince + ", companyaddresscity=" + companyaddresscity
				+ ", companyaddressdist=" + companyaddressdist + ", companyaddress=" + companyaddress
				+ ", companyphone=" + companyphone + ", enploynature=" + enploynature + ", salary=" + salary + ", post="
				+ post + ", workyear=" + workyear + ", con1name=" + con1name + ", con1relation=" + con1relation
				+ ", con1phone=" + con1phone + ", con1company=" + con1company + ", con1addressprovince="
				+ con1addressprovince + ", con1addresscity=" + con1addresscity + ", con1addressdist=" + con1addressdist
				+ ", con1address=" + con1address + ", con2name=" + con2name + ", con2relation=" + con2relation
				+ ", con2phone=" + con2phone + ", con2company=" + con2company + ", con2addressprovince="
				+ con2addressprovince + ", con2addresscity=" + con2addresscity + ", con2addressdist=" + con2addressdist
				+ ", con2address=" + con2address + ", con3name=" + con3name + ", con3relation=" + con3relation
				+ ", con3phone=" + con3phone + ", con3company=" + con3company + ", con3addressprovince="
				+ con3addressprovince + ", con3addresscity=" + con3addresscity + ", con3addressdist=" + con3addressdist
				+ ", con3address=" + con3address + ", houseaddress=" + houseaddress + ", housezipcode=" + housezipcode
				+ ", houseowner=" + houseowner + ", mortgageproportion=" + mortgageproportion + ", mortgagetime="
				+ mortgagetime + ", mortgageperson=" + mortgageperson + ", mortgagebank=" + mortgagebank + ", money="
				+ money + ", staging=" + staging + ", refundWay=" + refundWay + ", bank=" + bank + ", account="
				+ account + ", cardno=" + cardno + ", relname=" + relname + ", relation=" + relation + ", relidno="
				+ relidno + ", relworkplace=" + relworkplace + ", relworkphone=" + relworkphone + ", relphone="
				+ relphone + ", relworknature=" + relworknature + ", relpost=" + relpost + ", relsalary=" + relsalary
				+ ", relworkyear=" + relworkyear + ", project=" + project + ", handperson=" + handperson
				+ ", handpersonphone=" + handpersonphone + ", applcde=" + applcde + ", applseq=" + applseq + ", flag="
				+ flag + ", pushnum=" + pushnum + ", state=" + state + ", homeareacode=" + homeareacode
				+ ", companyareacode=" + companyareacode + ", relworkareacode=" + relworkareacode + ", commonflag="
				+ commonflag + "]";
	}
	
}
