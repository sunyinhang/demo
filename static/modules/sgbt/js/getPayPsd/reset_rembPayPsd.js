require(['avalon', 'jquery', 'util', 'Const', 'layer'], function(avalon, $, util, Const) {
	
	//获取原密码
	var param = util.cache('oldpassword');

    var vm = avalon.redefine({
        $id: "reset_rembPayPsd",
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
                if (reasons.length) {
                	util.alert(reasons[0]);
                } else {
                   	util.post({
 						url: 'updatePayPasswdServlet.do',
 						data: {
 							oldpassword: param && param.oldpassword,
 							newpassword: vm.payPassword
 							
 						},
 						success:function(res){
 							util.redirect("enterPayPsd.html");
 						}
 					});
                	
                }
            }
        }
    });
    avalon.scan(document.body);
});
