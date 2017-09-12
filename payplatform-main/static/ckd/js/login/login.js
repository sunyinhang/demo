require(['jquery', 'util', 'Const', 'bvUpload', 'bvForm'], function($, util, Const) {
    var vm = util.bind({
        container: 'login',
        data: {
            accountname:'',
            loginpassword:''
        },
        methods: {
            loginFn: function(){
                if(util.isEmpty(vm.accountname)){
                    util.alert('账号不能为空');
                }else if(util.isEmpty(vm.loginpassword)){
                    util.alert('密码不能为空');
                }else{
                    util.post({
                        url: "/shunguang/userlogin",
                        data: {
                            userId: this.accountname,
                            password: this.loginpassword
                        },
                        success: function (res) {
                            var data = util.data(res);
                            if(!util.isEmpty(data)){
                                util.redirect({
                                    url: data.backurl.substring(data.backurl.indexOf('/#!/') + 3)
                                });
                            }
                        }
                    });
                }
            }
        }

    });
});