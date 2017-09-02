require(['jquery', 'util', 'Const', 'bvLayout', 'async!map'], function($, util, Const) {

    //获取重新提交的订单号
    var orderNo = util.gup('orderNo');

    var vm = util.bind({
        container: 'btInstalments',
        data: {
            areacode: '', //区编码
            payAmt: '', //支付总额
            totalAmt: '', //应还款总额
            payMtd: [],
            applyTnr: '',
            paypwd: '',
            risk: {
                longitude: '',
                latitude: '',
                area: ''
            }
        },
        filters: {
            currency: function (val) {
                return util.format(val, 'currency');
            }
        },
        methods: {
            chooseCoupon: function () {
                util.popup({
                    $element: $('#popup1', vm.$el)
                });
            },
            openCheckPassword: function (orderNo, applSeq) {
                util.modal({
                    title: '请输入支付密码',
                    clazz: 'enterPayPwd',
                    message: '<p class="hide errorInform"></p><input type="password" placeholder="请输入支付密码" class="pwd-text">',
                    close: true,
                    inline: false,
                    operates: [
                        {
                            text: '确认支付',
                            close: false,
                            click: function () {
                                $('.errorInform').hide();
                                if( util.isEmpty($(".pwd-text").val())){
                                    $('.errorInform').text("支付密码不能为空");
                                    $('.errorInform').show();
                                }else{
                                    //util.loading('支付中...', 'paying')
                                    util.loading();
                                    util.post({
                                        url: '/shunguang/commitOrder',
                                        check: true,
                                        loading: false,
                                        data: {
                                            orderNo: orderNo,
                                            applSeq: applSeq,
                                            paypwd: $(".pwd-text").val(),
                                            longitude: vm.risk.longitude,
                                            latitude: vm.risk.latitude,
                                            area: vm.risk.area
                                        },
                                        alert: false ,
                                        success: function(res){
                                            util.modal('close');
                                            util.loading('hide');
                                            util.loading(null, 'paying');
                                            // 判断订单状态
                                            var t = setInterval(function () {
                                                util.post({
                                                    url: '/queryOrderInfo',
                                                    loading: false,
                                                    alert: false,
                                                    data: {
                                                        orderNo: orderNo
                                                    },
                                                    success: function (res) {
                                                        var data = util.data(res);
                                                        if (!util.isEmpty(data) && !util.isEmpty(data.outSts)) {
                                                            // 02   03    aa   22  拒绝
                                                            // 04   05    06   23   24    成功
                                                            if (data.outSts === '02' || data.outSts === '03' || data.outSts === 'aa' || data.outSts === '22') {
                                                                // 失败
                                                                // util.modal('close');
                                                                util.loading('hide', 'paying');
                                                                clearInterval(t);
                                                                util.cache({
                                                                    $error: data.app_out_advice || '支付结果未知'
                                                                });
                                                                util.redirect({
                                                                    url: util.mix('/payByBt/payFail.html', {
                                                                        edxg: util.gup('edxg'),
                                                                        orderNo: orderNo
                                                                    }, true),
                                                                    back: false
                                                                });
                                                            } else if (data.outSts === '04' || data.outSts === '05' || data.outSts === '06' || data.outSts === '23' || data.outSts === '24') {
                                                                // 成功
                                                                // util.modal('close');
                                                                util.loading('hide', 'paying');
                                                                clearInterval(t);
                                                                util.redirect({
                                                                    // title: '支付成功',
                                                                    url: util.mix('/payByBt/paySuccess.html', {
                                                                        edxg: util.gup('edxg'),
                                                                        applSeq: applSeq
                                                                    }, true),
                                                                    back: false
                                                                });
                                                            } else {
                                                                // 审批中
                                                            }
                                                        } else {
                                                            util.loading('hide', 'paying');
                                                            // 查询支付结果失败，则跳转支付失败页面
                                                            clearInterval(t);
                                                            util.cache({
                                                                $error: '支付结果异常'
                                                            });
                                                            util.redirect({
                                                                url: util.mix('/payByBt/payFail.html', {
                                                                    edxg: util.gup('edxg'),
                                                                    orderNo: orderNo
                                                                }, true),
                                                                back: false
                                                            });
                                                        }
                                                    },
                                                    error: function (res) {
                                                        util.loading('hide', 'paying');
                                                        // 查询支付结果失败，则跳转支付失败页面
                                                        clearInterval(t);
                                                        util.cache({
                                                            $error: res && res.head ? res.head.retMsg : '支付结果异常'
                                                        });
                                                        util.redirect({
                                                            url: util.mix('/payByBt/payFail.html', {
                                                                edxg: util.gup('edxg'),
                                                                orderNo: orderNo
                                                            }, true),
                                                            back: false
                                                        });
                                                    }
                                                });
                                            }, 10000);
                                        },
                                        error: function(res){
                                            util.loading('hide');
                                            if (res && res.head && util.endsWith(res.head.retFlag,'error')) {
                                                $('.errorInform').text("支付密码输入错误");
                                                $('.errorInform').show();
                                            }else{
                                                util.modal('close');
                                                util.cache({
                                                    $error: res.head.retMsg
                                                });
                                                util.redirect({
                                                    // title: '支付失败',
                                                    url: util.mix('/payByBt/payFail.html', {
                                                        edxg: util.gup('edxg'),
                                                        orderNo: orderNo
                                                    }, true),
                                                    back: false
                                                });
                                            }
                                        }
                                    });
                                }
                            }
                        },
                        {
                            text: '忘记密码',
                            click: function () {
                                util.redirect({
                                    // title: '找回支付密码',
                                    url: util.mix('/getPayPsd/getPayPsdWay.html', {
                                        from: 'btInstalments',
                                        orderNo: orderNo,
                                        edxg: util.gup('edxg')
                                    })
                                });
                            }
                        }
                    ]
                });
            },
            payFn: function () {
                if (!vm.areacode) {
                    if (Const.init.locationFailSubmit === false) {
                        util.alert('#locationFail');
                        return;
                    } else {
                        // TODO: 临时写死370203
                        vm.areacode = '370203';
                        util.report({
                            message: '定位失败，使用默认地区'
                        });
                    }
                }
                if( !util.isEmpty(orderNo)){
                    var data={
                        flag: '1',
                        orderNo: orderNo ,
                        applyTnr: vm.applyTnr,
                        areaCode: vm.areacode
                    }
                }else{
                    var data={
                        applyTnr: vm.applyTnr,
                        areaCode: vm.areacode
                    }
                }
                util.post({
                    url: '/shunguang/saveOrder',
                    data: data,
                    success: function(res) {
                        var data = util.data(res);
                        if (data) {
                            util.cache({
                                orderNo: data.orderNo,
                                applSeq: data.applSeq
                            });
                            orderNo = data.orderNo;
                            vm.openCheckPassword(data.orderNo, data.applSeq);
                        }
                    }
                });
            },
            //什么是白条
            definitionBtFn: function(){
                util.alert('#btDescribe');
            },
            //选择期数
            changePeriodFn: function(e,applyTnr){
                $('.fenqi').removeClass('selected');
                $(e.target).parent().addClass('selected');
                $(e.target).addClass('selected');
                vm.applyTnr = applyTnr;
                util.get({
                    url: '/shunguang/gettotalAmt?applyTnr='+applyTnr ,
                    success: function (res) {
                        var data = util.data(res);
                        vm.totalAmt = data.totalAmt
                    }
                });
            }
        },
        mounted: function(){
            //预加载
            if( !util.isEmpty(orderNo)){
                var url = '/shunguang/initPayApply?flag=1&orderNo='+orderNo;
            }else{
                var url = '/shunguang/initPayApply';
            }
            util.get({
                url: url,
                success: function (res) {
                    var data = util.data(res);
                    vm.payAmt = data.payAmt;
                    vm.totalAmt = data.totalAmt;
                    vm.payMtd = data.payMtd;
                    if(!util.isEmpty(data.applyTnr)){
                        vm.applyTnr = data.applyTnr;
                    }else{
                        vm.applyTnr = data.payMtd[0].psPerdNo;
                    }
                }
            });
            app.getCurrentPosition(function (result) {
                vm.areacode = result.addressComponent.adcode;
                vm.risk.longitude = result.location.lng;
                vm.risk.latitude = result.location.lat;
                vm.risk.area = result.addressComponent.country + result.addressComponent.province + result.addressComponent.city + result.addressComponent.district;
            });

            if (util.gup('from') === 'reset') {
                var param = util.cache('orderNo,applSeq');
                if (param) {
                    this.openCheckPassword(param.orderNo, param.applSeq);
                }
            }
        }
    });
});