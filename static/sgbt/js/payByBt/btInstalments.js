require(['jquery', 'util', 'Const', 'bvLayout', 'async!map'], function($, util, Const) {
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
            payFn: function () {
                util.post({
                    url: '/shunguang/saveOrder',
                    data:{
                        applyTnr: vm.applyTnr,
                        areaCode: '370203'
                    },
                    success: function(res) {
                        var data = util.data(res);
                        if (data) {
                            util.cache({
                                orderNo: data.orderNo,
                                applSeq: data.applSeq
                            });
                            this.openCheckPassword(data.orderNo, data.applSeq);
                        }
                    }
                });
            },
            openCheckPassword: function (orderNo, applSeq) {
                util.modal({
                    title: '请输入支付密码',
                    clazz: 'xxx',
                    message: '<div class="enretPwd-c"><input type="password" placeholder="请输入支付密码" class="pwd-text"></div>',
                    inline: false,
                    operates: [
                        {
                            text: '确认支付',
                            click: function () {
                                util.post({
                                    url: '/shunguang/commitOrder',
                                    data: {
                                        orderNo: orderNo,
                                        applSeq: applSeq,
                                        paypwd: $(".pwd-text").val()
                                    },
                                    success: function(res){
                                        util.redirect({
                                            title: '支付成功',
                                            url: '/getPayPsd/paySuccess.html',
                                            back: false
                                        });
                                    },
                                    error: function(res){
                                        util.redirect({
                                            title: '支付成功',
                                            url: '/getPayPsd/payFail.html',
                                            back: false
                                        });
                                    }
                                });
                            }
                        },
                        {
                            text: '忘记密码',
                            click: function () {
                                util.redirect({
                                    title: '找回支付密码',
                                    url: '/getPayPsd/getPayPsdWay.html',
                                    back: false
                                });
                            }
                        }
                    ]
                });
            },
            //什么是白条
            definitionBtFn: function(){
                util.alert('#btDefin');
            },
            //选择期数
            changePeriodFn: function(e,applyTnr){
                $('.fenqi').removeClass('selected');
                $(e.target).parent().addClass('selected');
                $(e.target).addClass('selected');
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
            util.get({
                url: '/shunguang/initPayApply',
                success: function (res) {
                    var data = util.data(res);
                    vm.payAmt = '￥'+data.payAmt;
                    vm.totalAmt = data.totalAmt;
                    vm.payMtd = data.payMtd;
                    vm.applyTnr = data.payMtd[0].psPerdNo;

                    /*util.cache({

                     });*/
                    /*util.redirect({
                     title: '实名绑卡',
                     url: '/applyQuota/checkIdCardB.html',
                     back: false
                     });*/
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
                            }
                        }
                    });
                } else {
                    // util.loading('close');
                    util.alert('#locationFail');
                }
            }, function(e) {
                // util.loading('close');
                util.alert('#locationFail');
            }, {
                enableHighAccuracy: true
            });
        }
    });
});