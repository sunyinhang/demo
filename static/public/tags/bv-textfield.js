define([
    'vue',
    'jquery',
    'util'
], function (vue, $, util) {
    vue.component('bv-textfield', {
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

            type: {
                default: 'text'
            },

            // 输入限制
            fix: '',

            chooseUrl: '',
            onChoosePreset: ''
        },
        data: function () {
            return {
                innerEntity: this.entity,
                innerClass: this.clazz,
                innerStyle: this.css,
                innerAttr: this.attr || {},
                innerValue: this.value
            };
        },
        created: function() {
            util.initDefault(this);
            util.initId(this);
            if (!this.innerAttr.type) {
                this.innerAttr.type = this.type;
            }
        },
        methods: {
            // TODO: 暂不支持fix
            checkInput: function(event) {
                if (this.fix) {
                    util.fix(event, this.fix);
                }
            },
            openModal: function () {
                if (util.type(this.onChoosePreset) === 'function') {
                    this.onChoosePreset.call(null);
                }
                util.modal({
                    url: this.chooseUrl,
                    callback: function () {
                        // TODO:
                        console.log('xxxxxx');
                    }
                });
            }
        },
        /****** 模板定义 ******/
        template: util.heredoc(function() {
            /*!
            <div :class="[{'input-group': chooseUrl}, innerClass]" :style="innerStyle">
                <input class="form-control" v-bind="innerAttr" v-model="innerEntity[name]" />
                <span class="input-group-addon bv-pointer" v-if="chooseUrl" @click="openModal($event)"><i class="iconfont icon-query"></i></span>
            </div>
            */
        })
    });
});