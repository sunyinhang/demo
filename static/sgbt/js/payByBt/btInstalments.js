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
            paypwd: ''
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
                                            paypwd: $(".pwd-text").val()
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
                                                                    $error: '支付结果异常'
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
                                                            $error: res.head.retMsg
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
                    // util.alert('#locationFail');
                    // TODO: 临时写死370200
                    // return;
                    // util.alert('临时固定为青岛市测试');
                    vm.areacode = '370203';
                    util.report({
                        message: '定位失败，使用默认地区'
                    });
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
            if (util.gup('from') === 'reset') {
                var param = util.cache('orderNo,applSeq');
                if (param) {
                    this.openCheckPassword(param.orderNo, param.applSeq);
                }
            }
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
            //获取当前位置
            var geolocation = new BMap.Geolocation();
            geolocation.getCurrentPosition(function(r) {
                var lat=r.latitude;
                var lng=r.longitude;
                //关于状态码
                //BMAP_STATUS_SUCCESS	检索成功。对应数值“0”。
                //BMAP_STATUS_CITY_LIST	城市列表。对应数值“1”。
                //BMAP_STATUS_UNKNOWN_LOCATION	位置结果未知。对应数值“2”。
                //BMAP_STATUS_UNKNOWN_ROUTE	导航结果未知。对应数值“3”。
                //BMAP_STATUS_INVALID_KEY	非法密钥。对应数值“4”。
                //BMAP_STATUS_INVALID_REQUEST	非法请求。对应数值“5”。
                //BMAP_STATUS_PERMISSION_DENIED	没有权限。对应数值“6”。(自 1.1 新增)
                //BMAP_STATUS_SERVICE_UNAVAILABLE	服务不可用。对应数值“7”。(自 1.1 新增)
                //BMAP_STATUS_TIMEOUT	超时。对应数值“8”。(自 1.1 新增)
                if (this.getStatus() == BMAP_STATUS_SUCCESS) {
                    util.get({
                        url: 'https://api.map.baidu.com/geocoder/v2/?location=' + lat + ',' + lng + '&output=json&ak=' + Const.params.mapKey,
                        urlType: 'json',
                        dataType: 'jsonp',
                        check: true,
                        success: function(res) {
                            if (res.status === 0 && res.result && res.result.addressComponent && res.result.addressComponent.adcode) {
                                vm.areacode = res.result.addressComponent.adcode;
                                //console.log(vm.areacode);
                            }
                        }
                    });
                } else {
                    // util.loading('close');
                    util.report({
                        message: '定位失败',
                        status: this.getStatus()
                    });
                    util.alert('#locationFail');
                }
            }, function(e) {
                // util.loading('close');
                util.report({
                    message: '定位失败',
                    error: e
                });
                util.alert('#locationFail');
            }, {
                enableHighAccuracy: true
            });
        }
    });
});