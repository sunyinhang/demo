define([
    'vue',
    'jquery',
    'util'
], function (vue, $, util) {
    vue.component('bv-date', {
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

            // 标签调用方来源，主要区别在于有没有日历图标
            // filter-table过滤;form-表单
            from: {
                default: 'form'
            },
            /*
             日期格式yyyy-MM-dd hh:mm:ss
             对应yyyy-mm-dd hh:ii:ss
             */
            format: {
                default: 'yyyy-MM-dd'
            },
            // 最小日期值
            startDate: '',
            // 最大日期值
            endDate: '',
            // 最小日期属性
            triggerStart: '',
            // 最大日期属性
            triggerEnd: ''
        },
        data: function () {
            return {
                innerEntity: this.entity,
                innerClass: this.clazz,
                innerStyle: this.css,
                innerAttr: this.attr,
                innerValue: this.value,
                innerFormat: this.format,
                innerStartDate: this.startDate,
                innerEndDate: this.endDate
            };
        },
        created: function() {
            util.initDefault(this);
            util.initId(this);
            // 初始化日期格式
            if (this.innerFormat === 'date') {
                this.innerFormat = 'yyyy-MM-dd';
            } else if (this.innerFormat === 'datetime') {
                this.innerFormat = 'yyyy-MM-dd hh:mm:ss';
            } else if (this.innerFormat === 'time') {
                this.innerFormat = 'hh:mm:ss';
            } else if (!this.innerFormat) {
                this.innerFormat = 'yyyy-MM-dd';
            } else if (this.innerFormat === 'timestamp') {
                this.innerFormat = 'yyyy-MM-dd hh:mm:ss';
            }
        },
        mounted: function() {
            // 初始化日期选择器
            var $selector;
            if (this.from === 'filter') {
                $selector = $('input', this.$el);
            } else {
                $selector = $(this.$el).addClass('input-group');
            }

            var vm = this;
            // 监听日期插件的值变动及失去焦点事件
            util.datepicker($selector, vm.innerFormat).on('dp.change blur', function(event) {
                // 赋值并进行数据校验
                /// vm.innerValue = $('input', vm.$el).val();
                vm.innerEntity[vm.name] = $('input', vm.$el).val();

                util.validate($('input', vm.$el));
            });

            // 如果有最小日期属性定义（此时该标签为最大日期）并且有时间段定义，则进行初始值设定
            if (vm.load && vm.triggerStart) {
                vm.$watch('innerEntity.' + vm.load, function(val, oldVal) {
                    var date = vm.innerEntity[vm.triggerStart];
                    if (val && date) {
                        date = util.date(vm.format, date, val);
                    }
                    vm.innerEntity[vm.name] = date;
                    $('input', vm.$el).val(date);
                });
            }
            if (vm.triggerStart) {
                // 最小日期对应值
                vm.innerStartDate = vm.innerEntity[vm.triggerStart];
                // 监听最小日期变动
                vm.$watch('innerEntity.' + vm.triggerStart, function (val, oldVal) {
                    vm.innerStartDate = val;
                    if (!val) {
                        // 如果置空，则不限定最小日期
                        $selector.data('DateTimePicker').minDate(false);
                    } else {
                        // 如果最小日期不空，则限定该标签的最小日期
                        $selector.data('DateTimePicker').minDate(val);
                    }
                    // 如果同时定义了时间段并且有值，则设置该标签的值
                    if (vm.load && vm.entity[vm.load]) {
                        var date = util.date(vm.format, val, vm.innerEntity[vm.load]);
                        vm.innerEntity[vm.name] = date;
                        $('input', vm.$el).val(date);
                    }
                });
            }
            if (vm.triggerEnd) {
                // 最大日期对应值
                vm.innerEndDate = vm.innerEntity[vm.triggerEnd];
                // 监听最大日期变动
                vm.$watch('innerEntity.' + vm.triggerEnd, function (val, oldVal) {
                    vm.innerEndDate = val;
                    if (!val) {
                        // 如果置空，则不限定最大日期
                        $selector.data('DateTimePicker').maxDate(false);
                    } else {
                        // 如果最大日期不空，则限定该标签的最大日期
                        $selector.data('DateTimePicker').maxDate(val);
                    }
                });
            }

            // 如果有日期最小值定义，进行限定
            if (vm.innerStartDate) {
                $selector.data("DateTimePicker").minDate(vm.innerStartDate);
            }
            // 如果有日期最大值定义，进行限定
            if (vm.innerEndDate) {
                $selector.data("DateTimePicker").maxDate(vm.innerEndDate);
            }
        },
        /****** 模板定义 ******/
        template: util.heredoc(function() {
            /*!
            <div class="date" :class="innerClass" :style="innerStyle">
                <input type="text" class="form-control" v-bind="innerAttr" :class="{datepicker: from !== 'filter'}" v-model="innerEntity[name]" />
                <span class="input-group-addon" v-if="from !== 'filter'"><i class="iconfont icon-calendar"></i></span>
            </div>
             */
        })
    });
});