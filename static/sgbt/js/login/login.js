require(['jquery', 'util', 'Const', 'bvUpload', 'bvForm'], function($, util, Const) {
    var vm = util.bind({
        container: 'login',
        data: {
            accountname:'',
            loginpassword:''
        },
        methods: {
            loginFn: function(){
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
                                url: data.backurl
                            });
                        }
                    }
                });
            }
        }

    });
});