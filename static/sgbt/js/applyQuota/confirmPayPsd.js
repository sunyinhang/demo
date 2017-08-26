require(['jquery', 'util', 'Const', 'bvUpload', 'bvForm'], function($, util, Const) {
    //获取手机号
    var param=util.cache('mobile');

    var vm = util.bind({
        container: 'confirmPayPsd',
        data: {
            tags: {
                formKey: 'confirmPayPsdForm'
            },
            entity: {},
            formConfig: {
                layout: 'inline',
                attr:{

                },
                columns: [
                    {
                        head: '支付密码',
                        name: 'payPassword',
                        config: {
                            type: 'password',
                            attr: {
                                placeholder: '6-20位字母和数字组成'
                            },
                            validate: {
                                required: '请输入支付密码',
                                custom: {
                                    code: 'password',
                                    desc: '密码由6-20位字母和数字组成'
                                }
                            }
                        },
                        operate: {
                            position: 'bottom',
                            text: '忘记密码？',
                            click: function () {
                                util.redirect({
                                    title: '重置支付密码',
                                    url: '/getPayPsd/getPayPsdWay.html',
                                    back: false
                                });
                            }
                        }
                    }
                ],
                operates: [
                    {
                        text: '下一步',
                        layout: 'primary',
                        click: function (event, editType, entity) {
                            util.cache({
                                mobile: param && param.mobile,
                                payPasswd: entity.payPassword,
                                edxgflag: '1'
                            });
                            util.redirect({
                                title: '短信验证码',
                                url: '/applyQuota/payPsdValidcode.html',
                                back: false
                            });
                        }
                    }
                ]
            }
        }
    });
});