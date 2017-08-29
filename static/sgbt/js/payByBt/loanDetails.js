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
            goodsName: '',
            goodsPrice: '',
            mthAmt: '',
            psPrcpAmt: '',
            psNormIntAmt: '',
            applyTnrTyp: '',
            repayAmt: '',
            setlPrcpAmt: '',
            repayPrcpAmt: '',
            items: '',
            zdhkFee: ''
        },
        methods: {

        },
        mounted: function(){
            // 还款卡 repayAccBankName
            // 还款卡号 repayApplCardNo
            // 贷款编号  applCde
            // 申请日期  applyDt
            // 商品名称	goodsName
            // 商品编号	goodsCode
            // 分期本金  psPrcpAmt
            // 总利息金额	psNormIntAmt
            // 应还款总额	repayAmt
            util.get({
                url: '/queryLoanDetailInfo?applSeq='+ applSeq,
                success: function(res){
                    var data = util.data(res);
                    vm.repayApplCardNo = '****'+ data.repayApplCardNo.substring(-4,4);
                    vm.repayAccBankName = data.repayAccBankName;
                    vm.applyDt = data.applyDt;
                    vm.applCde = data.applCde;
                    vm.goodsName = data.goodsName;
                    vm.goodsPrice = data.goodsPrice;
                    vm.mthAmt = data.mthAmt;
                    vm.psPrcpAmt = '￥'+ data.psPrcpAmt;
                    vm.psNormIntAmt = data.psNormIntAmt;
                    vm.applyTnrTyp = data.applyTnrTyp;
                    vm.repayAmt = '￥'+data.repayAmt;
                    vm.setlPrcpAmt = '￥'+data.setlPrcpAmt;
                    vm.repayPrcpAmt = '￥'+data.repayPrcpAmt;
                }
            });
            // 已还本金	setlPrcpAmt
            // 剩余本金	repayPrcpAmt
            // 期供金额	amount
            // 期数	psPerdNo
            // 剩余天数	days
            util.get({
                url: '/queryApplListBySeq?applSeq='+ applSeq,
                success: function(res){
                    vm.items = util.data(res);
                }
            });
            util.get({
                url: '/queryApplAmtBySeqAndOrederNo?applSeq='+ applSeq,
                success: function(res){
                    var data = util.data(res);
                    // TODO: 该使用过滤器
                    vm.zdhkFee = '￥'+data.zdhkFee;
                }
            });
        }
    });
});