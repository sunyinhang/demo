require(['jquery', 'util', 'Const', 'bvTabs', 'bvList'], function($, util, Const) {
    var currentIndex = util.gup('currentIndex');
    if (currentIndex) {
        currentIndex = util.toNumber(currentIndex);
    } else {
        currentIndex = 1;
    }
    var vm = util.bind({
        container: 'installmentBill',
        data: {
            pageNo: 1,
            pageSize: 20,
            tags: {
                tabsKey: 'installmentBillTabs',
                tabsContainerKey: 'installmentBillTabsContainer',
                tabsContentKey1: 'installmentBillTabsContent1',
                tabsContentKey2: 'installmentBillTabsContent2',
                tabsContentKey3: 'installmentBillTabsContent3',
                tabsContentKey4: 'installmentBillTabsContent4',
                listKey1: 'installmentBillList1',
                listKey2: 'installmentBillList2',
                listKey3: 'installmentBillList3',
                listKey4: 'installmentBillList4'
            },
            tabsConfig: {
                layout: 'head',
                currentIndex: currentIndex,
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
            listConfig1: {
                type: 'media',
                items: [
                ],
                refresh: true,
                infinite: true
            },
            listConfig2: {
                type: 'media',
                items: [
                ],
                refresh: true,
                infinite: true
            },
            listConfig3: {
                type: 'media',
                items: [
                ],
                refresh: true,
                infinite: true
            },
            listConfig4: {
                type: 'media',
                items: [
                ],
                refresh: true,
                infinite: true
            }
        },
        filters: {
            currency: function (val) {
                return util.format(val, 'currency');
            }
        },
        methods: {
            onContentActive: function (index) {
                util.refresh({
                    vm: util.vm(this, this.tags.tabsKey),
                    index: index
                });
            },
            onRefresh: function (index) {
                this.onActive(index);
            },
            onInfinite: function (index) {
                this.onActive(index, true);
            },
            onActive: function (index, pagination, _vm) {
                /*util.refresh({
                    vm: util.vm(vm, vm.tags.listKey),
                    title: '第' + (index + 1) + '个',
                    items: util.clone(items)
                });*/
                if (!_vm) {
                    var _contentVm = util.vm(this, this.tags.tabsContainerKey, this.tags['tabsContentKey' + (index + 1)]);
                    if (_contentVm) {
                        _vm = util.vm(_contentVm, this.tags['listKey' + (index + 1)]);
                    }
                }
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
                                vm: _vm,
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
                            vm.refresh(util.data(res), pagination, _vm , '1');
                        },
                        error: function () {
                            util.refresh({
                                vm: _vm,
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
                                vm: _vm,
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
                                vm: _vm,
                                items: []
                            });
                        }
                    });
                }
            },
            refresh: function (data, pagination, _vm , flag) {
                if (data && data.orders && data.orders.length > 0) {
                    if (data.orders.length < vm.pageSize) {
                        _vm.complete();
                    }
                    var items = [];
                    for (var i=0; i<data.orders.length; i++) {
                        var order = data.orders[i];
                        items.push(
                            {
                                title: '<span class="bv-left">日期：' + order.applyDt + '</span><span class="bv-right">状态：' + util.trans(order.outSts, '#outSts') + '</span>',
                                innerImage: 'custom/themes/default/images/payByBt/product.png',
                                content: order.goodsName,
                                badge: 'badge',
                                extra: '<span class="bv-align-right">合计：总计'+order.goodsCount +'件商品，合计: '+ util.format(order.apprvAmt , 'currency' )+'</span>',
                                order: order,
                                click: function(event, item){
                                    // debugger
                                    if(flag === '1'){
                                        util.redirect({
                                            // title: '订单详情',
                                            url: '/payByBt/orderDetails.html?orderNo='+ item.order.orderNo + '&currentIndex=' + util.tabsIndex(util.vm(vm, vm.tags.tabsKey)),
                                            back: '/payByBt/installmentBill.html?currentIndex=' + util.tabsIndex(util.vm(vm, vm.tags.tabsKey))
                                        });
                                    }else{
                                        util.redirect({
                                            // title: '贷款详情',
                                            url: '/payByBt/loanDetails.html?applSeq='+ item.order.applSeq + '&currentIndex=' + util.tabsIndex(util.vm(vm, vm.tags.tabsKey)),
                                            back: '/payByBt/installmentBill.html?currentIndex=' + util.tabsIndex(util.vm(vm, vm.tags.tabsKey))
                                        });
                                    }
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
                                            if( item.order.outSts === '1'){
                                                return true;
                                            }else{
                                                return false;
                                            }
                                        },
                                        click: function (event, item) {
                                            util.redirect({
                                                // title: '顺逛白条',
                                                url: '/payByBt/btInstalments.html?orderNo='+ item.order.orderNo
                                            });
                                        }
                                    },{
                                        text: '删除订单',
                                        layout: 'primary',
                                        show: function (item) {
                                            if( item.order.outSts === '1'){
                                                return true;
                                            }else{
                                                return false;
                                            }
                                        },
                                        click: function (event, item, index) {
                                            /*vm.onActive(vm.tabsConfig.currentIndex);
                                            util.alert('删除成功');*/
                                            util.post({
                                                url: '/deleteOrderInfo',
                                                data: {
                                                    orderNo: item.order.orderNo
                                                },
                                                success: function(res){
                                                    util.alert('删除成功');
                                                    vm.onActive(util.tabsIndex(util.vm(vm, vm.tags.tabsKey)));
                                                }
                                            });

                                        }
                                    },{
                                        text: '审批进度',
                                        layout: 'primary',
                                        show: function (item) {
                                            if( item.order.outSts === '01' || item.order.outSts === '02' || item.order.outSts === '04' || item.order.outSts === '05' || item.order.outSts === '06'
                                                || item.order.outSts === '20' || item.order.outSts === '22' || item.order.outSts === '24'){
                                                return true;
                                            }else{
                                                return false;
                                            }
                                        },
                                        click: function (event, item) {
                                            util.redirect({
                                                // title: '审批进度',
                                                url: '/payByBt/applyProgress.html?applSeq='+ item.order.applSeq,
                                                back: '/payByBt/installmentBill.html?currentIndex=' + util.tabsIndex(util.vm(vm, vm.tags.tabsKey))
                                                //back: false
                                            });
                                        }
                                    },{
                                        text: '还款',
                                        layout: 'primary',
                                        show: function (item) {
                                            if( item.order.outSts === '20' || item.order.outSts === 'OD'){
                                                return true;
                                            }else{
                                                return false;
                                            }
                                        },
                                        click: function (event, item) {
                                            console.log('clicked');
                                        }
                                    },/*{
                                        text: '修改提交',
                                        layout: 'primary',
                                        show: function (item) {
                                            if( item.order.outSts === '22'){
                                                return true;
                                            }else{
                                                return false;
                                            }
                                        },
                                        click: function (event, item) {
                                            console.log('clicked');
                                        }
                                    },*/{
                                        text: '申请放款',
                                        layout: 'primary',
                                        show: function (item) {
                                            if( item.order.outSts === '20'){
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
                            vm: _vm,
                            items: items
                        });
                    } else {
                        _vm.load(items, true);
                    }
                } else {
                    if (!pagination) {
                        _vm.load([], false);
                    } else {
                        _vm.complete();
                    }
                }
            }
        },
        mounted: function () {
            util.initPage('swiper');
            util.refresh({
                vm: util.vm(this, this.tags.tabsKey),
                content: this.tabsConfig.currentIndex
            });
            this.onActive(this.tabsConfig.currentIndex);
        }
    });
});