require(['avalon', 'jquery', 'util', 'Const', 'layer'], function(avalon, $, util, Const) {

    var vm = avalon.redefine({
        $id: "byRembPayPsd",
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
                	util.cache({
                		oldpassword: vm.payPassword
                	});
                    util.redirect('getPayPsd/reset_rembPayPsd.html');
                }
            }
        }
    });

    avalon.scan(document.body);
});