define([
    'vue',
    'jquery',
    'util',
    'Const'
], function (vue, $, util, Const) {
    vue.component('bv-select', {
        props: {
            entity: '',

            id: '',
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
            initOption: {
                default: '请选择'
            },
            onChange: '',

            multiple: false,
            // select专用
            label: ''
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
                innerGroups: [],
                innerOptions: []
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
            util.initId(this);
            util.initSelectParam(this);

            var vm = this;
            if (this.load) {
                vm.$watch('innerEntity.' + vm.load, function(val, oldVal) {
                    if (!util.isEmpty(val)) {
                        util.initSelectData(vm, 'select', 'load');
                    } else {
                        vm.innerEntity[vm.name] = null;
                        vm.innerOptions = [];
                    }
                });
            }
            util.initSelectData(this, 'select');

            if (util.type(this.onChange) === 'function') {
                vm.$watch('innerEntity.' + vm.name, function (val, oldVal) {
                    this.onChange.call(null, this.innerEntity, this.innerEntity[this.name], util.index(this.innerOptions, this.innerEntity[this.name], this.innerCode, true));
                })
            }
        },
        /****** 模板定义 ******/
        template: util.heredoc(function() {
            /*!
            <div>
                <select class="form-control" v-bind="innerAttr" :class="innerClass" v-model="innerEntity[name]">
                    <option v-bind="{value: null}" v-if="initOption" v-text="initOption"></option>
                    <optgroup v-for="optgroup in innerGroups" v-bind="{label: optgroup.label}">
                        <option v-for="option in optgroup.innerOptions" v-bind="{value: option[innerCode]}" v-text="option[innerDesc]"></option>
                    </optgroup>
                    <option v-for="option in innerOptions" v-bind="{value: option[innerCode]}" v-text="option[innerDesc]"></option>
                </select>
            </div>
             */
        })
    });
});