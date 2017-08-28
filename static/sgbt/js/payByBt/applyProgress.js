require(['jquery', 'util', 'Const'], function($, util, Const) {

    var applSeq = util.gup('applSeq');
    var orderNo = util.gup('orderNo');

    var vm = util.bind({
        container: 'applyProgress',
        data: {
            lastItem:{
                wfiNodeName: '',
                appConclusionDesc: '',
                operateTime: ''
            },
            items: []
        },
        mounted: function(){
            util.get({
                url: "/queryApprovalProcessInfo?applSeq="+ applSeq,
                success:function(res){
                    vm.items=util.data(res).reverse();
                    vm.lastItem=vm.items[0];
                }
            });
        }
    });
});