require(['jquery', 'util', 'Const', 'bvUpload', 'bvForm'], function($, util, Const) {
    var vm = util.bind({
        container: 'checkIdCardB',
        data: {
            tags: {
                formKey: 'checkIdCardBForm'
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
                        head: '身份证反面'
                        // url: '/app/file/accd0c797d58ae8e31521c1b74b785d2'
                    }
                ],
                success: function (file, upload, uploads, entity) {
                    if (upload) {
                        util.refresh({
                            vm: util.vm(vm, vm.tags.formKey),
                            entity: {
                                issued_by: upload.issued_by,
                                valid_date: upload.valid_date
                            }
                        });
                    }
                }
            },
            formConfig: {
                layout: 'inline',
                columns: [
                    {
                        head: '发证机关',
                        name: 'issued_by',
                        config: {
                            attr: {
                                readonly: 'readonly'
                            },
                            validate: {
                                required: '发证机关不能为空,请重新上传身份证!'
                            }
                        }
                    },
                    {
                        head: '有效期',
                        name: 'valid_date',
                        config: {
                            attr: {
                                readonly: 'readonly'
                            },
                            validate: {
                                required: '有效期不能为空,请重新上传身份证!'
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
                            util.redirect({
                                title: '个人资料',
                                url: '/applyQuota/tiedBnkCrd.html'
                            });
                        }
                    }
                ]
            }
        }
    });
});