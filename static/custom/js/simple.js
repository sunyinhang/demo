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
            config: {
                entityName: 'bcBankInfo',
                keys: 'cardSign',
                columns: [
                    {
                        name: 'cardSign',
                        head: 'cardSign',
                        filter: 'like'
                    },
                    {
                        name: 'cardSignLen',
                        head: 'cardSignLen',
                        filter: 'between'
                    },
                    {
                        name: 'cardNoLen',
                        head: 'cardNoLen',
                        filter: 'between'
                    },
                    {
                        name: 'cardType',
                        head: '卡类型',
                        filter: 'like'
                    },
                    {
                        name: 'cardName',
                        head: '卡名称',
                        filter: 'like'
                    },
                    {
                        name: 'bankNo',
                        head: '银行代码',
                        filter: 'like'
                    },
                    {
                        name: 'bankName',
                        head: '银行名称',
                        filter: 'like'
                    }
                ],
                select: 'checkbox',
                orders: 'cardSign'
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