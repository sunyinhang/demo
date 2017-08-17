require(['jquery', 'util', 'Const'], function($, util, Const) {
    var vm = util.bind({
        container: 'applyFail',
        data: {
            config:{
                imgSrc: 'custom/themes/default/images/amtNotTopBg.png',
                title: '海尔白条',
                whether: false,
                subTitle: '可用总额度',
                amtNum: 'amtSitu',
                num: '额度申请失败',
                whetherDesc: true
            }
        }
    });
});