require(['avalon', 'jquery', 'util', 'Const', 'layer'], function(avalon, $, util, Const) {
	//获取姓名和身份证 
	// var param = util.cache('');
	
	var vm=avalon.redefine({
		$id: "byRlNmCertif_PayPsd",
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
                		cardNo: vm.cardnumber
                	});
                    util.redirect('getPayPsd/byRlNmCertif_PayPsdC.html');
                }
            }
		}
	});

	avalon.scan(document.body);
});