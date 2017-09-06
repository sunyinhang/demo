define([
    'vue',
    'jquery',
    'util',
    'Const'
], function (vue, $, util, Const) {
    /**
     form表单格式比较灵活
     layoutCols默认为6，即两列模式，可以设置的值有：12（每行1列）6（每行2列）,4（每行3列）,3（每行4列）,其他值目前无效
     columns中每列可以设置属性edit.cols，表示宽度，可以设置的值有12（整行）,6（半行），其他值目前无效
     */
    vue.component('bv-form', {
        props: {
            /****** 接口属性 ******/
            name: '',
            clazz: '',
            css: '',
            size: '',
            attr: '',
            title: '',
            closeTitle: {
                default: '关闭'
            },
            // insert update
            editType: '',

            // 表单是否允许折叠
            collapse: {
                default: false
            },
            // 仅支持12,6,4,3
            layoutCols: {
                default: 6
            },
            // 默认空，支持inline-不居中显示
            operateLayout: '',
            entityName: '',
            saveUrl: '',
            insertUrl: '',
            updateUrl: '',
            keys: '',
            // 支持guid
            keyGenerator: '',
            entityDefaults: {
                default: function () {
                    return {};
                }
            },
            initEntity: {},
            columns: {
                default: function () {
                    return [];
                }
            },
            operates: {
                default: function () {
                    return [];
                }
            },

            initData: false,
            initUrl: '',
            initParam: '',
            initParamList: {
                default: function () {
                    return [];
                }
            },

            /****** 特殊属性 ******/
            ///sysCode: '',

            /****** 接口方法 ******/
            // formOnChange: undefined,
            // formOnSave: undefined,
            // formOnSaveSuccess: undefined,
            extraParams: ''
        },
        data: function () {
            return {
                innerClass: this.clazz,
                innerStyle: this.css,
                innerTitle: this.title,
                innerEditType: this.editType,
                innerVisible: true,
                innerLayout: '',
                innerSize: this.size,
                innerLayoutClass: '',
                innerColumns: [],
                innerEntity: {},
                innerInitEntity: util.mix(this.entityDefaults, this.initEntity) || {},
                // 主键
                innerKeys: {},
                // 是否自定义保存
                // 顶部按钮
                innerHeaderOperates: [],
                // 底部按钮
                innerFooterOperates: []
            }
        },
        beforeCreate: function () {
            // 内部用
            this.localIsCustomSave = false;
            // 后台校验
            this.localCheck = [];
        },
        created: function () {

        },
        mounted: function() {
            // 编辑类型初始化
            if (!this.innerEditType) {
                this.innerEditType = util.gup('type');
                if (!this.innerEditType) {
                    this.innerEditType = 'insert';
                }
            }
            // 页面布局及大小初始化
            if (!this.innerLayout) {
                this.innerLayout = util.layout($(this.$el));
            }
            if (!this.innerSize && this.innerLayout === 'modal') {
                if (this.layoutCols < 12) {
                    this.innerSize = 'lg';
                }
            }
            if (this.layoutCols === 12 || this.layoutCols === 6) {
                this.innerLayoutClass = 'col-md-' + this.layoutCols;
            } else if (this.layoutCols === 4) {
                this.innerLayoutClass = 'col-md-6 col-lg-4';
            } else if (this.layoutCols === 3) {
                this.innerLayoutClass = 'col-sm-6 col-md-4 col-lg-3';
            } else {
                this.innerLayoutClass = 'col-md-6';
            }

            // initParam合并到initParamList
            if (!util.isEmpty(this.initParam)) {
                for (var p in this.initParam) {
                    this.initParamList.push({
                        name: p,
                        operate: '=',
                        value: this.initParam[p]
                    });
                }
            }
            // 初始数据
            if (this.initData) {
                this.innerEditType = 'init';
                if (!this.initUrl) {
                    this.initUrl = Const.url.form.init;
                }
                var vm = this;
                util.post({
                    url: vm.initUrl,
                    async: false,
                    data: {
                        entityName: vm.entityName,
                        initParamList: vm.initParamList
                    },
                    success: function(res) {
                        vm.innerInitEntity = util.data(res);
                    }
                });
            }

            // 列定义
            if (this.columns.length > 0) {
                for (var i=0; i<this.columns.length; i++) {
                    var formColumn = util.clone(this.columns[i]);
                    if (!formColumn.config) {
                        formColumn.config = {};
                    }
                    if (!formColumn.edit) {
                        formColumn.edit = {
                        };
                    }
                    if ((this.innerEditType === 'update' || this.innerEditType === 'init') && formColumn.edit.condition === 'insert') {
                        formColumn.edit.type = 'static';
                    }
                    if (!formColumn.edit.type) {
                        formColumn.edit.type = 'textfield';
                    }
                    if (formColumn.edit.type === 'ignore') {
                        continue;
                    }
                    if (formColumn.edit.check) {
                        this.localCheck.push({
                            name: formColumn.name,
                            type: formColumn.edit.check.type,
                            describe: formColumn.edit.check.describe,
                            initParamList: util.clone(formColumn.edit.check.initParamList)
                        });
                    }
                    if (!formColumn.type) {
                        formColumn.type = 'bv-' + formColumn.edit.type;
                    }
                    formColumn.config.from = 'form';
                    var preset = formColumn.name;
                    if (formColumn.config.attr && util.type(formColumn.config.attr.preset) != 'undefined') {
                        preset = formColumn.config.attr.preset;
                        delete formColumn.config.attr.preset;
                    }
                    formColumn.config.attr = util.mix(formColumn.config.attr, util.attr(formColumn.name, formColumn.config.attr && preset, formColumn.config.attr));
                    if (!formColumn.config.attr) {
                        formColumn.config.attr = {};
                    }
                    if (!util.isEmpty(formColumn.config.validate)) {
                        formColumn.config.attr = util.mix(formColumn.config.attr, util.validateMix(formColumn.config.validate));
                        delete formColumn.config.validate;
                    }
                    if (util.type(formColumn.hide) === 'undefined') {
                        if (formColumn.edit && formColumn.edit.type === 'hidden') {
                            formColumn.hide = true;
                        } else {
                            formColumn.hide = false;
                        }
                    }

                    // 是否占多列
                    // 仅支持12,6
                    if (formColumn.edit.cols) {
                        if (!formColumn.head && formColumn.edit.cols === 12) {
                            formColumn.labelInputClass = 'col-sm-12';
                        }
                        if (formColumn.edit.cols === 12) {
                            formColumn.layoutClass = 'col-sm-12 col-md-12';
                            if (this.layoutCols === 3) {
                                formColumn.labelLayoutClass = 'col-sm-2 col-md-1 col-md-1x col-lg-1';
                                if (!formColumn.labelInputClass) {
                                    formColumn.labelInputClass = 'col-sm-10 col-md-11 col-md-11y col-lg-11';
                                }
                            } else if (this.layoutCols === 4) {
                                formColumn.labelLayoutClass = 'col-sm-4 col-md-2 col-lg-1 col-lg-1x';
                                if (!formColumn.labelInputClass) {
                                    formColumn.labelInputClass = 'col-sm-8 col-md-10 col-lg-11 col-lg-11y';
                                }
                            } else if (this.layoutCols === 6) {
                                formColumn.labelLayoutClass = 'col-sm-4 col-md-2 col-lg-2';
                                if (!formColumn.labelInputClass) {
                                    formColumn.labelInputClass = 'col-sm-8 col-md-10 col-lg-10';
                                }
                            }
                        } else if (formColumn.edit.cols === 6) {
                            if (this.layoutCols === 3) {
                                formColumn.layoutClass = 'col-sm-12 col-md-8 col-lg-6';
                                formColumn.labelLayoutClass = 'col-sm-2 col-md-2';
                                if (!formColumn.labelInputClass) {
                                    formColumn.labelInputClass = 'col-sm-10 col-md-10';
                                }
                            } else if (this.layoutCols === 4) {
                                formColumn.layoutClass = 'col-sm-12 col-md-6 col-lg-8';
                                formColumn.labelLayoutClass = 'col-sm-4 col-md-4 col-lg-2';
                                if (!formColumn.labelInputClass) {
                                    formColumn.labelInputClass = 'col-sm-8 col-md-8 col-lg-10';
                                }
                            } else if (this.layoutCols === 6) {
                                formColumn.layoutClass = 'col-sm-12 col-md-6';
                                formColumn.labelLayoutClass = 'col-sm-4 col-md-4';
                                if (!formColumn.labelInputClass) {
                                    formColumn.labelInputClass = 'col-sm-8 col-md-8';
                                }
                            }
                        }
                    } else {
                        if (this.layoutCols === 12) {
                            formColumn.layoutClass = 'col-sm-12 col-md-12';
                            formColumn.labelLayoutClass = 'col-sm-4 col-md-3 col-lg-2';
                            if (!formColumn.labelInputClass) {
                                formColumn.labelInputClass = 'col-sm-8 col-md-9 col-lg-10';
                            }
                        }
                    }
                    if (!formColumn.head && formColumn.edit.cols === 12) {
                        formColumn.layoutClass += ' bv-col-fill';
                    }

                    this.innerColumns.push(formColumn);
                }
            }

            if (this.innerEditType === 'insert') {
                for (var i=0; i<this.innerColumns.length; i++) {
                    this.innerInitEntity[this.innerColumns[i].name] = null;
                }
            } else if (this.innerEditType === 'update') {
                this.innerInitEntity = util.mix(this.innerInitEntity, util.data('modal'));
            }/* else if (this.innerEditType === 'init') {
             for (var i=0; i<this.innerColumns.length; i++) {
             if (this.innerColumns[i].edit.type === 'static') {
             this.innerInitEntity[this.innerColumns[i].name + 'Static'] = null;
             }
             }
             }
             if (this.innerEditType === 'update') {
             this.innerInitEntity = util.mix(this.innerInitEntity, util.data('modal'));
             }*/
            util.clone(this.innerEntity, this.innerInitEntity);
            // this.innerEntity = this.innerInitEntity;

            if (this.operates && this.operates.length > 0) {
                for (var i=0; i<this.operates.length; i++) {
                    if (util.type(this.operates[i].show) === 'undefined') {
                        this.operates[i].show = true;
                    }
                    if (!this.operates[i].position || this.operates[i].position === 'footer') {
                        this.innerFooterOperates.push(this.operates[i]);
                    } else if (this.operates[i].position === 'header') {
                        this.innerHeaderOperates.push(this.operates[i]);
                    }
                }
            }

            util.validateInit($('form', $(this.$el)));
        },
        methods: {
            isColumnVisible: function(column) {
                if (!column || column.hide === true || column.show === false) {
                    return false;
                }
                var hide;
                if (util.type(column.hide) === 'undefined' || column.hide === false) {
                    hide = false;
                } else if (util.type(column.hide) === 'function') {
                    hide = column.hide.call(null, this.innerEntity);
                }
                if (hide) {
                    return false;
                }
                var show;
                if (util.type(column.show) === 'undefined' || column.show === true) {
                    return true;
                } else if (util.type(column.show) === 'function') {
                    return column.show.call(null, this.innerEntity);
                }
            },
            checkColumnAttr: function(el, type) {
                if (!type) {
                    return el.edit.type;
                } else if (type === 'for') {
                    if (!el.edit.type || el.edit.type === 'textfield' || el.edit.type === 'auto' || el.edit.type === 'select') {
                        return {'for': el.name};
                    }
                    return {'for': ''};
                }
            },
            isRequired: function(validate, attr) {
                return util.isRequired(validate, attr);
            },
            hideColumn: function(name) {
                for (var i=0; i<this.innerColumns.length; i++) {
                    if (this.innerColumns[i].name === name) {
                        this.innerColumns[i].hide = true;
                        break;
                    }
                }
            },
            showColumn: function(name) {
                for (var i=0; i<this.innerColumns.length; i++) {
                    if (this.innerColumns[i].name === name) {
                        this.innerColumns[i].hide = false;
                        break;
                    }
                }
            },
            hideOperate: function(name) {
                var find = false;
                for (var i=0; i<this.innerFooterOperates.length; i++) {
                    if (this.innerFooterOperates[i].name === name) {
                        this.innerFooterOperates[i].show = false;
                        find = true;
                        break;
                    }
                }
                if (!find) {
                    for (var i=0; i<this.innerHeaderOperates.length; i++) {
                        if (this.innerHeaderOperates[i].name === name) {
                            this.innerHeaderOperates[i].show = false;
                            break;
                        }
                    }
                }
            },
            showOperate: function(name) {
                var find = false;
                for (var i=0; i<this.innerFooterOperates.length; i++) {
                    if (this.innerFooterOperates[i].name === name) {
                        this.innerFooterOperates[i].show = true;
                        find = true;
                        break;
                    }
                }
                if (!find) {
                    for (var i=0; i<this.innerHeaderOperates.length; i++) {
                        if (this.innerHeaderOperates[i].name === name) {
                            this.innerHeaderOperates[i].show = true;
                            break;
                        }
                    }
                }
            },
            /*isColumnVisible: function(el) {
                if (el.hide) {
                    return false;
                }
                if (util.type(el.edit.type) !== 'function') {
                    return true;
                }
                var configType = el.edit.type.call(null, this.innerEntity);
                return configType !== 'hide';
            },*/
            isButtonVisible: function(data) {
                if (!data || data.show === false) {
                    return false;
                }
                if (!data.show || data.show === true) {
                    return true;
                }
                if (data.show === 'insert') {
                    return this.innerEditType === 'insert';
                } else if (data.show === 'update') {
                    return this.innerEditType === 'update';
                } else if (util.type(data.show) === 'function') {
                    return data.show.call();
                }
            },
            click: function(event, operate) {
                if (operate) {
                    if (operate.type) {
                        if (operate.type === 'save') {
                            if (operate.prepare && util.type(operate.prepare) === 'function') {
                                operate.prepare.call(null, this.innerEntity);
                            }
                            if (!util.validate($('form', $(this.$el)))) {
                                return;
                            }

                            var insertUrl = '';
                            var updateUrl = '';
                            if (this.innerEditType === null || this.innerEditType === 'insert') {
                                // 新增
                                if (this.insertUrl || this.saveUrl) {
                                    this.localIsCustomSave = true;
                                    insertUrl = this.insertUrl || this.saveUrl
                                } else {
                                    insertUrl = Const.url.form.insert;
                                }
                            } else {
                                // 修改
                                if (this.updateUrl || this.saveUrl) {
                                    this.localIsCustomSave = true;
                                    updateUrl = this.updateUrl || this.saveUrl;
                                } else {
                                    updateUrl = Const.url.form.update;
                                }
                            }

                            if (this.innerEditType === null || this.innerEditType === 'insert') {
                                // 新增
                                var formData;
                                if (this.localIsCustomSave) {
                                    formData = util.clone(this.innerEntity);
                                } else {
                                    formData = {
                                        entityName: this.entityName,
                                        define: util.clone(this.innerEntity),
                                        generator: this.keyGenerator
                                    };
                                    if (this.localCheck.length > 0) {
                                        for (var i=0; i<this.localCheck.length; i++) {
                                            this.localCheck[i].value = this.innerEntity[this.check[i].name];
                                        }
                                        formData.checkList = this.localCheck;
                                    }
                                }
                                util.post({
                                    $element: $(event.target),
                                    url: insertUrl,
                                    ///sysCode: this.sysCode,
                                    data: formData,
                                    close: true,
                                    success: operate.success
                                });
                            } else {
                                // 修改
                                var formData;
                                if (this.localIsCustomSave) {
                                    formData = util.clone(this.innerEntity);
                                } else {
                                    formData = {
                                        entityName: this.entityName,
                                        define: util.clone(this.innerEntity),
                                        keyValues: this.handleKeyValues()
                                    };
                                }
                                util.post({
                                    $el: $(event.target),
                                    url: updateUrl,
                                    ///sysCode: this.sysCode,
                                    data: formData,
                                    close: true,
                                    success: operate.success
                                });
                            }
                        }
                    } else if (operate.click && util.type(operate.click) === 'function') {
                        if (util.isTrue(operate.validate, true)) {
                            if (!util.validate($(event.target).closest('form'))) {
                                return;
                            }
                        }
                        if (operate.prepare && util.type(operate.prepare) === 'function') {
                            operate.prepare.call(null, this.innerEntity);
                        }
                        operate.click.call(null, $(event.target), this.innerEditType, this.innerEntity, this.extraParams);
                    }
                }
            },
            handleKeyValues: function () {
                var keyValues = '';
                if (this.keys) {
                    this.innerKeys = util.replaceAll(this.keys, ' ', '').split(',');
                    keyValues = {};
                    for (var j=0; j<this.innerKeys.length; j++) {
                        keyValues[this.innerKeys[j]] = this.innerEntity[this.innerKeys[j]];
                    }
                }
                return keyValues;
            }
        },
        /****** 内部属性 ******/
        template: util.heredoc(function() {
            /*!
            <div class="bv-form" :class="[{'modal-dialog': innerLayout === 'modal', 'form-collapse': collapse}, 'modal-' + innerSize, innerClass]" :style="innerStyle">
                <div :class="{'modal-content': innerLayout === 'modal'}">
                    <div class="modal-header" v-if="innerLayout === 'modal'">
                        <button type="button" class="close" data-dismiss="modal">
                            &times;
                        </button>
                        <h4 class="modal-title" v-text="innerTitle"></h4>
                    </div>
                    <h4 class="form-title" v-show="innerLayout === 'body'">
                        <a href="javascript:;" @click="innerVisible = !innerVisible" v-if="collapse"><i class="iconfont" :class="innerVisible ? 'icon-more' : 'icon-gt'"></i><span v-text="innerTitle"></span></a>
                        <span v-text="innerTitle" v-if="!collapse"></span>
                        <span class="pull-right" v-show="innerHeaderOperates.length > 0">
                            <button v-for="el in innerHeaderOperates" type="button" class="btn" v-if="el.text"
                                    v-bind="{id: el.id, 'data-loading-text': el.loading || ''}" :class="el.clazz || 'btn-default'"
                                    @click="click($event, el)" v-show="isButtonVisible(el)">
                                <i class="iconfont" :class="el.icon" v-if="el.icon"></i>{{el.text}}
                            </button>
                        </span>
                    </h4>
                    <form class="form-horizontal" v-bind="attr" v-show="innerVisible">
                        <div :class="{'modal-body': innerLayout === 'modal', 'form-body': innerLayout === 'body'}">
                            <div v-for="el in innerColumns" :class="!el.hide && (el.layoutClass || innerLayoutClass)" v-show="isColumnVisible(el)">
                                <div class="form-group">
                                    <label class="control-label" v-bind="checkColumnAttr(el, 'for')" :class="el.labelLayoutClass || 'col-sm-4'" v-if="el.head">
                                        <i class="required iconfont icon-required" v-if="isRequired(el.config.validate, el.config.attr)"></i>{{el.head}}：
                                    </label>
                                    <div :class="el.labelInputClass || 'col-sm-8'">
                                        <component :is="el.type" :name="el.name" :from="'form'" :entity="innerEntity" v-bind="el.config"></component>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div :class="{'modal-footer': innerLayout === 'modal', 'form-footer': innerLayout === 'body' && operateLayout !== 'inline'}">
                            <button v-for="el in innerFooterOperates" type="button" class="btn" v-if="el.text"
                                    v-bind="{id: el.id, 'data-loading-text': el.loading || ''}" :class="el.clazz || 'btn-default'"
                                    @click="click($event, el)" v-show="isButtonVisible(el)">
                                <i class="iconfont" :class="el.icon" v-if="el.icon"></i>{{el.text}}
                            </button>
                            <button type="button" class="btn btn-default" data-dismiss="modal" v-if="innerLayout === 'modal'">
                                <i class="iconfont icon-cancel"></i>{{closeTitle}}
                            </button>
                        </div>
                    </form>
                </div>
            </div>
            */
        })
    });
});