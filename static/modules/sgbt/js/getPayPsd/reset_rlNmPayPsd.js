require(['avalon', 'jquery', 'util', 'Const', 'layer'], function(avalon, $, util, Const) {
	var param = util.cache('cardNo,bankCode,mobile,verifyNo');

    var vm = avalon.redefine({
        $id: "reset_rlNmPayPsd",
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
                } else {
                	util.post({
                        url: 'updatePayPwdIdentityServlet.do',
                        data: {
                    		cardNo: param && param.cardNo,
                    		bankCode: param && param.bankCode,
                    		mobile: param && param.mobile,
                    		verifyNo: param && param.verifyNo,
                    		newpassword: vm.payPassword
                        },
                        success: function(res) {
                        	util.redirect('enterPayPsd.html');
                        }
                    });
                }
            }
        }
    });
    avalon.scan(document.body);
});
