require(['jquery', 'util', 'Const', 'bvLayout'], function($, util, Const) {

    var vm = util.bind({
        container: 'payFail',
        data: {
            error: ''
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
        },
        mounted: function () {
            var param = util.cache('$error');
            if (param.$error) {
                this.error = param.$error;
            }
        }
    });
});