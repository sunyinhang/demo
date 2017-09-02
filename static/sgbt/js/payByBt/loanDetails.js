require(['jquery', 'util', 'Const', 'bvLayout'], function($, util, Const) {
    //获取 applSeq
    var applSeq = util.gup('applSeq');

    var vm = util.bind({
        container: 'loanDetails',
        data: {
            repayApplCardNo: '',
            repayAccBankName: '',
            applyDt: '',
            applCde: '',
            goodsList: [],
            applseq: '',
            applyAmt: '',
            xfze: '',
            ordertotal: '',
            setlTotalAmt: '',
            repayAmt: '',
            goodsName: '',
            bills: [],
            zdhkFee: '',
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
            pricefn: function(obj){
                if(obj.setlInd == 'Y'){
                    return obj.amount;
                }else if(obj.setlInd == 'N' && obj.days >0){
                    return obj.amount;
                }else if(obj.setlInd == 'N' && obj.days <= 0){
                    return obj.odAmt;
                }
            },
            stateFn: function(obj){
                if(obj.setlInd == 'Y'){
                    return '已结清';
                }else if(obj.setlInd == 'N' && obj.days >0){
                    return '剩余'+ obj.days + '天';
                }else if(obj.setlInd == 'N' && obj.days == 0){
                    return '还款日';
                }
                else if(obj.setlInd == 'N' && obj.days <0){
                    return '逾期'+ obj.days + '天';
                }
            },
            repaymentFn: function(){
                util.modal({
                    message: '您可下载嗨付app进行主动还款',
                    close: false,
                    inline: true,
                    operates: [{
                        text: '取消',
                        close: true,
                    },{
                        text: '下载',
                        close: false,
                        click: function () {
                            window.location.href="http://app.haiercash.com/appdeploy/haiercustomer/appdownload.html" ;//下载app
                        }
                    }]
                })
            }
        },
        mounted: function(){

            util.get({
                url: '/queryLoanDetailInfo?applSeq='+ applSeq,
                success: function(res){
                    var data = util.data(res);
                    if( !util.isEmpty(data)){
                        vm.repayApplCardNo = '****'+ util.format(data.repay_appl_card_no, 'card4');
                        vm.repayAccBankName = data.repay_acc_bank_name;
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
                        vm.setlTotalAmt = data.setlFeeAmt + data.setlIncAmt + data.setlPrcpAmt
                        vm.repayAmt = data.repayAmt;
                        vm.outSts = data.outSts
                    }
                }
            });
            util.get({
                url: '/queryApplListBySeq?applSeq='+ applSeq,
                success: function(res){
                    vm.bills = util.data(res).list;
                    vm.zdhkFee = util.data(res).fqze;
                }
            });
        }
    });
});