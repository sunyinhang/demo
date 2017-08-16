require(['avalon', 'jquery', 'util', 'Const', 'layer'], function(avalon, $, util, Const) {
	//获取卡号
	var param = util.cache('cardNo');
	
	//获取卡类型
	util.post({
		url: 'getCardInfoServlet.do',
		data: {
			cardNo: param && param.cardNo
		},
		success: function(res) {
			vm.cardType = res.retObj.bankName+" "+res.retObj.cardType;
			vm.bankCode=res.retObj.bankNo;
		}
	});
	
	var vm=avalon.redefine({
		$id: "byRlNmCertif_PayPsdC",
		bankCode: '',
		lengths: Const.lengths,
		nextFn: function(event){
			this.validate.onManual();
		},
		validate: {
            onSuccess: function (reasons) {
            },
            onError: function (reasons) {
            },
            onValidateAll: function (reasons) {
                if (!util.isEmpty(reasons)) {
                    util.alert(reasons[0]);
                }else {
                	util.cache({
                		cardNo: param && param.cardNo,
                		bankCode: vm.bankCode,
                		mobile: vm.mobile
                	});
                	util.redirect("getPayPsd/byRlNmCertif_loginPsdVldcode.html");
                }
            }
        }

	});
	avalon.scan(document.body);
});