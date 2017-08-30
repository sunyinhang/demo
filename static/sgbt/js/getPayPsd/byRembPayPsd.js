require(['jquery', 'util', 'Const', 'bvUpload', 'bvForm'], function($, util, Const) {
    var vm = util.bind({
        container: 'byRembPayPsd',
        data: {
            tags: {
                formKey: 'byRembPayPsdForm'
            },
            entity: {},
            formConfig: {
                layout: 'inline',
                columns: [
                    {
                        head: '输入原密码',
                        name: 'payPassword',
                        clazz: '',
                        config: {
                            type: 'password',
                            attr: {
                                placeholder: '6-20位字母，数字组合',
                                maxlength: 'payPassword'
                            },
                            validate: {
                                required: '密码不能为空',
                                custom: {
                                    code: 'password',
                                    desc: '密码由6-20位字母和数字组成'
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
                                // title: '设置支付密码',
                                url: util.mix('/getPayPsd/reset_rembPayPsd.html', {
                                    from: util.gup('from'),
                                    edxg: util.gup('edxg')
                                }, true)
                            });
                        }
                    }
                ]
            }
        }
    });
});