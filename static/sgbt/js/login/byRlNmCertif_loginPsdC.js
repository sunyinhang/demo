require(['jquery', 'util', 'Const', 'bvUpload', 'bvForm'], function($, util, Const) {
    //获取银行卡号
    var param=util.cache('cardnumber');

    var vm = util.bind({
        container: 'byRlNmCertif_loginPsdC',
        data: {
            tags: {
                formKey: 'byRlNmCertif_loginPsdCForm'
            },
            entity: {},
            formConfig: {
                layout: 'inline',
                columns: [
                    {
                        head: '卡类型',
                        name: 'cardType',
                        clazz: '',
                        config: {
                            attr: {
                                readonly: 'readonly'
                            },
                            validate: {
                                // TODO: 确认一下非必填
                                //required: '银行卡类型不能为空'
                            }
                        }
                    },{
                        head: '手机号',
                        name: 'mobile',
                        clazz: '',
                        config: {
                            attr: {
                                placeholder: '请输入银行卡绑定手机号',
                                maxlength: '#mobile'

                            },
                            validate: {
                                required: '手机号不能为空',
                                custom: {
                                    code: 'mobile',
                                    desc: '请输入正确的手机号码'
                                }
                            }
                        },
                        operate: {
                            type: 'hint',
                            click: function () {
                                util.alert('#reservedMobile');
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
                                mobile: entity.mobile,
                                cardnumber: param && param.cardnumber,
                                bankNo: entity.bankNo
                            });
                            util.redirect({
                                // title: '实名认证找回密码',
                                url: '/login/byRlNmCertif_loginPsdVldcode.html'
                            });
                        }
                    }
                ]
            }
        }
    });
    util.get({
        url: util.mix("/getCardInfo", {
            cardNo: param && param.cardnumber,
        }),
        success:function(res){
            util.refresh({
                vm: util.vm(vm, vm.tags.formKey),
                entity: {
                    cardType: res.body.bankName+res.body.cardType,
                    bankNo: res.body.bankNo
                }
            });
        }
    });
});