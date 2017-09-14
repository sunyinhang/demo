require(['jquery', 'util', 'Const'], function($, util, Const) {
    var vm = util.bind({
        container: 'myAmountView',
        data: {
            config:{
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
            crdComAvailAnt:''
        },
        filters: {
            currency: function (val) {
                return util.format(val, 'currency');
            }
        },
        methods:{
            goShopFn: function(){
                util.get({
                    url: '/shunguang/getedbackurl',
                    success: function(res){
                        var data = util.data(res);
                        if( !util.isEmpty(data)){
                            util.location(data.edbackurl) ;
                        }
                    }
                });
            }
        },
        mounted: function(){
            util.post({
                url: '/edCheck',
                success: function(res){
                    var data = util.data(res);
                    console.log(data)
                    if(!util.isEmpty(data)){
                        vm.crdNorAvailAmt = data.crdNorAvailAmt;
                        vm.crdComAvailAnt = data.crdComAvailAnt;
                        vm.config.num = data.crdComAvailAnt;
                    }
                }
            });
        }
    });
});