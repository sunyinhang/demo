require(['jquery', 'util', 'Const', 'bvList'], function($, util, Const) {
    var vm = util.bind({
        container: 'getPayPsdWay',
        data: {
            config: {
                items: [
                    {
                        title: '我记得支付密码',
                        href: util.mix('/getPayPsd/byRembPayPsd.html', {
                            from: util.gup('from'),
                            orderNo: util.gup('orderNo'),
                            edxg: util.gup('edxg')
                        }, true)
                    },
                    {
                        title: '我忘记支付密码了',
                        href: util.mix('/getPayPsd/byRlNmCertif_PayPsd.html', {
                            from: util.gup('from'),
                            orderNo: util.gup('orderNo'),
                            edxg: util.gup('edxg')
                        }, true)
                    }
                ]
            }
        }
    });
});