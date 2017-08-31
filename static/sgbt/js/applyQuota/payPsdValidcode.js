require(['jquery', 'util', 'Const', 'bvUpload', 'bvForm'], function($, util, Const) {
    //获取密码
    var param=util.cache('payPasswd');
    //获取额度修改的标识
    var edxg = util.gup('edxg');

    var vm = util.bind({
        container: 'payPsdValidcode',
        data: {
            tags: {
                formKey: 'payPsdValidcodeForm'
            },
            entity: {
                phoneNo: ''
            },
            formConfig: {
                columns: [{
                    head: '请输入手机收到的短信校验码', //'+ (util.format(vm.phoneNo || '', 'phone4'))+'
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
                                desc: '请输入正确格式验证码'
                            }
                        }
                    },
                    operate: {
                        id: 'sendSms',
                        text: '发送验证码',
                        clazz: 'big',
                        click: function(event) {
                            util.countdown($(event.target), {
                                text: '发送验证码',
                                second: 60
                            });
                            util.get({
                                url: '/sendMsg',
                                data: {
                                    phone: vm.phoneNo
                                }
                            });
                        }
                    },
                    agree: {
                        name: 'agree',
                        text: '《海尔消费金融账户开通相关协议》',
                        click: function () {
                            // 多个协议
                            //个人借款合同地  contract
                            // 征信查询credit
                            // 注册协议register
                            // 个人信息协议person

                            util.actions([
                                {
                                    text: '消费信贷协议',
                                    click: function () {
                                        util.post({
                                            url: '/treatyShow',
                                            data: {
                                                flag: 'register'
                                            },
                                            success: function (res) {
                                                var data = util.data(res);
                                                if (data.realmName) {
                                                    util.popup({
                                                        $element: $('.agree-popup'),
                                                        title: '消费信贷协议',
                                                        url: data.realmName     // '/app/ht/agreement/PersonInfo.html'
                                                    });
                                                }
                                            }
                                        });
                                    }
                                }, {
                                    text: '个人征信查询授权书',
                                    click: function () {
                                        util.post({
                                            url: '/treatyShow',
                                            data: {
                                                flag: 'credit'
                                            },
                                            success: function (res) {
                                                var data = util.data(res);
                                                if (data.realmName) {
                                                    util.popup({
                                                        $element: $('.agree-popup'),
                                                        title: '个人征信查询授权书',
                                                        url: data.realmName     // '/app/ht/agreement/PersonInfo.html'
                                                    });
                                                }
                                            }
                                        });
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
                                    util.alert('#agreement');
                                    return false;
                                }
                                return true;
                            }
                        },
                        click: function (event, editType, entity) {
                            util.post({
                                url: '/resetPayPasswd',
                                data:{
                                    payPasswd: param && param.payPasswd,
                                    verifyNo: entity.verifyNo,
                                    edxgflag: edxg,
                                },
                                success:function(res){
                                    util.redirect({
                                        // title: '额度输出中',
                                        url: '/applyQuota/applyIn.html',
                                        back: false
                                    });
                                }
                            });
                        }
                    }
                ]
            }
        },
        mounted: function(){
            util.get({
                url: '/getPhoneNo',
                success: function(res) {
                    var data = util.data(res);
                    if (data && data.phoneNo) {
                        vm.phoneNo = data.phoneNo ;
                        util.refresh({
                            vm: util.vm(vm, vm.tags.formKey),
                            column: {
                                name: 'verifyNo',
                                head: '请输入手机' + util.format(vm.phoneNo, 'phone4') + '收到的短信校验码'
                            }
                        });
                    }
                    // vm.formConfig.columns[0].head = util.data(res).phoneNo;
                }
            });
        }
    });
});