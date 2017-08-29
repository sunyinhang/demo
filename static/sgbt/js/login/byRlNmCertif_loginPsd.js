require(['jquery', 'util', 'Const', 'bvUpload', 'bvForm'], function($, util, Const) {
    var vm = util.bind({
        container: 'byRlNmCertif_loginPsd',
        data: {
            tags: {
                formKey: 'byRlNmCertif_loginPsdForm'
            },
            entity: {},
            formConfig: {
                layout: 'inline',
                columns: [
                    {
                        head: '姓名',
                        name: 'cardholder',
                        clazz: '',
                        config: {
                            attr: {
                                placeholder: '请输入姓名',
                                maxlength: '#name'
                            },
                            validate: {
                                required: '姓名不能为空',
                            }
                        }
                    },{
                        head: '身份证',
                        name: 'cardID',
                        config: {
                            attr: {
                                placeholder: '请输入身份证',
                                maxlength: '#idCardNo'
                            },
                            validate: {
                                required: '身份证号不能为空',
                                custom: {
                                    code: 'idCardNumber',
                                    desc: '请输入正确的身份证号'
                                }
                            }
                        }
                    },{
                        head: '银行卡号',
                        name: 'cardnumber',
                        clazz: '',
                        config: {
                            attr: {
                                placeholder: '16位或19位银行卡卡号',
                                maxlength: '#bankCard'
                            },
                            validate: {
                                required: '请输入银行卡号',
                                custom: {
                                    code: 'cardNo',
                                    desc: '请输入16位或19位卡号'
                                }
                            }
                        },
                        operate: {
                            type: 'hint',
                            click: function () {
                                util.alert('#cardSupport');
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
                                cardnumber: entity.cardnumber
                            });
                            util.redirect({
                                // title: '实名认证找回密码',
                                url: '/login/byRlNmCertif_loginPsdC.html'
                            });
                        }
                    }
                ]
            }
        }
    });
});