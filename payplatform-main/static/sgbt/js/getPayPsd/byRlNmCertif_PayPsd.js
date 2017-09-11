require(['jquery', 'util', 'Const', 'bvUpload', 'bvForm'], function($, util, Const) {
    var data = '';
    var vm = util.bind({
        container: 'byRlNmCertif_PayPsd',
        data: {
            tags: {
                formKey: 'byRlNmCertif_PayPsdForm'
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
                            value:'',
                            attr: {
                                readonly: 'readonly'
                            },
                            validate: {
                                required: '姓名不能为空'
                            }
                        }
                    },{
                        head: '身份证',
                        name: 'cardID',
                        config: {
                            value: data && data.idNo,
                            attr: {
                                readonly: 'readonly'
                            },
                            validate: {
                                required: '身份证号不能为空'
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
                                url: util.mix('/getPayPsd/byRlNmCertif_PayPsdC.html', {
                                    from: util.gup('from'),
                                    orderNo: util.gup('orderNo'),
                                    edxg: util.gup('edxg')
                                }, true)
                            });
                        }
                    }
                ]
            }
        },
        mounted: function(){
            util.get({
                 url: '/queryCustNameByUId',
                 success: function(res){
                     data = util.data(res);
                     if (data && data.name) {
                         util.refresh({
                             vm: util.vm(vm, vm.tags.formKey),
                             entity: {
                                 cardholder: data.name
                             }
                         });
                     }
                     if (data && data.idNo) {
                         util.refresh({
                             vm: util.vm(vm, vm.tags.formKey),
                             entity: {
                                 cardID: data.idNo
                             }
                         });
                     }
                 }
             });
        }
    });
});