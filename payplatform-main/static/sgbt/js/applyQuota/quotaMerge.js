require(['jquery', 'util', 'Const','bvForm'], function($, util, Const) {
    var vm = util.bind({
        container: 'quotaMerge',
        data: {
            config:{
                whetherRedirect: false,
                imgSrc: 'custom/themes/default/images/amtNotTopBg.png',
                // title: '顺逛白条',
                whether: false,
                subTitle: '可用总额度',
                amtNum: 'amtSitu',
                num: '额度输出中...',
                whetherBtn: false,
                activBtn: 'goGrayBtn',
                btnText: '去购物'
            },
            config2:{
                imgSrc: 'custom/themes/default/images/amtNotTopBg.png',
                // title: '顺逛白条',
                whether: false,
                subTitle: '可用总额度',
                amtNum: 'amtSitu',
                num: '额度申请失败',
                whetherDesc: true
            },
            config3:{
                imgSrc: 'custom/themes/default/images/amtNotTopBg.png',
                // title: '顺逛白条',
                whether: true ,
                subTitle: '可用总额度',
                amtNum: 'amtNum',
                num: '暂无',
                whetherBtn: true,
                activBtn: 'activAmuntBtn',
                btnText: '激活额度'
            },
            config4:{
                imgSrc: 'custom/themes/default/images/amtTopBg.png',
                // title: '顺逛白条',
                whether: true,
                subTitle: '可用额度',
                amtNum: 'amtNum',
                numUnit: true,
                num: '',
                whetherBtn: false,
                activBtn: 'activAmuntBtn',
                btnText: '去购物'
            },
            mounthAmount: '',
            crdNorAvailAmt: '',
            config5:{
                imgSrc: 'custom/themes/default/images/amtNotTopBg.png',
                // title: '顺逛白条',
                whether: false ,
                subTitle: '可用总额度',
                amtNum: 'amtSitu',
                num: '申请被退回',
                whetherBtn: true,
                activBtn: 'goGrayBtn',
                btnText: '查看原因'
            },
            formConfig:{
                operates: [
                    {
                        text: '修改',
                        layout: 'primary',
                        click: function (event, editType, entity) {
                            util.redirect({
                                // title: '个人资料',
                                url: util.mix('/applyQuota/personalInform.html', {
                                    edxg: '1'
                                })
                            });
                        }
                    }
                ]
            }
        },
        filters: {
            currency: function (val) {
                return util.format(val, 'currency');
            }
        },
        methods:{
            redirectFn: function(){
                util.get({
                    url: '/shunguang/getedbackurl',
                    success: function(res){
                        var data = util.data(res);
                        if( !util.isEmpty(data)){
                           util.location(data.edbackurl) ;
                        }
                    }
                });
            },
            goShopFn: function(){
                //TODO
            },
            /*activeAmountFn: function(param){
                util.redirect({
                    // title: '实名',
                    url: '/applyQuota/checkIdCard.html'
                });
            },*/
            activeAmountFn: function(param){
                util.post({
                    url: "/creditLineApply",
                    success: function (res) {
                        var data = util.data(res);
                        if (data) {
                            initRedirect(data.flag);
                        }
                    }
                });
            },
            goShopMyAmountFn: function(){
                util.get({
                    url: '/shunguang/getedbackurl',
                    success: function(res){
                        var data = util.data(res);
                        if( !util.isEmpty(data)){
                            util.location(data.edbackurl) ;
                        }
                    }
                });
            },
            getReasonsFn: function(param){
                util.redirect({
                    // title: '额度申请进度',
                    url: '/applyQuota/applyProgress.html'
                });
            }
        },
        mounted: function(){
           util.post({
                url: '/shunguang/approveStatus',
                success: function(res){
                    var data = util.data(res);
                    if( data.flag == '01'){ //额度审批中
                        $('#applyIn').show();
                    }else if(data.flag == '02'){ //额度申请失败
                        $('#applyFail').show();
                    }else if(data.flag == '03'){ //暂无额度
                        $('#amountNot').show();
                    }else if(data.flag == '04'){ //我的额度
                        $('#myAmount').show();
                        util.post({
                            url: '/getPersonalCenterInfo',
                            success: function(res){
                                var data = util.data(res);
                                if(!util.isEmpty(data)){
                                    vm.mounthAmount = data.mounthAmount;
                                }
                            }
                        });
                        util.post({
                            url: '/edCheck',
                            success: function(res){
                                var data = util.data(res);
                                if(!util.isEmpty(data)){
                                    vm.config4.num = data.crdComAvailAnt;
                                    vm.crdNorAvailAmt = data.crdAmt;
                                }
                            }
                        });
                    }else if(data.flag == '05'){ //额度被退回
                        $('#applyReturn').show();
                    }
                }
            });
        }
    });
});