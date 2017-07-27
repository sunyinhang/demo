define([
    'vue',
    'jquery',
    'util',
    'Const'
], function (vue, $, util, Const) {
    vue.component('bv-static', {
        props: {
            entity: '',

            id: '',
            name: '',
            // class
            clazz: '',
            css: '',
            // 属性定义
            attr: '',
            title: '',
            href: '',
            // 默认值
            defaultValue: '',
            // 初始值
            value: '',
            // 设置值
            text: '',
            from: '',
            // 格式化
            format: '',
            // 自定义显示
            show: ''
        },
        data: function() {
            return {
                innerEntity: this.entity,
                innerClass: this.clazz,
                innerStyle: this.css,
                innerAttr: this.attr,
                innerValue: this.value
            };
        },
        created: function() {
            util.initDefault(this);

            this.initAttr();
        },
        mounted: function () {
            if (this.from === 'table') {
                this.innerValue = this.text;
                this.$watch('text', function(val, oldVal) {
                    this.initAttr();
                    this.innerValue = val;
                });
            } else {
                this.initVal(this.innerValue);
                this.$watch('entity.' + this.name, function(val, oldVal) {
                    this.initAttr();
                    this.initVal(val);
                });
            }
        },
        methods: {
            initAttr: function () {
                this.innerClass = this.clazz || '';
                this.innerAttr = this.attr || {};
                if (this.title) {
                    this.innerAttr['data-title'] = this.title;
                    this.innerAttr['data-original-title'] = this.title;
                    if (!this.href) {
                        this.innerClass += 'abbr';
                    }
                }
                if (this.from) {
                    this.innerClass += ' from-' + this.from;
                }
            },
            initVal: function (val) {
                if (this.show && util.type(this.show) === 'function') {
                    this.innerValue = util.format(this.show.call(null, val), this.format);
                } else {
                    this.innerValue = util.format(val, this.format);
                }
            }
        },
        /****** 模板定义 ******/
        template: util.heredoc(function() {
            /*!
            <p v-bind="innerAttr" class="form-static" :class="innerClass" :style="innerStyle"><span v-text="innerValue"></span></p>
            */
        })
    });
});