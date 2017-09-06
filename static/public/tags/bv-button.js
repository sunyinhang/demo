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

    vue.component('bv-href', {
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
            // 默认值
            defaultValue: '',
            // 初始值
            value: '',
            text: '',
            from: '',
            format: '',
            show: '',

            icon: '',
            href: '',
            click: ''
        },
        data: function() {
            return {
                innerEntity: this.entity || {},
                innerClass: this.clazz,
                innerStyle: this.css,
                innerAttr: this.attr || {},
                innerValue: this.value,
                innerIcon: this.icon
            };
        },
        created: function() {
            util.initDefault(this);
            /*if (this.text) {
                this.innerValue = this.text;
            }*/
            if (this.title) {
                this.innerAttr['data-title'] = this.title;
                this.innerAttr['data-original-title'] = this.title;
            }
            if (this.href && this.href.type === 'sub') {
                this.innerIcon = 'icon-more';
            }
        },
        mounted: function () {
            if (this.from === 'table') {
                this.innerValue = this.text;
                this.$watch('text', function(val, oldVal) {
                    this.innerValue = val;
                });
            } else {
                if (this.show && util.type(this.show) === 'function') {
                    this.innerValue = util.format(this.show.call(null, this.innerValue), this.format);
                } else {
                    this.innerValue = util.format(this.innerValue, this.format);
                }

                this.$watch('entity.' + this.name, function(val, oldVal) {
                    if (this.show && util.type(this.show) === 'function') {
                        this.innerValue = util.format(this.show.call(null, val), this.format);
                    } else {
                        this.innerValue = util.format(val, this.format);
                    }
                });
            }
        },
        methods: {
            // TODO: 暂时只支持table的column调用
            innerClick: function (event) {
                if (util.type(this.click) === 'function') {
                    this.click.call(this.$parent, event);
                }
            }
        },
        /****** 模板定义 ******/
        template: util.heredoc(function() {
            /*!
             <a href="javascript:;" :class="innerClass" :style="innerStyle" v-bind="innerAttr" @click="innerClick($event)"><i v-if="innerIcon" class="iconfont" :class="innerIcon"></i>{{innerValue}}</a>
             */
        })
    });
});