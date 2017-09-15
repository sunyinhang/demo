define([
    'vue',
    'jquery',
    'util',
    'Const'
], function (vue, $, util, Const) {
    vue.component('bv-auto', {
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
                default: Const.url.auto.query
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

            initUrl: {
                default: Const.url.auto.init
            },
            // 是否增加去重
            distinct: '',
            extraColumns: '',
            /*
             每次最多显示数量
             */
            limit: {
                default: 10
            },
            onItemInit: '',
            onChange: ''
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
                innerOptions: []
            };
        },
        beforeCreate: function () {
            this.localOrderList = [];
            this.localCustomLoad = false;
            /*
             内部用，结构为：{code: {code: '', name: '', desc: ''}}
             */
            this.localMap = {};
            this.localShows = [];
        },
        mounted: function () {
            util.initDefault(this);
            util.initId(this);
            util.initSelectParam(this);

            var vm = this;
            if (this.load) {
                vm.$watch('innerEntity.' + vm.load, function(val, oldVal) {
                    if (!util.isEmpty(val)) {
                        util.initSelectData(vm, 'auto', 'load');
                    } else {
                        $('input', vm.$el).val('');
                        vm.innerEntity[vm.name] = null;
                        vm.localMap = {};
                        vm.localShows = [];
                    }
                });
                vm.$watch('innerEntity.' + vm.name, function(val, oldVal) {
                    if (util.isEmpty(val)) {
                        $('input', vm.$el).val('');
                    }
                });
            }
            util.initSelectData(this, 'auto');

            $('input', vm.$el).typeahead({
                source: function (query, process) {
                    util.doAutoProcess(vm, query, process);
                },
                matcher: !vm.choose && vm.url && function() {
                    return true;
                },
                items: vm.limit,
                minLength: 0,
                showHintOnFocus: true,
                fitToElement: true,
                autoSelect: false,
///                    delay: 500,
                afterSelect: function (item) {
                    vm.innerEntity[vm.name] = vm.localMap[item][vm.innerCode];
                    util.validate($('input', vm.$el));
                    if (util.type(vm.onChange) === 'function') {
                        vm.onChange.call(null, vm.innerEntity, vm.innerEntity[vm.name], vm.localMap[item]);
                    }
                }
            });

            $('input', vm.$el).on('blur', function() {
                if (util.isEmpty(vm.innerEntity[vm.name])) {
                    $(this).val('');
                }
                if (util.isEmpty($(this).val())) {
                    vm.innerEntity[vm.name] = '';
                }

                var $typeahead = $(this).data('typeahead');
                if ($typeahead) {
                    $typeahead.focused = false;
                }
            });
        },
        methods: {
            trigger: function(event) {
                $('input', this.$el).typeahead('lookup').focus();
            },
            change: function(event) {
                this.innerEntity[this.name] = '';
                util.validate($('input', this.$el));
                if (util.type(this.onChange) === 'function') {
                    this.onChange.call(null, {});
                }
            }
        },
        /****** 模板定义 ******/
        template: util.heredoc(function() {
            /*!
            <div class="input-group auto" :class="innerClass" :style="innerStyle">
                <input type="text" :id="name" class="form-control" v-bind="innerAttr" @input="change" autocomplete="off" data-provide="typeahead" />
                <span class="input-group-addon"><i class="iconfont icon-more" @click="change($event)"></i></span>
            </div>
             */
        })
    });
});