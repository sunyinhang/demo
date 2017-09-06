require(['jquery', 'util', 'Const', 'bvForm'], function($, util, Const) {
    var columns = [];
    var params = '';
    util.get({
        url: '${root}/api/demo/allCache',
        async: false,
        success: function (res) {
            var data = util.data(res);
            if (data) {
                for (var p in data) {
                    columns.push({
                        name: p,
                        head: p,
                        config: {
                            value: data[p]
                        }
                    });
                    if (params) {
                        params += ',';
                    }
                    params += p;
                }
            }
        }
    });
    var vm = util.bind({
        container: 'demoAllCache',
        data: {
            tags: {
                formKey: 'demoAllCacheForm'
            },
            formConfig: {
                // layout: 'inline',
                columns: columns,
                operates: [
                    {
                        text: '刷新',
                        click: function (event, editType, entity) {
                            util.get({
                                url: '${root}/api/demo/allCache',
                                success: function (res) {
                                    var data = util.data(res);
                                    if (data) {
                                        util.refresh({
                                            vm: util.vm(vm, vm.tags.formKey),
                                            entity: util.data(res)
                                        });
                                    }
                                }
                            });
                        }
                    },
                    {
                        text: '修改保存',
                        layout: 'primary',
                        click: function (event, editType, entity) {
                            util.cache(entity);
                            /*util.post({
                             url: '/cache',
                             data: util.mix({
                             type: 'set',
                             params: params
                             }, entity),
                             success: function () {
                             util.alert('成功');
                             }
                             });*/
                        }
                    }
                ]
            }
        }
    });
});