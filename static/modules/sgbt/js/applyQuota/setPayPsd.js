require(['avalon', 'jquery', 'util', 'Const', 'layer'], function(avalon, $, util, Const) {
	
	//获取手机号码
	var param = util.cache();

    var vm = avalon.redefine({
        $id: "setPayPsd",
        lengths: Const.lengths,

        nextFn: function(event) {
            this.validate.onManual();
        },
        validate: {
            onSuccess: function(reasons) {
            },
            onError: function(reasons) {
            },
            onValidateAll: function(reasons) {
            	if (!util.isEmpty(reasons)) {
                    util.alert(reasons[0]);
                }else {
					util.cache({
						password: vm.payPassword,
						phone: param && param.phone
					});
                	util.redirect('applyQuota/payPsdValidcode.html');
                }
            }
        }
    });
    avalon.scan(document.body);
});
