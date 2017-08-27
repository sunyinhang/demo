require(['jquery', 'util', 'Const'], function($, util, Const) {
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
                url: "/queryApprovalProcessInfo",
                success:function(res){
                    var data = util.data(res);
                    if (data && data.length > 0) {
                        vm.items = data.reverse();
                        vm.lastItem = vm.items[0];
                    }
                }
            });
        }
    });
});