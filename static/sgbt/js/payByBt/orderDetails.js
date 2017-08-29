require(['jquery', 'util', 'Const', 'bvLayout'], function($, util, Const) {

    var orderNo = util.gup('orderNo');

    var vm = util.bind({
        container: 'orderDetails',
        data: {
            repayApplCardNo: '',
            repayAccBankName: '',
            applyDt: '',
            applseq: '',
            mthAmt: '',
            applyAmt: '',
            applyTnrTyp: '',
            xfze: '',
            goodName: '',
            goodsPrice: '',
            ordertotal: ''
        },
        methods: {

        },
        mounted: function(){
            util.post({
                url: '/queryOrderInfo',
                data: {
                    // TODO: 写死？
                    orderNo: "ede45c47cf524411bf185799a1cc1944"
                },
                success: function (res) {
                    var data = util.data(res);
                    if( util.isEmpty(data)){
                        vm.repayApplCardNo = '****'+data.appl_card_no.substring(-4,4);
                        vm.repayAccBankName = data.acc_bank_name;
                        vm.applyDt = data.applyDt;
                        vm.applseq = data.applseq;
                        vm.goodsName = data.goodsName;
                        vm.goodsPrice = data.goodsPrice;
                        vm.mthAmt = data.mthAmt;
                        vm.applyAmt = '￥'+data.applyAmt;
                        vm.xfze = data.xfze;
                        vm.applyTnrTyp = data.applyTnrTyp;
                        vm.ordertotal = '￥'+data.ordertotal;
                    }
                }
            });
        }
    });
});