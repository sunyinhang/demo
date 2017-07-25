define([
    'vue',
    'jquery',
    'util'
], function (vue, $, util) {
    // 目前应用场景为table的精确查询
    vue.component('bv-between', {
        props: {
            entity: '',

            id: '',
            name: '',
            type: '',
            start: '',
            end: '',
            range: '',
            format: '',
            // 格式:{units: ['秒', '分'], options: [5， 10， 15], input: false}
            // units支持秒、分、时、天、月、年
            // input为true时允许输入
            period: '',
            from: '',
            operate: '',
            tagName: ''
        },
        /****** 模板定义 ******/
        template: util.heredoc(function() {
            /*
            <div class="input-group bv-between">
                <component :is="type" :key="start + '-filter'" :from="'filter'" :entity="entity" :id="start" :name="start" :format="format" :triggerEnd="end"></component>
                <div class="input-group-addon" v-if="!period">至</div>
                <component class="input-group-addon bv-group" v-if="period" is="bv-select" :key="end + '-range'" :from="'filter'" :entity="entity" :name="range" :choose="period && period.options" v-bind="{code: 'code', desc: 'desc'}"></component>
                <component :is="type" :key="end + '-filter'" :from="'filter'" :entity="entity" :name="end" :format="format" :triggerStart="start" :load="range"></component>
            </div>
             */
        })
    });
});