/*
主页面引用
*/
var simple;
require(['vue', 'jquery', 'util', 'Const', 'bootstrap', 'toastr', 'md5', 'scroll', 'validation', 'keySwitch',
            'bvStatic', 'bvTextfield', 'bvTextarea', 'bvHidden', 'bvDate', 'bvSelect', 'bvRadio', 'bvCheckbox', 'bvAuto',
            'bvModal', 'bvTree', 'bvTabs', 'bvForm', 'bvTable'], function(vue, $, util, Const) {
    // 初始化Const、部分组件定义及ajax error公共处理
    util.init();

    simple = util.bind({
        container: 'simple',
        data: {
            tags: {
                tableId: 'tagTableColumnTitleExampleTable'
            },
            config: {
                select: 'checkbox',
                entityName: 'demoCity',
                keys: 'cityCode',
                orders: 'cityCode',
                columns: [
                    {
                        name: 'cityCode',
                        head: '城市代码',
                        filter: {
                            type: 'textfield'
                        }
                    },
                    {
                        name: 'cityName',
                        head: '城市名称',
                        filter: {
                            type: 'date',
                            operate: 'between'
                        },
                        config: {
                            format: 'timestamp',
                            period: {
                                options: [
                                    {
                                        code: '60,seconds',
                                        desc: '一分钟'
                                    },
                                    {
                                        code: '120,seconds',
                                        desc: '两分钟'
                                    },
                                    {
                                        code: '5,minutes',
                                        desc: '五分钟内'
                                    },
                                    {
                                        code: '10,minutes',
                                        desc: '十分钟内'
                                    },
                                    {
                                        code: '1,hour',
                                        desc: '一小时内'
                                    }
                                ]
                            }
                        }
                    },
                    {
                        name: 'parentCode',
                        head: '上级城市'
                    }
                ],
                operates: [
                    {
                        text: '新增',
                        icon: 'icon-add',
                        show: 'none',
                        type: 'insert',
                        url: 'modules/demo/editDemoCity.html'
                    },
                    {
                        text: '修改',
                        icon: 'icon-edit',
                        show: 'one',
                        type: 'update',
                        url: 'modules/demo/editDemoCity.html'
                    },
                    {
                        text: '删除',
                        icon: 'icon-delete',
                        show: 'oneOrMore',
                        type: 'delete'
                    }
                ]
            }
        }
    });

    util.tooltip($(document));
    util.tooltip($(document), 'data-title');
    $(document).on('click', '[title], [data-title]', function() {
        $(this).tooltip('hide');
    });
    $(document).on('click', '[data-href]', function() {
        util.open($(this).attr('data-href'));
    });
    $(document).on('click', '[data-modal]', function() {
        util.modal({
            url: $(this).attr('data-modal')
        });
    });
    $('#download').load(function(event) {
        util.show({
            title: '文件下载失败',
            message: $(event.target).contents().find("body").html(),
            level: 'error'
        });
    });
    $.fn.keySwitch();
});