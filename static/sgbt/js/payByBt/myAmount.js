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
                num: '15000',
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
                data: function(res){
                    var data = util.getData(res);
                    if(!util.isEmpty(res)){
                        vm.config.num = data.crdNorAvailAmt;
                        vm.crdNorAvailAmt = data.crdNorAvailAmt;
                        vm.mounthAmount = data.mounthAmount;
                    }
                }
            });
        }
    });
});