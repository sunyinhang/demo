require(['jquery', 'util', 'Const'], function($, util, Const) {
    var vm = util.bind({
        container: 'applyIn',
        data: {
            config:{
                imgSrc: 'custom/themes/default/images/amtNotTopBg.png',
                title: '海尔白条',
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
            goShopFn: function(){
                console.log(123);
            }
        }
    });
});