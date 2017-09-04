require(['jquery', 'util', 'Const'], function($, util, Const) {
    var vm = util.bind({
        container: 'applyIn',
        data: {
            config:{
                whetherRedirect: false,
                imgSrc: 'custom/themes/default/images/amtNotTopBg.png',
                // title: '顺逛白条',
                whether: false,
                subTitle: '可用总额度',
                amtNum: 'amtSitu',
                num: '额度输出中...',
                whetherBtn: true,
                activBtn: 'goGrayBtn',
                btnText: '去购物'
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
            }
        }
    });
});