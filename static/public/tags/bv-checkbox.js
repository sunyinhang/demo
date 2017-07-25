define([
    'vue',
    'jquery',
    'util',
    'Const'
], function (vue, $, util, Const) {
    vue.component('bv-checkbox', {
        props: {
            entity: '',

            name: '',
            // class
            clazz: '',
            css: '',
            // 属性定义
            attr: '',
            // 数据校验
            validate: '',
            // 默认值
            defaultValue: '',
            // 初始值
            value: '',
            load: '',

            // 字典类型，支持enums,dicts
            preset: {
                default: 'default'
            },
            code: '',
            desc: '',
            // 数据
            choose: '',
            // 数据接口
            url: {
                default: Const.url.select.query
            },
            method: {
                default: 'post'
            },
            entityName: '',
            initParam: '',
            initParamList: '',
            orders: '',
            // 翻译code用的字典
            trans: '',
            // 额外显示
            extras: '',
            // 不予显示的code值
            excludes: '',
            show: '',
            onChange: '',

            // radio、checkbox专用
            labelClass: '',
            extraColumns: '',
            cols: '',
            sticky: ''
        },
        data: function () {
            return {
                innerEntity: this.entity,
                innerClass: this.clazz,
                innerStyle: this.css,
                innerAttr: this.attr,
                innerValue: this.value,
                innerCode: this.code,
                innerDesc: this.desc,
                innerChoose: this.choose,
                innerOptions: [],
                innerLabelClass: this.labelClass,
                innerSticky: this.sticky,
                innerValues: []
            };
        },
        watch: {
            innerValues: function (val, oldVal) {
                this.innerEntity[this.name] = util.arrayToString(val, this.localSeprate);
            }
        },
        beforeCreate: function () {
            this.localOrderList = [];
            this.localCustomLoad = false;
            this.localSeprate = ',';
        },
        mounted: function () {
            if (this.url !== Const.url.select.query) {
                this.localCustomLoad = true;
            }
            if (this.innerSticky && util.startsWith(this.innerSticky, '#')) {
                this.innerSticky = this.innerEntity[this.innerSticky.substring(1)];
            }
            util.initDefault(this);
            util.initSelectParam(this);

            if (this.cols && this.cols > 0 && this.cols <= 12) {
                this.innerLabelClass = 'col-custom col-md-' + this.cols;
            }

            if (util.type(this.innerValue) !== 'array' ) {
                if (this.innerValue) {
                    if (util.type(this.innerValue) === 'string' && this.innerValue.indexOf(this.localSeprate) !== -1) {
                        this.innerValues = util.stringToArray(this.innerValue, this.localSeprate);
                    } else{
                        this.innerValues.push(this.innerValue);
                    }
                }
            } else {
                this.innerValues = innerValue;
            }
            if (this.innerSticky && !util.contains(this.innerValues, this.innerSticky)) {
                this.innerValues.push(this.innerSticky);
                if (util.type(this.innerValue) !== 'array' ) {
                    if (!this.innerValue) {
                        this.innerValue = this.innerSticky;
                    } else {
                        this.innerValue += this.localSeprate + this.innerSticky;
                    }
                    this.innerEntity[this.name] = this.innerValue;
                }
            }

            var vm = this;
            if (this.load) {
                vm.$watch('innerEntity.' + vm.load, function(val, oldVal) {
                    if (!util.isEmpty(val)) {
                        util.initSelectData(vm, 'checkbox', 'load');
                    } else {
                        vm.innerEntity[vm.name] = null;
                        vm.innerOptions = [];
                    }
                });
            }
            util.initSelectData(this, 'checkbox');

            if (util.type(this.onChange) === 'function') {
                vm.$watch('innerEntity.' + vm.name, function (val, oldVal) {
                    this.onChange.call(null, this.innerEntity, this.innerEntity[this.name], util.index(this.innerOptions, this.innerEntity[this.name], this.innerCode, true));
                })
            }
        },
        /****** 模板定义 ******/
        template: util.heredoc(function() {
            /*!
            <div class="bv-checkbox">
                <label v-for="option in innerOptions" class="checkbox-inline" :class="innerLabelClass" ms-click="@doClick($event, option[@code])">
                    <input type="checkbox" :class="innerClass" :style="innerStyle" :id="name + '-' + option[innerCode]" :name="name" :value="option[innerCode]" :disabled="option[innerCode] === innerSticky" v-bind="innerAttr" v-model="innerValues" />{{option[innerDesc]}}
                </label>
            </div>
             */
        })
    });
});