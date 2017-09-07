define([
    'vue',
    'jquery',
    'util'
], function (vue, $, util) {
    vue.component('bv-operate', {
        props: {
            name: '',
            clazz: '',
            css: '',
            attr: '',
            entity: '',
            click: '',
            operates: ''
        },
        data: function () {
            return {
                innerClass: this.clazz,
                innerStyle: this.css,
                innerAttr: this.attr,
                innerEntity: this.entity
            }
        },
        created: function () {
            util.initDefault(this);
        },
        methods: {
            checkShow: function(op) {
                if (op.show === undefined) {
                    return true;
                } else if (util.type(op.show) === 'function') {
                    return op.show.call(null, this.innerEntity);
                }
            },
            click: function(event, op, position) {
                this.click.call(this.$parent, event, op, position);
            }
        },
        /****** 模板定义 ******/
        template: util.heredoc(function() {
            /*!
            <div v-bind="innerAttr" :class="innerClass" :style="innerStyle">
                <a href="javascript:;" v-for="op in operates" v-if="checkShow(op)" @click="click($event, op, 'body')">
                    <i class="iconfont" v-if="op.icon || op.type === 'sub'" :class="op.icon || op.type === 'sub' ? 'icon-more' : ''"></i>{{op.text}}
                </a>
            </div>
            */
        })
    });
});