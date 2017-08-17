require(['jquery', 'util', 'Const', 'bvLayout'], function($, util, Const) {
    var vm = util.bind({
        container: 'btInstalments',
        data: {
            areacode: ''
        },
        methods: {
            chooseCoupon: function () {
                util.popup({
                    $element: $('#popup1', vm.$el)
                });
            },
            payFn: function () {
                util.modal({
                    title: '请输入支付密码',
                    message: '<div class="enretPwd-c"><input type="text" placeholder="请输入支付密码" class="pwd-text"></div>',
                    inline: false,
                    operates: [
                        {
                            text: '确认支付',
                            click: function () {

                            }
                        },
                        {
                            text: '忘记密码',
                            click: function () {

                            }
                        }
                    ]
                });
            },
            definitionBtFn: function(){
                util.alert('#btDefin');
            }

        },
        mounted: function(){
            //获取当前位置
            var geolocation = new BMap.Geolocation();
            geolocation.getCurrentPosition(function(r) {
                console.log(r);
                if (this.getStatus() == BMAP_STATUS_SUCCESS) {
                    console.log('success');
                    util.get({
                        url: 'https://api.map.baidu.com/geocode/v2/?location=' + r.point.lat + ',' + r.point.lng + '&output=json&ak=vUz58Gv8yMI0LuDeIzE37GnETZlLhAGm',
                        urlType: 'json',
                        dataType: 'jsonp',
                        check: true,
                        success: function(rs) {
                            console.log(rs);

                        }
                    });
                } else {
                    util.loading('close');
                    util.alert('#locationFail');
                }
            }, function(e) {
                util.loading('close');
                util.alert('#locationFail');
            }, {
                enableHighAccuracy: true
            });
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
        }
    });
});