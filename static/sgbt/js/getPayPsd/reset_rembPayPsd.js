require(['jquery', 'util', 'Const','bvForm'], function($, util, Const) {
    //获取旧密码
    var param=util.cache('payPasswd');

    var vm = util.bind({
        container: 'reset_rembPayPsd',
        data: {
            tags: {
                formKey: 'reset_rembPayPsdForm'
            },
            entity: {},
            formConfig: {
                layout: 'inline',
                columns: [
                    {
                        head: '输入新密码',
                        name: 'payPassword',
                        config: {
                            type: 'password',
                            attr: {
                                placeholder: '6-20位字母和数字组成',
                                maxSize: '#payPassword'

                            },
                            validate: {
                                required: '密码不能为空',
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
                        text: '确定',
                        layout: 'primary',
                        click: function (event, editType, entity) {
                            util.post({
                                url: '/updatePayPasswd',
                                data: {
                                    oldpassword: param && param.payPasswd,
                                    newpassword: entity.payPassword
                                },
                                success: function (res){
                                    util.alert('重置成功');
                                    // from
                                    // confirmPayPsd
                                    // btInstalments
                                    var from = util.gup('from');
                                    if (from) {
                                        var url;
                                        // var title;
                                        if (from === 'confirmPayPsd') {
                                            // title = '验证支付密码';
                                            url = '/applyQuota/confirmPayPsd.html';
                                        } else if (from === 'btInstalments') {
                                            // title = '白条支付';
                                            url = '/payByBt/btInstalments.html';
                                        }
                                        util.redirect({
                                            // title: title,
                                            url: util.mix(url, {
                                                from: 'reset',
                                                edxg: util.gup('edxg')
                                            }, true)
                                        });
                                    }
                                }
                            });
                        }
                    }
                ]
            }
        }
    });
});