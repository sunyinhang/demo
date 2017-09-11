define([
    'vue',
    'jquery',
    'util',
    'Const'
], function (vue, $, util, Const) {
    vue.component('bv-export', {
        props: {
            url: '',
            template: '',
            data: ''
        },
        methods: {
            doExport: function(event) {
                util.download({
                    $element: $(event.target),
                    url: this.url,
                    data: data
                });
            },
            downloadTemplate: function() {
                util.download(Const.url.excel.template + '?fileName=' + this.template);
            }
        },
        /****** 模板定义 ******/
        template: util.heredoc(function() {
            /*!
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal">
                            &times;
                        </button>
                        <h4 class="modal-title">导出数据</h4>
                    </div>
                    <form id="exportExcel" class="form-horizontal">
                        <div class="modal-footer">
                            <button type="button" id="export" class="btn btn-primary" data-loading-text="处理中..." @click="doExport"><i class="iconfont icon-export"></i>导出</button>
                            <button type="button" id="template" class="btn btn-default" v-if="template" @click="downloadTemplate"><i class="iconfont icon-download"></i>模板文件下载</button>
                            <button type="button" class="btn btn-default" data-dismiss="modal"><i class="iconfont icon-cancel"></i>关闭</button>
                        </div>
                    </form>
                </div>
            </div>
             */
        })
    });
});