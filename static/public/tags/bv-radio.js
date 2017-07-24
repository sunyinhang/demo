define([
    'vue',
    'jquery',
    'util',
    'Const'
], function (vue, $, util, Const) {
    vue.component('bv-radio', {
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
            cols: ''
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
                innerLabelClass: this.labelClass
            };
        },
        beforeCreate: function () {
            this.localOrderList = [];
            this.localCustomLoad = false;
        },
        mounted: function () {
            if (this.url !== Const.url.select.query) {
                this.localCustomLoad = true;
            }
            util.initDefault(this);
            util.initSelectParam(this);

            if (this.cols && this.cols > 0 && this.cols <= 12) {
                this.innerLabelClass = 'col-custom col-md-' + this.cols;
            }

            var vm = this;
            if (this.load) {
                vm.$watch('innerEntity.' + vm.load, function(val, oldVal) {
                    if (!util.isEmpty(val)) {
                        util.initSelectData(vm, 'radio', 'load');
                    } else {
                        vm.innerEntity[vm.name] = null;
                        vm.innerOptions = [];
                    }
                });
            }
            util.initSelectData(this, 'radio');

            if (util.type(this.onChange) === 'function') {
                vm.$watch('innerEntity.' + vm.name, function (val, oldVal) {
                    this.onChange.call(null, this.innerEntity, this.innerEntity[this.name], util.index(this.innerOptions, this.innerEntity[this.name], this.innerCode, true));
                })
            }
        },
        /****** 模板定义 ******/
        template: util.heredoc(function() {
            /*!
            <div class="bv-radio">
                <label v-for="option in innerOptions" class="radio-inline" :class="innerLabelClass" :style="innerStyle">
                    <input type="radio" :class="innerClass" :id="name + '-' + option[innerCode]" :value="option[innerCode]" v-bind="innerAttr" v-model="innerEntity[name]" />{{option[innerDesc]}}
                </label>
            </div>
             */
        })
    });
});