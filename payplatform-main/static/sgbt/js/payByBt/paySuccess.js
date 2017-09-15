require(['jquery', 'util', 'Const', 'bvLayout'], function($, util, Const) {

    var applSeq = util.gup('applSeq');

    var vm = util.bind({
        container: 'paySuccess',
        data: {
        },
        methods: {
            reviewOrdersFn: function(){
                util.redirect({
                    url: '/payByBt/loanDetails.html?applSeq='+applSeq
                });
            }
        }
    });
});