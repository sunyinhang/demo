define([
    'vue',
    'jquery',
    'util'
], function (vue, $, util) {
    vue.component('bv-hidden', {
        props: {
            entity: '',

            id: '',
            name: '',
            clazz: '',
            // 属性定义
            attr: '',
            defaultValue: '',
            value: ''
        },
        data: function() {
            return {
                innerEntity: this.entity,
                innerClass: this.clazz,
                innerAttr: this.attr,
                innerValue: this.value
            };
        },
        watch: {
            innerValue: function (val, oldVal) {
                this.$emit('on-change', this.name, val, oldVal);
            }
        },
        created: function() {
            util.initDefault(this);
        },
        /****** 模板定义 ******/
        template: util.heredoc(function() {
            /*!
             <div><input type="hidden" :class="innerClass" v-bind="innerAttr" v-model="innerEntity[name]" /></div>
             */
        })
    });
});