require(['jquery', 'util', 'Const'], function($, util, Const) {
    var vm = util.bind({
        container: 'myAmount',
        data: {
            config:{
                imgSrc: 'custom/themes/default/images/amtTopBg.png',
                title: '海尔白条',
                whether: true,
                subTitle: '可用总额度',
                amtNum: 'amtNum',
                numUnit: true,
                num: '',
                whetherBtn: true,
                activBtn: 'activAmuntBtn',
                btnText: '去购物'
            },
            mounthAmount: '',
            crdNorAvailAmt: '',
        },
        methods:{
            goShopFn: function(){
                console.log(123);
            }
        },
        mounted: function(){
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
                        vm.config.num = data.crdAmt;
                        vm.crdNorAvailAmt = data.crdComAvailAnt
                    }
                }
            });
        }
    });
});