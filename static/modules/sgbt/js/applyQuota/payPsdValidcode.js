require(['avalon', 'jquery', 'util', 'Const', 'layer'], function(avalon, $, util, Const) {

	//获取手机号码和支付密码
	var param = util.cache();

    vm = avalon.redefine({
        $id: "payPsdValidcode",
        lengths: Const.lengths,
        mobile: param && param.phone,
        agreement: 'true',
        // 显示协议
        showTreatyContent: false,
        showTreatyCover: false,
        sendValidCodeFn: function(event) {
            util.countDown('sendMessageServlet.do', param && param.phone, 60, $(event.currentTarget));
        },
        hideTreatyContent: function() {
            vm.showTreatyContent = false;
            vm.showTreatyCover = true;
        },
        showTreaty: function(flag) {
            /*
             contract 个人借款合同
             credit 个人征信查询授权书
             register 消费信贷协议
             */
            util.post({
                url: 'treatyShowServlet.do',
                data: {
                    flag: flag
                },
                success: function(res) {
                    if (res.retObj) {
                        /// vm.showOrhide();
                        $('#treatyContainer').attr('src', res.retObj).show();
                        vm.showTreatyCover = false;
                        vm.showTreatyContent = true;
                    }
                }
            });
        },
        nextFn: function(event) {
            this.validate.onManual();
        },
        validate: {
            onSuccess: function(reasons) {},
            onError: function(reasons) {},
            onValidateAll: function(reasons) {
                if (!util.isEmpty(reasons)) {
                    util.alert(reasons[0]);
                } else {
                    util.post({
                        url: "edApplyServlet.do",
                        type: 'post',
                        data: { 
                        	password: param && param.password,
                        	verifyNo: vm.verifyNo                      	
                        },
                        success: function(obj) {
                        	util.redirect("applySucc.html");
                        }
                    });
                }
            }
        }
    });
    
    avalon.scan(document.body);
});
