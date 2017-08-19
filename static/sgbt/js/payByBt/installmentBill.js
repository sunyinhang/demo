require(['jquery', 'util', 'Const', 'bvTabs', 'bvList'], function($, util, Const) {
    var count = 0;
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
    }
    var vm = util.bind({
        container: 'installmentBill',
        data: {
            tags: {
                tabsKey: 'installmentBillTabs',
                listKey: 'installmentBillList'
            },
            tabsConfig: {
                layout: 'head',
                currentIndex: 1,
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
                    var items = [];
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
                    }
                }
            }
        },
        methods: {
            onActive: function (index) {
                util.refresh({
                    vm: util.vm(vm, vm.tags.listKey),
                    title: '第' + (index + 1) + '个',
                    items: util.clone(items)
                });
            }
        },
        mounted: function () {
            util.post({
                url: '/queryAllLoanInfo',
                data: {
                    page: '1',
                    size: '3'
                },
                success: function(res){
                    var data = util.data(res);
                }
            });
            util.refresh({
                vm: util.vm(this, this.tags.listKey),
                items: util.clone(items)
            });
        }
    });
});