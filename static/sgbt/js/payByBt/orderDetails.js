require(['jquery', 'util', 'Const', 'bvLayout'], function($, util, Const) {

    var orderNo = util.gup('orderNo');

    var vm = util.bind({
        container: 'orderDetails',
        data: {
            repayApplCardNo: '',
            repayAccBankName: '',
            applyDt: '',
            applseq: '',
            applyAmt: '',
            applyTnrTyp: '',
            xfze: '',
            goodsList: [],
            ordertotal: ''
        },
        filters: {
            currency: function (val) {
                return util.format(val, 'currency');
            }
        },
        mounted: function(){
            util.post({
                url: '/queryOrderInfo',
                data: {
                    orderNo: orderNo
                },
                success: function (res) {
                    var data = util.data(res);
                    if( !util.isEmpty(data)){
                        vm.repayApplCardNo = '****'+ util.format(data.repay_appl_card_no, 'card4');
                        vm.repayAccBankName = data.repay_acc_bank_name;
                        vm.applyDt = data.apply_dt;
                        vm.applseq = data.appl_seq;
                        vm.goodsList = data.goodsList.good;
                        vm.applyAmt = data.apply_amt;
                        vm.xfze = data.xfze;
                        vm.ordertotal = parseInt(data.apply_amt)+ parseInt(data.xfze);
                    }
                }
            });
        }
    });
});