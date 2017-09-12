require(['jquery', 'util', 'Const', 'bvLayout'], function($, util, Const) {
    //获取 applSeq
    var applSeq = util.gup('applSeq');

    var vm = util.bind({
        container: 'loanResult',
        data: {
            applyDt: '',
            applCde: '',
            goodsList: [],
            applseq: '',
            applyAmt: '',
            xfze: '',
            ordertotal: '',
            goodsName: '',
            outSts: ''
        },
        computed: {
            orderStsName: function () {
                return util.trans(this.outSts, '#orderSts');
            }
        },
        filters: {
            currency: function (val) {
                return util.format(val, 'currency');
            }
        },
        methods: {

        },
        mounted: function(){
            util.get({
                url: '/queryLoanDetailInfo?applSeq='+ applSeq,
                success: function(res){
                    var data = util.data(res);
                    if( !util.isEmpty(data)){
                        vm.applyDt = data.apply_dt;
                        vm.applseq = data.appl_seq;
                        vm.goodsList = data.goodsList.good;
                        var goodsName = '';
                        for(var i=0;i<vm.goodsList.length; i++){
                            if (goodsName) {
                                goodsName += '+';
                            }
                            goodsName += vm.goodsList[i].goods_name;
                        }
                        vm.goodsName = goodsName;
                        vm.applyAmt = data.apply_amt;
                        vm.xfze = data.totfee;
                        vm.ordertotal = data.ordertotal;
                        vm.outSts = data.outSts
                    }
                }
            });
        }
    });
});