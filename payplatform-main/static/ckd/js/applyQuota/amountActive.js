require(['jquery', 'util', 'Const'], function($, util, Const) {
    var vm = util.bind({
        container: 'amountActive',
        data: {
            stateConfig:{
                imgSrc: 'custom/themes/default/images/amtNotTopBg.png',
                // title: '顺逛白条',
                whether: true ,
                subTitle: '可用总额度',
                amtNum: 'amtNum',
                num: '暂无',
                whetherBtn: true,
                activBtn: 'activAmuntBtn',
                btnText: '激活额度'
            }
        },
        methods: {
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
            }
        }
    });
});