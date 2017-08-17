require(['jquery', 'util', 'Const', 'bvList'], function($, util, Const) {
    var vm = util.bind({
        container: 'getPayPsdWay',
        data: {
            config: {
                items: [
                    {
                        title: '我记得支付密码',
                        href: '/getPayPsd/byRembPayPsd.html'
                    },
                    {
                        title: '我忘记支付密码了',
                        href: '/getPayPsd/byRlNmCertif_PayPsd.html'
                    }
                ]
            }
        }
    });
});