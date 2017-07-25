define([
    'vue',
    'jquery',
    'util'
], function (vue, $, util) {
    vue.component('bv-textarea', {
        props: {
            entity: '',

            id: '',
            name: '',
            clazz: '',
            css: '',
            // 属性定义
            attr: '',
            // 数据校验
            validate: '',
            defaultValue: '',
            value: ''
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
            util.initId(this);
        },
        /****** 模板定义 ******/
        template: util.heredoc(function() {
            /*!
             <div><textarea class="form-control" :class="innerClass" :style="innerStyle" v-bind="innerAttr" v-model="innerEntity[name]"></textarea></div>
             */
        })
    });
});