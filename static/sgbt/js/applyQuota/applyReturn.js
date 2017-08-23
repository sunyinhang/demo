require(['jquery', 'util', 'Const','bvForm'], function($, util, Const) {
    var vm = util.bind({
        container: 'applyReturn',
        data: {
            config:{
                imgSrc: 'custom/themes/default/images/amtNotTopBg.png',
                title: '海尔白条',
                whether: false ,
                subTitle: '可用总额度',
                amtNum: 'amtSitu',
                num: '申请被退回',
                whetherBtn: true,
                activBtn: 'goGrayBtn',
                btnText: '查看原因'
            },formConfig:{
                operates: [
                    {
                        text: '修改',
                        layout: 'primary',
                        click: function (event, editType, entity) {
                            util.redirect({
                                title: '个人资料',
                                url: '/applyQuota/personalInform.html?edxg=1',
                                back: false
                            });
                        }
                    }
                ]
            }
        },
        methods: {
            getReasonsFn: function(param){
                util.redirect({
                    title: '个人资料',
                    url: '/applyQuota/applyProgress.html',
                    back: false
                });
            }
        }
    });
});