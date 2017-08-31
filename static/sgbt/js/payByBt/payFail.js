require(['jquery', 'util', 'Const', 'bvLayout'], function($, util, Const) {

    var vm = util.bind({
        container: 'payFail',
        data: {
        },
        methods: {
            payFailFn: function(){
                var url = '/payByBt/btInstalments.html';
                util.redirect({
                    // title: title,
                    url: util.mix(url, {
                        from: 'reset',
                        orderNo: util.gup('orderNo'),
                    }, true)
                });
            }
        }
    });
});