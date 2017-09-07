require(['jquery', 'util', 'Const', 'bvForm'], function($, util, Const) {
    var param = util.cache('userId');
    var vm = util.bind({
        container: 'demoOrder',
        data: {
            tags: {
                formKey: 'demoOrderForm',
                form1Key: 'demoOrderForm1',
                form2Key: 'demoOrderForm2',
                formOperateKey: 'demoOrderOperateForm'
            },
            entity: {
                order: {},
                goods1: {},
                goods2: {}
            },
            formConfig: {
                title: '订单信息',
                layout: 'inline',
                columns: [
                    {
                        head: 'userId',
                        name: 'userId',
                        config: {
                            defaultValue: param.userId
                        }
                    }, {
                        head: 'applyAmt',
                        name: 'applyAmt',
                        config: {
                            defaultValue: '1800'
                        }
                    }, {
                        head: 'mallOrderNo',
                        name: 'mallOrderNo',
                        config: {
                            defaultValue: 'dd002'
                        }
                    }, {
                        head: 'typCde',
                        name: 'typCde',
                        config: {
                            defaultValue: '17035a'
                        }
                    }, {
                        head: 'deliverAddr',
                        name: 'deliverAddr',
                        config: {
                            defaultValue: '山东青岛崂山'
                        }
                    }, {
                        head: 'deliverProvince',
                        name: 'deliverProvince',
                        config: {
                            defaultValue: '370000'
                        }
                    }, {
                        head: 'deliverCity',
                        name: 'deliverCity',
                        config: {
                            defaultValue: '370200'
                        }
                    }, {
                        head: 'deliverArea',
                        name: 'deliverArea',
                        config: {
                            defaultValue: '370202'
                        }
                    }
                ]
            },
            formConfig1: {
                title: '商品1',
                layout: 'inline',
                columns: [
                    {
                        head: 'brandName',
                        name: 'brandName',
                        config: {
                            defaultValue: 'SG2'
                        }
                    }, {
                        head: 'goodsModel',
                        name: 'goodsModel',
                        config: {
                            defaultValue: 'sg2'
                        }
                    }, {
                        head: 'goodsKind',
                        name: 'goodsKind',
                        config: {
                            defaultValue: 'sg2'
                        }
                    }, {
                        head: 'goodsName',
                        name: 'goodsName',
                        config: {
                            defaultValue: 'sg2'
                        }
                    }, {
                        head: 'cOrderSn',
                        name: 'cOrderSn',
                        config: {
                            defaultValue: 'wd002'
                        }
                    }, {
                        head: 'goodsNum',
                        name: 'goodsNum',
                        config: {
                            defaultValue: '1'
                        }
                    }, {
                        head: 'goodsPrice',
                        name: 'goodsPrice',
                        config: {
                            defaultValue: '1000'
                        }
                    }
                ]
            },
            formConfig2: {
                title: '商品2',
                layout: 'inline',
                columns: [
                    {
                        head: 'brandName',
                        name: 'brandName',
                        config: {
                            defaultValue: 'SG3'
                        }
                    }, {
                        head: 'goodsModel',
                        name: 'goodsModel',
                        config: {
                            defaultValue: 'sg3'
                        }
                    }, {
                        head: 'goodsKind',
                        name: 'goodsKind',
                        config: {
                            defaultValue: 'sg3'
                        }
                    }, {
                        head: 'goodsName',
                        name: 'goodsName',
                        config: {
                            defaultValue: 'sg3'
                        }
                    }, {
                        head: 'cOrderSn',
                        name: 'cOrderSn',
                        config: {
                            defaultValue: 'wd003'
                        }
                    }, {
                        head: 'goodsNum',
                        name: 'goodsNum',
                        config: {
                            defaultValue: '1'
                        }
                    }, {
                        head: 'goodsPrice',
                        name: 'goodsPrice',
                        config: {
                            defaultValue: '800'
                        }
                    }
                ]
            },
            formOperate: {
                operates: [
                    {
                        text: '保存',
                        layout: 'primary',
                        click: function (event, editType, entity) {
                            var data = vm.entity.order;
                            data.appOrderGoodsList = [vm.entity.goods1, vm.entity.goods2];
                            util.post({
                                url: '/shunguang/payApplytest',
                                data: data,
                                headers: {
                                    token: util.gup('token'),
                                    channel: Const.rest.headers.channel,
                                    channelNo: Const.rest.headers.channelNo
                                },
                                success: function (res) {
                                    util.redirect({
                                        url: '/payByBt/btInstalments.html'
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