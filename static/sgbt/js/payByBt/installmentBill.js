require(['jquery', 'util', 'Const', 'bvTabs', 'bvList'], function($, util, Const) {
    /*var count = 0;
    var items = [];
    for (var i=0; i<8; i++) {
        items.push(
            {
                title: '<span class="bv-left">日期</span><span class="bv-right">状态</span>',
                image: 'custom/themes/default/images/active.png',
                content: '内容',
                extra: '<span class="bv-align-right">合计：</span>',
                data: 'xxxx',
                operates: [
                    {
                        text: '按钮1',
                        layout: 'primary',
                        show: function (item) {
                            return true;
                        },
                        click: function (event, item) {
                            console.log('clicked' + item.data);
                        }
                    },
                    {
                        text: '按钮2',
                        show: function (item) {
                            return true;
                        },
                        click: function (event, item) {
                            console.log('clicked');
                        }
                    }
                ]
            }
        );
    }*/

    var vm = util.bind({
        container: 'installmentBill',
        data: {
            pageNo: 1,
            pageSize: 20,
            tags: {
                tabsKey: 'installmentBillTabs',
                listKey: 'installmentBillList'
            },
            tabsConfig: {
                layout: 'head',
                currentIndex: 0,
                tabs: [
                    {
                        text: '全部',
                        target: 'tab1'
                    },
                    {
                        text: '待提交',
                        target: 'tab2'
                    },
                    {
                        text: '待还款',
                        target: 'tab3'
                    },
                    {
                        text: '审批中',
                        target: 'tab4'
                    }
                ]
            },
            listConfig: {
                type: 'media',
                /// title: 'xxxx',
                items: [
                ],
                infinite: function () {
                    var _vm = util.vm(vm , vm.tags.tabsKey);
                    if (_vm) {
                        vm.onActive(util.tabsIndex(_vm), true, this);
                    }
                    /*var items = [];
                    for (var i=0; i<10; i++) {
                        items.push({
                            title: '标题' + (count*10 + i),
                            content: '内容'
                        });
                    }
                    count++;
                    if (count > 3) {
                        this.load(items, false);
                    } else {
                        this.load(items, true);
                    }*/
                }
            }
        },
        methods: {
            onActive: function (index, pagination, _vm) {
                /*util.refresh({
                    vm: util.vm(vm, vm.tags.listKey),
                    title: '第' + (index + 1) + '个',
                    items: util.clone(items)
                });*/
                if (!pagination) {
                    this.pageNo = 1;
                } else {
                    this.pageNo++;
                }
                if (index === 0) {
                    // 全部
                    util.post({
                        url: '/queryAllLoanInfo',
                        data: {
                            page: this.pageNo,
                            size: this.pageSize
                        },
                        success: function(res){
                            vm.refresh(util.data(res), pagination, _vm);
                        },
                        error: function () {
                            util.refresh({
                                vm: util.vm(vm, vm.tags.listKey),
                                items: []
                            });
                        }
                    });
                } else if (index === 1) {
                    // 待提交
                    util.post({
                        url: '/queryPendingLoanInfo',
                        data: {
                            page: this.pageNo,
                            size: this.pageSize
                        },
                        success: function(res){
                            vm.refresh(util.data(res), pagination, _vm);
                        },
                        error: function () {
                            util.refresh({
                                vm: util.vm(vm, vm.tags.listKey),
                                items: []
                            });
                        }
                    });
                } else if (index === 2) {
                    // 待还款
                    util.post({
                        url: '/queryPendingRepaymentInfo',
                        data: {
                            page: this.pageNo,
                            size: this.pageSize
                        },
                        success: function(res){
                            vm.refresh(util.data(res), pagination, _vm);
                        },
                        error: function () {
                            util.refresh({
                                vm: util.vm(vm, vm.tags.listKey),
                                items: []
                            });
                        }
                    });
                } else if (index === 3) {
                    // 审批中
                    util.post({
                        url: '/queryApplLoanInfo',
                        data: {
                            page: this.pageNo,
                            size: this.pageSize,
                            outSts: '01'
                        },
                        success: function(res){
                            vm.refresh(util.data(res), pagination, _vm);
                        },
                        error: function () {
                            util.refresh({
                                vm: util.vm(vm, vm.tags.listKey),
                                items: []
                            });
                        }
                    });
                }
            },
            refresh: function (data, pagination, _vm) {
                if (data && data.orders && data.orders.length > 0) {
                    var items = [];
                    for (var i=0; i<data.orders.length; i++) {
                        var order = data.orders[i];
                        items.push(
                            {
                                title: '<span class="bv-left">日期：' + order.applyDt + '</span><span class="bv-right">状态：' + util.trans(order.outSts, '#outSts') + '</span>',
                                innerImage: 'custom/themes/default/images/payByBt/product.png',
                                content: order.goodsName,
                                badge: 'badge',
                                extra: '<span class="bv-align-right">合计：总计'+order.goodsCount +'件商品，合计: ￥'+ order.apprvAmt+'</span>',
                                click: function(){
                                    util.redirect({
                                        title: '贷款详情',
                                        url: '/payByBt/loanDetails.html',
                                        back: false
                                    });
                                },
                                operates: [
                                    // 1-待提交
                                    // 2-待确认
                                    // 3-被退回
                                    // 01	审批中
                                    // 02	贷款被拒绝
                                    // 03	贷款已取消
                                    // 04	合同签订中
                                    // 05	审批通过，等待放款
                                    // 06	已放款
                                    // 20	待放款
                                    // 22	审批退回
                                    // 23	合同签章中
                                    // 24	放款审核中
                                    // 25	额度申请被拒
                                    // 26	额度申请已取消
                                    // 27	已通过
                                    // AA	取消放款
                                    // OD   逾期
                                    // WS   待发货/待取货
                                    // 30-已付款待发货
                                    // 31-已发货
                                    // 92-退货中
                                    // 93-已退货
                                    {
                                        text: '继续提交',
                                        layout: 'primary',
                                        show: function (item) {
                                            if( order.outSts === '1'){
                                                return true;
                                            }else{
                                                return false;
                                            }
                                        },
                                        click: function (event, item) {
                                            console.log('clicked' + item.data);
                                        }
                                    },{
                                        text: '删除订单',
                                        layout: 'primary',
                                        show: function (item) {
                                            if( order.outSts === '1'){
                                                return true;
                                            }else{
                                                return false;
                                            }
                                        },
                                        click: function (event, item) {
                                            util.post({
                                                url: '/deleteOrderInfo',
                                                data: {
                                                    orderNo: order.orderNo
                                                },
                                                success: function(res){
                                                    util.alert('删除成功');
                                                }
                                            });

                                        }
                                    },{
                                        text: '审批进度',
                                        layout: 'primary',
                                        show: function (item) {
                                            if( order.outSts === '01' || order.outSts === '02' || order.outSts === '05' || order.outSts === '06' || order.outSts === '20' || order.outSts === '22' || order.outSts === '24'){
                                                return true;
                                            }else{
                                                return false;
                                            }
                                        },
                                        click: function (event, item) {
                                            util.redirect({
                                                title: '审批进度',
                                                url: '/payByBt/applyProgress.html',
                                                back: false
                                            });
                                        }
                                    },{
                                        text: '还款',
                                        layout: 'primary',
                                        show: function (item) {
                                            if( order.outSts === '20' || order.outSts === 'OD'){
                                                return true;
                                            }else{
                                                return false;
                                            }
                                        },
                                        click: function (event, item) {
                                            console.log('clicked');
                                        }
                                    },{
                                        text: '修改提交',
                                        layout: 'primary',
                                        show: function (item) {
                                            if( order.outSts === '22'){
                                                return true;
                                            }else{
                                                return false;
                                            }
                                        },
                                        click: function (event, item) {
                                            console.log('clicked');
                                        }
                                    },{
                                        text: '申请放款',
                                        layout: 'primary',
                                        show: function (item) {
                                            if( order.outSts === '20'){
                                                return true;
                                            }else{
                                                return false;
                                            }
                                        },
                                        click: function (event, item) {
                                            console.log('clicked');
                                        }
                                    }
                                ]
                            }
                        );
                    }
                    if (!pagination) {
                        util.refresh({
                            vm: util.vm(vm, vm.tags.listKey),
                            items: items
                        });
                    } else {
                        _vm.load(items, true);
                    }
                } else {
                    if (pagination) {
                        _vm.load([], false);
                    }
                }
            }
        },
        mounted: function () {
            this.onActive(this.tabsConfig.currentIndex);
            /*util.refresh({
                vm: util.vm(this, this.tags.listKey),
                items: util.clone(items)
            });*/
        }
    });
});