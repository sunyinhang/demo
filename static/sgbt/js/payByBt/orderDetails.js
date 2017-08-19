require(['jquery', 'util', 'Const', 'bvLayout'], function($, util, Const) {
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
                    orderNo: "cb895162b97946ad9808f140c010ab69"
                },
                success: function (res) {
                    var data = util.data(res);
                    console.log(data)
                    // 默认还款卡号：repayApplCardNo
                    // 默认还款卡银行名称：repayAccBankName
                    // 业务日期：applyDt
                    // 贷款编号：applseq
                    // 本金：applyAmt
                    // 息费：分期xfze/随借随还rlx元/天
                    // 合计：分期applyAmt+xfze/随借随还“本金+”rlx元/天
                    // 收货人：adName
                    // 收货电话：adPhone
                    // 收货地址：deliverProvince+deliverCity+deliverArea+deliverAddr
                    // 商品价格：goodsPrice
                    // 商品名称：goodName
                    // 商品数量：goodsNum
                    // 商品名称中有可能带有“goodname”，要截取掉
                    vm.repayApplCardNo = '****'+data.repayApplCardNo.substring(-4,4);
                    vm.repayAccBankName = data.repayAccBankName;
                    vm.applyDt = data.applyDt;
                    vm.applseq = data.applseq;
                    vm.applyAmt = '￥'+data.applyAmt;
                    vm.xfze = data.xfze;
                    vm.applyTnrTyp = data.applyTnrTyp;
                    vm.ordertotal = data.ordertotal;



                }
            });
        }
    });
});