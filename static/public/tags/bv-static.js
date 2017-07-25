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
            // 默认值
            defaultValue: '',
            // 初始值
            value: '',
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

            if (this.show && util.type(this.show) === 'function') {
                this.innerValue = util.format(this.show.call(null, this.innerValue), this.format);
            } else {
                this.innerValue = util.format(this.innerValue, this.format);
            }

            if (!this.innerClass) {
                this.innerClass = '';
            }
            if ((this.attr['data-title'] || this.attr['title']) && !this.attr['data-href']) {
                this.innerClass += 'abbr';
            }
            if (this.from) {
                this.innerClass += ' from-' + this.from;
            }

            this.$watch('entity.' + this.name, function(val, oldVal) {
                if (this.show && util.type(this.show) === 'function') {
                    this.innerValue = util.format(this.show.call(null, val), this.format);
                } else {
                    this.innerValue = util.format(val, this.format);
                }
            });
        },
        /****** 模板定义 ******/
        template: util.heredoc(function() {
            /*!
            <div><p v-bind="innerAttr" class="form-static" :class="innerClass" :style="innerStyle"><span v-text="innerValue"></span></p></div>
            */
        })
    });
});