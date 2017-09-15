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
        computed: {
            adviceImage: function () {
                // custom/themes/default/images/refuse.png
                if (this.lastItem.appConclusion === '20' || this.lastItem.appConclusion === '40') {
                    return 'custom/themes/default/images/refuse.png';
                } else {
                    return 'custom/themes/default/images/pass.png';
                }
            },
            adviceName: function () {
                if (this.lastItem.appConclusion === '20') {
                    return '拒绝原因';
                } else if (this.lastItem.appConclusion === '40') {
                    return '退回原因';
                }
                return '审批意见';
            }
        },
        mounted: function(){
            util.get({
                url: "/queryApprovalProcessInfo",
                success: function (res) {
                    var data = util.data(res);
                    if (data) {
                        vm.items = util.data(res).reverse();
                        vm.lastItem = vm.items[0];
                    }
                }
            });
        }
    });
});