require(['jquery', 'util', 'Const','bvForm'], function($, util, Const) {
    //获取旧密码
    var param=util.cache('payPasswd');

    var vm = util.bind({
        container: 'reset_byVldcode',
        data: {
            tags: {
                formKey: 'reset_byVldcodeForm'
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
                                maxlength: '#password'
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
                                }
                            });
                        }
                    }
                ]
            }
        }
    });
});