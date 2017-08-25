require(['jquery', 'util', 'Const', 'bvUpload', 'bvForm'], function($, util, Const) {
    var vm = util.bind({
        container: 'checkIdCard',
        data: {
            tags: {
                formKey: 'checkIdCardForm'
            },
            entity: {},
            uploadConfig: {
                name: 'upload',
                type: 'camera',
                clazz: 'camera',
                formName: 'identityCard',
                url: '/ocrIdentity',
                files: [
                    {
                        name: 'identityCard',
                        head: '身份证正面'
                        // url: '/app/file/accd0c797d58ae8e31521c1b74b785d2'
                    }
                ],
                success: function (file, upload, uploads, entity) {
                    if (upload) {
                        util.refresh({
                            vm: util.vm(vm, vm.tags.formKey),
                            entity: {
                                name: upload.name,
                                gender: upload.gender,
                                birthday: upload.birthday,
                                address: upload.address,
                                idCard: upload.id_card_number
                            }
                        });
                    }
                }
            },
            formConfig: {
                layout: 'inline',
                columns: [
                    {
                        head: '姓名',
                        name: 'name',
                        config: {
                            attr: {
                                placeholder: '请输入姓名',
                                maxlength: '#name'
                            },
                            validate: {
                                required: '姓名不能为空'
                            }
                        }
                    },
                    {
                        head: '性别',
                        name: 'gender',
                        config: {
                            attr: {
                                readonly: 'readonly'
                            },
                            validate: {
                                required: '性别不能为空,请重新上传身份证!'
                            }
                        }
                    },
                    {
                        head: '出生年月',
                        name: 'birthday',
                        config: {
                            attr: {
                                readonly: 'readonly'
                            },
                            validate: {
                                required: '出生年月不能为空,请重新上传身份证!'
                            }
                        }
                    },
                    {
                        head: '家庭住址',
                        name: 'address',
                        edit: {
                            type: 'static'
                        }
                    },
                    {
                        head: '证件号码',
                        name: 'idCard',
                        config: {
                            attr: {
                                readonly: 'readonly'
                            },
                            validate: {
                                required: '证件号码不能为空,请重新上传身份证!'
                            }
                        }
                    }
                ],
                operates: [
                    {
                        text: '下一步',
                        layout: 'primary',
                        validate: {
                            before: function (event, editType, entity) {
                                if (!entity.upload || !entity.upload.identityCard) {
                                    util.alert('请使用扫描功能上传信息');
                                    return false;
                                }
                                return true;
                            }
                        },
                        click: function (event, editType, entity) {
                            util.post({
                                url: '/savaIdentityInfo',
                                data: {
                                    name: entity.name
                                },
                                success: function () {
                                    util.cache({
                                        name: entity.name
                                    });
                                    util.redirect({
                                        title: '实名绑卡',
                                        url: '/applyQuota/checkIdCardB.html'
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