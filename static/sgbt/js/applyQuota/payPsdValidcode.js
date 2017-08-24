require(['jquery', 'util', 'Const', 'bvUpload', 'bvForm'], function($, util, Const) {
    //获取密码
    var param=util.cache('payPasswd','edxgflag');

    var vm = util.bind({
        container: 'payPsdValidcode',
        data: {
            tags: {
                formKey: 'payPsdValidcodeForm'
            },
            entity: {
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
                            util.post({
                                url: '/resetPayPasswd',
                                data:{
                                    payPasswd: param && param.payPasswd,
                                    verifyNo: entity.verifyNo,
                                    edxgflag: param && param.edxgflag,
                                },
                                success:function(res){
                                    util.redirect({
                                        title: '额度输出中',
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
                        util.refresh({
                            vm: util.vm(vm, vm.tags.formKey),
                            column: {
                                name: 'verifyNo',
                                head: '请输入手机' + util.format(data.phoneNo, 'phone4') + '收到的短信校验码'
                            }
                        });
                    }
                    // vm.formConfig.columns[0].head = util.data(res).phoneNo;
                }
            });
        }
    });
});