require(['jquery', 'util', 'Const', 'bvUpload', 'bvForm'], function($, util, Const) {
    //获取持卡人姓名
    var param = util.cache('name');

    var vm = util.bind({
        container: 'tiedBnkCrd',
        data: {
            tags: {
                formKey: 'tiedBnkCrdForm'
            },
            entity: {
                cardholder: param.name
            },
            formConfig: {
                layout: 'inline',
                columns: [
                    {
                        head: '持卡人',
                        name: 'cardholder',
                        config: {
                            value: param && param.name,
                            attr: {
                                readonly: 'readonly'
                            },
                            validate: {
                                required: '持卡人不能为空'
                            }
                        },
                        operate: {
                            type: 'hint',
                            click: function () {
                                util.alert('#cardholder');
                            }
                        }
                    },{
                        head: '卡号',
                        name: 'cardnumber',
                        config: {
                            attr: {
                                placeholder: '请输入卡号',
                                maxlength: '#bankCard'
                            },
                            validate: {
                                required: '请输入银行卡卡号',
                                custom: {
                                    code: 'cardNo',
                                    desc: '请输入16位或19位卡号'
                                }
                            },
                            blur: function (event, val, entity) {
                                var cardNumReg=/^([0-9]{16}|[0-9]{19})$/;//卡号正则
                                if(cardNumReg.exec(entity.cardnumber)==null){
                                    util.alert('请输入16位或19位卡号');
                                }else{
                                    util.get({
                                        url: util.mix("/getCardInfo", {
                                            cardNo: entity.cardnumber
                                        }),
                                        success:function(res){
                                            util.refresh({
                                                vm: util.vm(vm, vm.tags.formKey),
                                                entity: {
                                                    cardtype: res.body.bankName+res.body.cardType
                                                }
                                            });
                                        }
                                    });
                                }
                            }
                        }
                    },{
                        head: '卡类型',
                        name: 'cardtype',
                        config: {
                            attr: {
                                readonly: 'readonly',
                            },
                            validate: {
                                required: '卡类型不能为空'
                            },operate: {
                                click: function (event,entity) {
                                    if (util.isEmpty(entity.cardnumber)) {
                                        util.alert('请先输入银行卡号');
                                    }
                                }
                            }
                        },
                        operate: {
                            type: 'hint',
                            click: function () {
                                util.alert('#cardtype');
                            }
                        }
                    },{
                        head: '开户省市',
                        name: 'account',
                        edit: {
                            type: 'picker'
                        },
                        config: {
                            attr: {
                                placeholder: '请选择开户省市'
                            },
                            validate: {
                                required: '开户省市不能为空'
                            },
                            code: 'areaCode',
                            desc: 'areaName',
                            // cache: true,
                            initOption: {
                                areaCode: '',
                                areaName: '请选择'
                            },
                            choose: [
                                {
                                    items: [],
                                    onInit: function () {
                                        var _vm = this;
                                        app.getArea('', function (items) {
                                            _vm.changeItems(0, items);
                                        });
                                    },
                                    onChange: function (value, values) {
                                        if (util.isEmpty(value)) {
                                            this.changeItems(1, []);
                                        } else {
                                            var _vm = this;
                                            app.getArea(value, function (items) {
                                                _vm.changeItems(1, items);
                                            });
                                        }
                                    }
                                },
                                {
                                    items: [],
                                    onChange: function (value, values) {
                                        if (util.isEmpty(value)) {
                                            this.changeItems(2, []);
                                        } else {
                                            var _vm = this;
                                            app.getArea(value, function (items) {
                                                _vm.changeItems(2, items);
                                            });
                                        }
                                    }
                                },
                                {
                                    items: []
                                }
                            ]
                        }
                    },{
                        head: '预留手机号',
                        name: 'mobile',
                        config: {
                            attr: {
                                placeholder: '请输入银行预留手机号',
                                maxlength: '#mobile'
                            },
                            validate: {
                                required: '请输入银行预留手机号',
                                custom: {
                                    code: 'mobile',
                                    desc: '请输入正确的手机号码'
                                }
                            }
                        }
                    },{
                        head: '验证码',
                        name: 'verifyNo',
                        config: {
                            attr: {
                                placeholder: '请输入验证码',
                                maxlength: '#verifyNo'
                            },
                            validate: {
                                required: '请输入验证码',
                                minSize: {
                                    code: 6,
                                    desc: '请输入正确格式的验证码'
                                }
                            }
                        },
                        operate: {
                            text: '获取验证码',
                            click: function (event, entity) {
                                if (!util.validate($('#cardnumber', vm.$el))) {
                                    return;
                                }
                                if (!util.validate($('#mobile', vm.$el))) {
                                    return;
                                }
                                util.countdown($(event.target), {
                                    text: '获取验证码',
                                    second: 60
                                });
                                util.get({
                                    url: util.mix('/sendMsg',{
                                        phone: entity.mobile
                                    })
                                });
                            }
                        },
                        hint: '#card'
                    }
                ],
                operates: [
                    {
                        text: '下一步',
                        layout: 'primary',
                        click: function (event, editType, entity) {
                            util.post({
                                url: '/realAuthentication',
                                data:{
                                    cityCode: entity.account,
                                    cardnumber: entity.cardnumber,
                                    mobile: entity.mobile,
                                    verifyNo: entity.verifyNo,
                                },
                                success:function(res){
                                    util.cache({
                                        mobile: entity.mobile
                                    });
                                    util.redirect({
                                        title: '个人资料',
                                        url: '/applyQuota/personalInform.html',
                                        back: false
                                    });
                                }
                            });
                        }
                    }
                ]
            }
        }
    });
});