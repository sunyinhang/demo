define([
    'vue',
    'jquery',
    'util',
    'Const'
], function (vue, $, util, Const) {
    vue.component('bv-button', {
        props: {
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

            // 支持button，close
            type: {
                default: 'button'
            },

            loading: '',
            text: '',
            icon: '',
            click: ''
        },
        data: function() {
            return {
                innerClass: this.clazz,
                innerStyle: this.css,
                innerAttr: this.attr || {},
                innerValue: this.value,
                innerType: this.type,
                innerIcon: this.icon
            };
        },
        created: function() {
            util.initDefault(this);
            if (this.text) {
                this.innerValue = this.text;
            }
            if (!this.innerClass) {
                this.innerClass = 'btn-default';
            }
            if (this.innerType === 'close') {
                this.innerIcon = 'icon-cancel';
                this.innerAttr['data-dismiss'] = 'modal';
            }
            if (this.loading) {
                this.innerAttr['data-loading-text'] = this.loading;
            }
        },
        methods: {
            innerClick: function (event) {
                if (util.type(this.click) === 'function') {
                    this.click.call(null, event);
                }
            }
        },
        /****** 模板定义 ******/
        template: util.heredoc(function() {
            /*!
            <button :type="innerType" class="btn" :class="innerClass" :style="innerStyle" v-bind="innerAttr" @click="innerClick($event)"><i v-if="innerIcon" class="iconfont" :class="innerIcon"></i>{{innerValue}}</button>
            */
        })
    });
});