define([
    'vue',
    'jquery',
    'util',
    'Const'
], function (vue, $, util, Const) {
    vue.component('bv-import', {
        props: {
            name: {
                default: 'fileSign'
            },
            title: {
                default: '导入数据'
            },
            url: '',
            template: '',
            uploadUrl: {
                default: Const.url.excel.upload
            },
            // fileSign: '',
            // default-自动modal-弹窗body-非弹窗
            layout: {
                default: 'default'
            },
            // 自定义数据填充：格式importData(preset, data)
            // preset为预留字段
            // data为默认上传文件信息
            data: ''
        },
        data: function () {
            return {
                innerLayout: this.layout,
                innerResultFile: '',
                innerUploadHint: '',
                innerUpload: {
                    originName: '',
                    fileName: '',
                    fileExt: '',
                    fileSize: '',
                    filePath: '',
                    fileSign: ''
                }
            }
        },
        beforeCreate: function () {
            this.innerLayout = util.layout();
        },
        mounted: function () {
            util.validateInit($(this.$el));
        },
        methods: {
            doImport: function(event) {
                if (!util.validate($('form', $(this.$el)))) {
                    return;
                }
                var vm = this;
                if (util.isEmpty(vm.innerUpload.fileSign || vm.innerUpload.fileName)) {
                    vm.innerUploadHint = '请先上传导入文件!';
                } else {
                    vm.innerUploadHint = '';
                    var data = vm.innerUpload;
                    if (vm.data && util.type(vm.data) === 'function') {
                        // 第一个参数预留
                        data = vm.data.call(null, '', data);
                    }
                    util.post({
                        $element: $(event.target),
                        url: vm.url,
                        data: data,
                        success: function(data) {
                            data = util.data(data);
                            if (data) {
                                vm.innerResultFile = data;
                                vm.innerUploadHint = '导入完成，若有导入出错数据请下载检查结果进行重新导入!';
                            } else {
                                vm.innerUploadHint = '导入成功';
                            }
                        }
                    });
                }
            },
            downloadResult: function() {
                util.download(Const.url.excel.download + '?fileName=' + util.encode(this.innerResultFile));
            },
            downloadTemplate: function() {
                if (this.template) {
                    util.download(Const.url.excel.template + '?fileName=' + util.encode(this.template));
                }
            }
        },
        /****** 模板定义 ******/
        template: util.heredoc(function() {
            /*
             <div :class="{'modal-dialog': innerLayout === 'modal'}">
                <div :class="{'modal-content': innerLayout === 'modal'}">
                    <div class="modal-header" v-if="innerLayout === 'modal'">
                        <button type="button" class="close" data-dismiss="modal">
                            &times;
                        </button>
                        <h4 class="modal-title" v-text="title"></h4>
                    </div>
                    <h4 v-if="innerLayout === 'body'" v-text="title"></h4>
                    <form class="form-horizontal">
                        <div class="modal-body">
                            <div class="col-md-12">
                                <slot name="layoutBefore" />
                            </div>
                            <div class="col-md-12">
                                <component is="bv-upload" :key="name + '-upload'" :entity="innerUpload" v-bind="{name: name, url: uploadUrl}"></component>
                            </div>
                        </div>
                        <div class="modal-footer">
                            <div>
                                <span class="text-danger" v-show="innerUploadHint" v-text="innerUploadHint"></span>
                            </div>
                            <button type="button" class="btn btn-warning" v-show="innerResultFile" @click="downloadResult"><i class="iconfont icon-download"></i>下载检查结果</button>
                            <button type="button" class="btn btn-primary" data-loading-text="处理中..." @click="doImport"><i class="iconfont icon-import"></i>导入</button>
                            <button type="button" class="btn btn-default" v-if="template" @click="downloadTemplate"><i class="iconfont icon-download"></i>模板文件下载</button>
                            <button type="button" class="btn btn-default" data-dismiss="modal" v-if="innerLayout === 'modal'"><i class="iconfont icon-cancel"></i>关闭</button>
                        </div>
                    </form>
                </div>
            </div>
             */
        })
    });
});