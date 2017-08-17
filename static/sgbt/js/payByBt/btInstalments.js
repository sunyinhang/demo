require(['jquery', 'util', 'Const', 'bvLayout'], function($, util, Const) {
    var vm = util.bind({
        container: 'btInstalment',
        methods: {
            chooseCoupon: function () {
                util.popup({
                    $element: $('#popup1', vm.$el)
                });
            },
            payFn: function () {
                util.modal({
                    title: '请输入支付密码',
                    message: '<div class="enretPwd-c"><input type="text" placeholder="请输入支付密码" class="pwd-text"></div>',
                    inline: false,
                    operates: [
                        {
                            text: '确认支付',
                            click: function () {

                            }
                        },
                        {
                            text: '忘记密码',
                            click: function () {

                            }
                        }
                    ]
                });
            }
        }
    });
});