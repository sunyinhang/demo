require(['avalon', 'jquery', 'util', 'Const', 'layer'], function(avalon, $, util, Const) {
	//获取手机号
	var param = util.cache('mobile,cardNo,bankCode');

	var vm=avalon.redefine({
		$id: "byRlNmCertif_PayPsdVldcode",
		lengths: Const.lengths,
		mobile: param && param.mobile,
		agreement: true,
		sendValidCodeFn: function (event){
			util.countDown('sendMsgServlet.do', {userId: param && param.mobile}, 60, $(event.currentTarget));
		},
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
                		bankCode: param && param.bankCode,
                		mobile: param && param.mobile,
                		verifyNo: vm.verifyNo
                	});
                	util.redirect("getPayPsd/reset_rlNmPayPsd.html");
                }
            }
        }

	});

	avalon.scan(document.body);
});