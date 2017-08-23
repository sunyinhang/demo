require(['jquery', 'util', 'Const', 'bvUpload', 'bvForm'], function($, util, Const) {

    var vm = util.bind({
        container: 'setPayPsd',
        data: {
            tags: {
                formKey: 'setPayPsdForm'
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
                                placeholder: '6-20位字母和数字组成',
                                maxlength: '#payPassword'
                            },
                            validate: {
                                required: '请输入支付密码',
                                custom: {
                                    code: 'password',
                                    desc: '密码由6-20位字母和数字组成'
                                }
                            }
                        }
                    },{
                        head: '确认密码',
                        name: 'payPassword2',
                        config: {
                            type: 'password',
                            attr: {
                                placeholder: '请输入确认密码'
                            },
                            validate: {
                                required: '请输入确认密码',
                                equals:{
                                    code: 'payPassword',
                                    desc: '两次输入密码不一致'
                                }
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
                                payPasswd: entity.payPassword
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