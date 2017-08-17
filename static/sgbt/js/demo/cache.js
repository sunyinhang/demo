require(['jquery', 'util', 'Const', 'bvForm'], function($, util, Const) {
    var vm = util.bind({
        container: 'demoCache',
        data: {
            tags: {
                formKey: 'demoCacheForm'
            },
            formConfig: {
                layout: 'inline',
                columns: [
                    {
                        head: '手机号',
                        name: 'phoneNo',
                        config: {
                            attr: {
                                placeholder: '请输入手机号'
                            },
                            validate: {
                                required: '手机号不能为空'
                            }
                        }
                    }, {
                        head: '用户编号',
                        name: 'userId',
                        config: {
                            attr: {
                                placeholder: '请输入用户编号'
                            },
                            validate: {
                                required: '用户编号不能为空'
                            }
                        }
                    }, {
                        head: '客户编号',
                        name: 'custNo',
                        config: {
                            attr: {
                                placeholder: '请输入客户编号'
                            },
                            validate: {
                                required: '客户编号不能为空'
                            }
                        }
                    }, {
                        head: '姓名',
                        name: 'name',
                        config: {
                            attr: {
                                placeholder: '请输入姓名'
                            },
                            validate: {
                                required: '姓名不能为空'
                            }
                        }
                    }, {
                        head: '身份证号',
                        name: 'idCard',
                        config: {
                            attr: {
                                placeholder: '请输入身份证号'
                            }
                        }
                    }
                ],
                operates: [
                    {
                        text: '保存',
                        layout: 'primary',
                        click: function (event, editType, entity) {
                            util.cacheServer({
                                phoneNo: entity.phoneNo,
                                userId: entity.userId,
                                custNo: entity.custNo,
                                name: entity.name,
                                idCard: entity.idCard
                            });
                            /*util.post({
                             url: '/cache',
                             data: {
                             type: 'set',
                             params: 'phoneNo,userId,custNo,name,idCard',
                             phoneNo: entity.phoneNo,
                             userId: entity.userId,
                             custNo: entity.custNo,
                             name: entity.name,
                             idCard: entity.idCard
                             },
                             success: function () {
                             util.alert('成功');
                             }
                             });*/
                        }
                    }
                ]
            }
        },
        mounted: function () {
            var params = util.cacheServer('phoneNo,userId,custNo,name,idCard');
            util.refresh({
                vm: util.vm(this, this.tags.formKey),
                entity: params
            });
            /*util.post({
             url: '/cache',
             data: {
             type: 'get',
             params: 'phoneNo,userId,custNo,name,idCard'
             },
             success: function (res) {
             util.refresh({
             vm: util.vm(vm, vm.tags.formKey),
             entity: util.data(res)
             });
             }
             });*/
        }
    });
});