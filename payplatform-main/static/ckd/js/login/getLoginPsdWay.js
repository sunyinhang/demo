require(['jquery', 'util', 'Const', 'bvList'], function($, util, Const) {
    var vm = util.bind({
        container: 'getLoginPsdWay',
        data: {
            config: {
                items: [
                    {
                        title: '验证码找回',
                        href: '/login/byVldcode.html'
                    },
                    {
                        title: '实名认证找回',
                        href: '/login/byRlNmCertif_loginPsd.html'
                    }
                ]
            }
        }
    });
});