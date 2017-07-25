define([
    'vue',
    'jquery',
    'util'
], function (vue, $, util) {
    vue.component('bv-toggle', {
        props: {
            entity: '',

            name: '',
            defaultValue: {
                default: 'OFF'
            },
            value: '',
            onText: {
                default: 'ON'
            },
            offText: {
                default: 'OFF'
            }
        },
        data: function () {
            return {
                innerEntity: this.entity
            }
        },
        created: function () {
            util.initDefault(this);
        },
        mounted: function () {
            var vm = this;
            $('input', vm.$el).bootstrapSwitch({
                state: vm.innerEntity[vm.name] === 'ON',
                onText: vm.onText,
                offText: vm.offText,
                onSwitchChange: function(event, value) {
                    if (value) {
                        vm.innerEntity[vm.name] = 'ON';
                    } else {
                        vm.innerEntity[vm.name] = 'OFF';
                    }
                }
            });
        },
        /****** 模板定义 ******/
        template: util.heredoc(function() {
            /*!
             <div class="switch"><input type="checkbox" /></div>
            */
        })
    });
});