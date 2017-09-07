define([
    'vue',
    'jquery',
    'util',
    'Const'
], function (vue, $, util, Const) {
    vue.component('bv-upload', {
        props: {
            entity: '',
            name: '',
            clazz: '',
            browserTitle: {
                default: '请选择文件：'
            },
            browserButtonText: {
                default: '浏览'
            },
            showDetail: {
                default: true
            },
            // false or default image url
            preview: false,
            url: '',
            initParam: undefined,
            // fileType,subType,keyId,fileSource
            filters: {
                default: function () {
                    return {};
                }
            },
            onReady: '',
            onUpload: ''
        },
        data: function () {
            return {
                innerEntity: this.entity || {},
                innerClass: this.clazz,
                innerUrl: this.url,
                innerFileName: '',
                innerFileSize: '',
                innerInProgress: false,
                innerProgress: 0,
                innerProgressInfo: '',
                innerUploadError: ''
            }
        },
        computed: {
            innerFileSizeFormat: function () {
                return util.format(this.innerFileSize, 'file');
            }
        },
        created: function () {
            if (!this.innerUrl) {
                this.innerUrl = Const.url.upload.upload;
            }
            if (this.initParam) {
                this.innerUrl = util.mix(this.innerUrl, this.initParam);
            }
        },
        mounted: function () {
            if (!this.innerEntity[this.name] && this.preview) {
                $('img', this.$el).attr('src', this.preview);
            }
            var vm = this;
            if (util.type(vm.onReady) === 'function') {
                vm.onReady.call(null, vm.innerEntity, vm.$el);
            } else {
                vm.$emit('on-ready', vm.innerEntity, vm.$el);
            }
            //实例化一个上传对象
            var uploader = new plupload.Uploader({
                browse_button: $('#browser', vm.$el)[0],
                url: util.url(vm.innerUrl),
                flash_swf_url: util.url(Const.url.upload.swf),
                multipart: false,
                multi_selection: false,
                filters: vm.filters
            });
            uploader.init();
            uploader.bind('FilesAdded', function (uploader, files) {
                if (files && files.length == 1) {
                    uploader.refresh();
                    vm.innerFileName = files[0].name;
                    vm.innerFileSize = files[0].size;
                    vm.innerUploadError = '';
                    vm.innerInProgress = true;
                    uploader.start();
                }
            });
            uploader.bind('UploadProgress', function (uploader, file) {
                vm.innerProgress = file.percent + '%';
            });
            uploader.bind('FileUploaded', function (uploader, file, responseObject) {
                vm.innerInProgress = false;

                if (file.percent === 100) {
                    vm.innerProgressInfo = file.percent + '%';
                } else {
                    vm.innerProgressInfo = file.percent + '%';
                }
                vm.innerProgress = 0;
                var response = responseObject.response;
                if (util.type(response) === 'string') {
                    response = $.parseJSON(response);
                }

                if (util.type(vm.onUpload) === 'function') {
                    vm.onUpload.call(null, vm.innerEntity, $(vm.$el), util.data(response));
                } else {
                    vm.$emit('on-uploaded', vm.innerEntity, $(vm.$el), util.data(response));
                }

                var data = util.data(response);
                vm.innerEntity.originName = data.originName;
                vm.innerEntity.fileName = data.fileName;
                vm.innerEntity.fileExt = data.fileExt;
                vm.innerEntity.fileSize = data.fileSize;
                vm.innerEntity.filePath = data.filePath;
                vm.innerEntity.fileSign = data.fileSign;
            });
            uploader.bind('Error', function (uploader, error) {
                uploader.refresh();
                vm.innerInProgress = false;
                vm.innerUploadError = error.file.name + '-' + error.message;
            });
        },
        /****** 模板定义 ******/
        template: util.heredoc(function() {
            /*!
            <div class="bv-upload" :class="preview && 'thumbnail'">
                <img v-if="preview" />
                <div class="form-group" :class="innerClass">
                    <label class="col-sm-3 control-label" v-text="browserTitle" v-if="browserTitle"></label>
                    <div :class="browserTitle ? 'col-sm-9' : 'col-sm-12'">
                        <button type="button" id="browser" class="btn btn-default"><i class="iconfont icon-browse"></i>{{browserButtonText}}</button>
                        <span v-if="innerFileName" v-show="showDetail">
                            文件名：<span v-text="innerFileName"></span>
                            大小：<span v-text="innerFileSizeFormat"></span>
                            上传进度：<span v-text="innerProgressInfo" v-show="!innerUploadError"></span>
                            <span class="text-danger" v-show="innerUploadError" v-text="innerUploadError"></span>
                        </span>
                        <div class="progress bv-progress-line bv-progress-upload" v-show="innerInProgress">
                            <div class="progress-bar" :style="{width: innerProgress}">
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            */
        })
    });
});