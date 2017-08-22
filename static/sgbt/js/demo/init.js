require(['jquery', 'util', 'Const', 'bvForm'], function($, util, Const) {
    var vm = util.bind({
        container: 'demoInit',
        data: {
            tags: {
                formKey: 'demoInitForm'
            },
            formConfig: {
                layout: 'inline',
                columns: [
                    {
                        head: 'userId',
                        name: 'userId'
                    }
                ],
                operates: [
                    {
                        text: '保存',
                        layout: 'primary',
                        click: function (event, editType, entity) {
                            util.post({
                                url: '/shunguang/edApplytest',
                                data: util.mix({
                                    token: util.gup('token'),
                                    channel: Const.rest.headers.channel,
                                    channelNo: Const.rest.headers.channelNo
                                }, entity)
                            });
                        }
                    }
                ]
            }
        }
    });
});