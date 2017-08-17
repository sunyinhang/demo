require(['jquery', 'util', 'Const', 'bvUpload', 'bvForm'], function($, util, Const) {
    //获取手机号
    var param = util.cache('cardnumber','mobile','bankNo');

    var vm = util.bind({
        container: 'byRlNmCertif_PayPsdVldcode',
        data: {
            tags: {
                formKey: 'byRlNmCertif_PayPsdVldcodeForm'
            },
            entity: {
            },
            formConfig: {
                columns: [{
                    head: '请输入'+ (param && (util.format(param.mobile || '', 'phone4')))+ '手机收到的短信校验码',
                    name: 'verifyNo',
                    clazz: 'bv-align-left',
                    config: {
                        attr: {
                            placeholder: '请输入验证码',
                            maxlength: '#verifyNo'
                        },
                        validate: {
                            required: '请输入短信验证码',
                            minSize: {
                                code: 6,
                                desc: '请输入正确位数的验证码'
                            }
                        }
                    },
                    operate: {
                        id: 'sendSms',
                        text: '发送验证码',
                        click: function(event) {
                            util.countdown($(event.target), {
                                text: '发送验证码',
                                second: 60
                            });
                            util.get({
                                url: '/sendMsg',
                                data: {
                                    phone: param && param.mobile
                                }
                            });
                        }
                    },
                    agree: {
                        name: 'agree',
                        text: '海尔消费金融账户开通相关协议',
                        click: function () {
                            // 多个协议
                            util.actions([
                                {
                                    text: '协议1',
                                    click: function () {
                                        util.popup({
                                            $element: $('.agree-popup'),
                                            title: '协议1',
                                            url: 'https://www.baidu.com'
                                        });
                                    }
                                }, {
                                    text: '协议2',
                                    click: function () {
                                        console.log('协议2');
                                    }
                                }, {
                                    text: '协议3',
                                    click: function () {
                                        console.log('协议3');
                                    }
                                }
                            ]);
                        }
                    }
                }],
                operates: [
                    {
                        text: '下一步',
                        layout: 'primary',
                        validate: {
                            after: function (event, editType, entity) {
                                if (!entity.agree) {
                                    util.alert('#agree');
                                    return false;
                                }
                                return true;
                            }
                        },
                        click: function (event, editType, entity) {
                            util.cache({
                                verifyNo: entity.verifyNo,
                                mobile: param && param.mobile,
                                cardnumber: param && param.cardnumber,
                                bankNo: param && param.bankNo
                            });
                            util.redirect({
                                title: '实名认证找回密码',
                                url: '/getPayPsd/reset_rlNmPayPsd.html',
                                back: false
                            });
                        }
                    }
                ]
            }
        }
    });
});