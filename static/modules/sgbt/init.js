var __baseResourcePath = '../../mobile';
var __baseHtmlPath = 'html/';
function __heredoc(fn) {
    return fn.toString().replace(/^[^\/]+\/\*!?\s?/, '').replace(/\*\/[^\/]+$/, '').trim().replace(/>\s*</g, '><');
}
var __Const = {
    rest: {
        baseUrl: window.location.protocol + "//" + window.location.host + '/PaymentPlatform/servlet/meifenqi/'
    },
    params: {
        maritalStatusMarried: '20',
        relationTypeCouple: '06',
		brClientId: '3000425',	// 测试'3000425' 生产'3000424'
		baiduHm: '7e18821d82be100f1703d479460cdb79'	// 生产 f4144477da2d5745fc0a1dc12b2631bb 测试 7e18821d82be100f1703d479460cdb79，此处暂不用，在index.html中配置
    },
    dicts: {
        education: {
            '00': '硕士及以上',
            '10': '本科',
            '20': '大专',
            '30': '高中',
            '40': '初中及以下'
        },
        maritalStatus: {
            '10': '未婚',
            '20': '已婚',
            '40': '离异',
            '50': '丧偶'
        },
        relationType: {
            '01': '父母',
            '02': '子女及兄弟姐妹',
            '06': '夫妻',
            '99': '其他'
        }
    },
    titles: {      
        'applyQuota/checkIdCard.html': '实名绑卡',
        'applyQuota/checkIdCardB.html': '实名绑卡',
        'applyQuota/tiedBankCard.html': '实名绑卡',
        'applyQuota/personalInform.html': '个人资料',
        'applyQuota/identityVrfic.html': '人脸识别',
        'applyQuota/setPayPsd.html': '密码设置',
        'applyQuota/payPsdValidcode.html': '密码设置',        
        
        'applyQuota/amountNot.html':'我的额度',
        'applyQuota/amountFail.html':'我的额度',
        'applyQuota/applyIn.html':'我的额度',
        'applyQuota/applyReturn.html':'我的额度',
        'applyQuota/myAmount.html':'我的额度',
        'applyQuota/myAmountView.html':'我的额度',
        'applyQuota/applyProgress.html': '审批进度',      
        
        
        'payByBt/btInstalments.html': '顺逛白条支付分期',
        'payByBt/paySuccess.html': '支付成功',
        'payByBt/payFail.html': '支付失败',
        'payByBt/loanDetails.html': '订单详情',
        
        //getPayPsdWay/找回支付密码
        'getPayPsd/getPayPsdWay.html': '重置支付密码',
        'getPayPsd/byRembPayPsd.html': '输入原密码',
        'getPayPsd/reset_rembPayPsd.html': '设置支付密码',
        
        'getPayPsd/byRlNmCertif_PayPsd.html': '实名认证找回密码',
        'getPayPsd/byRlNmCertif_PayPsdC.html': '实名认证找回密码',
        'getPayPsd/byRlNmCertif_PayPsdVldcode.html': '实名认证找回密码',
        'getPayPsd/reset_rlNmPayPsd.html': '设置支付密码',
      	
    },
    messages: {
		notRegistered: '您的手机号尚未注册',
        requireLocation: '美分期想要获取您的地理位置',
        locationFail: '获取地理位置失败，无法继续操作，请确认GPS定位是否打开或网络是否正常！',
        noHospital: '定位不到医院',
        noProject: '医院尚未开通',
		noSubProject: '医院尚未开通',
        noLoan: '没有合适的贷款品种',
        applyMoneyError: '请输入正确的分期金额，最高5万，小数点最长2位。',
        supportedBank: '由于银行扣款要求，现已支持中国工商银行，中国邮政储蓄银行，中国农业银行，中国银行，中国建设银行，广东发展银行，兴业银行，  招商银行，交通银行，中信银行，中国光大银行，华夏银行，中国民生银行，平安银行，上海浦东发展银行，北京银行，上海银行，青岛银行',
        cardHolderBind: '为了您的账户安全，请您绑定本人银行卡',
        bankMobile: '银行预留手机号码是办理该银行卡时所填写的手机号码。没有预留、手机号码忘记或者已停用，请联系银行客服进行处理',
		toAuth1: '请先做运营商认证',
		toAuth2: '请先做网银或公积金认证',
		faceIdent:'请完成人脸自拍'
    },
    lengths: {
        userId: 11,
        name: 90,
        mobile: 11,
        phone: 30,
        password: 20,
        payPassword: 20,
        verifyNo: 6,
        // 工作单位
        officeName: 300,
        // 所在部门
        officeDept: 300,
        // 地址
        address: 300,
        // 分期金额
        applyMoney: 8,
        // 银行卡号
        bankCard: 19,
        idCard: 60,
        // 发证机关
        issue: 90
    },
    templates: {
        hospitalChooseTemplate: __heredoc(function() {
		/*!
		<div class="wrapper">
			<div id="body">
				<div class="kong32"></div>
				<div class="b-card1">
					<ul>
						<li>
							<div class="li-inner li-border cursorSty">
								<div class="li-l">
									<span class="label-title leftlocation">
										所在位置
									</span>
								</div>
								<a href="javascript:void(0)" class="li-r">
									<span class="label-content">${location}</span>
									<span class="small-text"></span>
								</a>
							</div>
						</li>
						${hospitals}
					</ul>
				</div>
			</div>
		</div>
		 */
	    }),
        projectChooseTemplate: __heredoc(function() {
		/*
		<div class="wrapper">
			<div id="body">
				<div class="kong32"></div>
				<div class="b-card1">
					<ul>
		 				${projects}
					</ul>
				</div>
		</div>
		 */
	    }),
        subProjectChooseTemplate: __heredoc(function() {
		/*
		<div class="wrapper">
			<div id="body">
				<div class="kong32"></div>
				<div class="b-card1">
					<ul>
						<li class="subprojectBack"><a href="javascript:;" id='back'>&nbsp;返回</a></li>
						${subProjects}
					</ul>
				</div>
		</div>
		*/
	    }),
        loanChooseTemplate: __heredoc(function() {
		/*
		<div class="wrapper">
			<div id="body">
				<div class="kong32"></div>
				<div class="b-card1">
					<ul>
						${loans}
					</ul>
				</div>
			</div>
		</div>
		 */
	    }),
        optChooseTemplate: __heredoc(function() {
		/*
		<div class="wrapper">
			<div id="body">
				<div class="kong32"></div>
				<div class="b-card1">
					<ul>
						${opts}
					</ul>
				</div>
			</div>
		</div>
		 */
	    }),
        payPlanTemplate: __heredoc(function() {
		/*
		<div class="wrapper">
			<div class="topBlueTips">此还款计划为还款试算，实际还款计划以实际放款后的还款计划为准</div>
			<div class="paydate">每月12日为固定还款日</div>
			<div class="whiteBody2">
				${pays}
				<div class="clear"></div>
			</div>
			<div class="payPlanTips">
					按月还本付费产品简介：<br>
					可借固定期限，每月归还相等的本金和手续费，可提前还款，但需支付剩余本金*1%的提前手续费
				 <div class="botmLine2"></div>
				   逾期费用说明：<br>
				   如果您在选定期限内没有归还贷款，每天按逾期金额的0.1%收取违约金，并按逾期金额的5%计收滞纳金，按逾期次数加收，最低人民币30元。
			</div>
			<div id="footer" style="position:relative;">
				<div class="footer-inner">
					<div class="footer-l">
						<p class="interest">含<span class="money">${totalFees}</span>息费</p>
						<p class="total"><span class="s-label">还款总额</span><span class="money">${repaymentTotalAmt}</span></p>
					</div>
					<div class="footer-r">
						<a href="javascript:void(0);" onclick="layer.closeAll()">知道了</a>
					</div>
				</div>
			</div>
		</div>
		 */
	    })
    }
};
