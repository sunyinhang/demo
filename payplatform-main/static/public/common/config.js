/*
定义requirejs的配置，以及一些全局变量
*/
// 根据html的if表达式判断浏览器版本，主要针对IE浏览器，8、9、10是三个差异化较大的版本
var __ieClass = document.getElementsByTagName('html')[0].className;
var __browserVersion = 99;
if (__ieClass) {
    if (__ieClass === 'ie8') {
        __browserVersion = 8;
    } else if (__ieClass === 'ie9') {
        __browserVersion = 9;
    }
}
// 全局变量
var __global = {
    browserVersion: __browserVersion,
    isMobile: navigator.userAgent.match(/mobile/i),        // 判断是否为移动版浏览器
    vm: {}
};
require.config({
    urlArgs: function(moduleName, url) {
        return '?_=' + (_version[url] || _version[moduleName] || _version._);
    },
    baseUrl: __baseResourcePath || '/public',
    map: {
        '*': {
            'css': 'js/require-css/css.min',
            'text': 'js/require-text/text.min'
        }
    },
    paths: {
        vue: 'js/vue/vue.min',
        jquery: __global.browserVersion < 9 ? 'js/jquery/jquery-1.min' : 'js/jquery/jquery-2.min',
        bootstrap: 'js/bootstrap/js/bootstrap.min',
//        form: 'js/form/jquery.form',
        ztree: 'js/ztree/jquery.ztree.core.min',
        ztreeCheck: 'js/ztree/jquery.ztree.excheck.min',
        moment: 'js/moment/moment-with-locales.min',
//        datepicker: 'js/bootstrap-datepicker/bootstrap-datepicker.min',
//        datepickerLang: 'js/bootstrap-datepicker/locales/bootstrap-datepicker.zh-CN.min',
        datetimepicker: 'js/bootstrap-datetimepicker/js/bootstrap-datetimepicker.min',
        'switch': 'js/bootstrap-switch/js/bootstrap-switch.min',
        typeahead: 'js/bootstrap-typeahead/bootstrap3-typeahead.min',
//        datetimepickerLang: 'js/bootstrap-datetimepicker/locales/bootstrap-datetimepicker.zh-CN',
        validationEngine: 'js/validation/jquery.validationEngine.min',
        validation: 'js/validation/languages/jquery.validationEngine-zh_CN',
        wangEditor: 'js/wangEditor/wangEditor.min',
        plupload: 'js/plupload-2/plupload.full.min',
        pluploadLang: 'js/plupload-2/i18n/zh_CN',
        json: 'js/json/json2.min',
        md5: 'js/md5/md5.min',
        toastr: 'js/toastr/toastr.min',
        popover: 'js/popover/jquery.webui-popover.min',
        keySwitch: 'js/jquery-keySwitch/jquery.keySwitch',
        scroll: 'js/jquery-nicescroll/jquery.nicescroll.min',
        multiselect: 'js/multiselect/js/multiselect',
        chart: 'js/chart/Chart.min',

        util: 'common/util',
        Const: 'common/const',
        bvStatic: 'tags/bv-static',
        bvButton: 'tags/bv-button',
        bvTextfield: 'tags/bv-textfield',
        bvTextarea: 'tags/bv-textarea',
        bvHidden: 'tags/bv-hidden',
        bvDate: 'tags/bv-date',
        bvSelect: 'tags/bv-select',
        bvRadio: 'tags/bv-radio',
        bvCheckbox: 'tags/bv-checkbox',
        bvAuto: 'tags/bv-auto',
        bvToggle: 'tags/bv-toggle',
        bvModal: 'tags/bv-modal',
        bvTree: 'tags/bv-tree',
        bvTabs: 'tags/bv-tabs',
        bvForm: 'tags/bv-form',
        bvBetween: 'tags/bv-between',
        bvOperate: 'tags/bv-operate',
        bvTable: 'tags/bv-table',
        bvChart: 'tags/bv-chart',
        bvUpload: 'tags/bv-upload',
        bvEditor: 'tags/bv-editor',
        bvImport: 'tags/bv-import',
        bvExport: 'tags/bv-export',
        bvGrant: 'tags/bv-grant'
    },
    shim: {
        vue: {
            exports: 'vue'
        },
        jquery: {
            exports: 'jquery'
        },
        bootstrap: {
            deps: ['jquery'],
            exports: 'bootstrap'
        },
        ztree: {
            deps: ['css!js/ztree/zTreeStyle', 'jquery'],
            exports: 'ztree'
        },
        ztreeCheck: {
            deps: ['ztree'],
            exports: 'ztreeCheck'
        },
        moment: {
            exports: 'moment'
        },
        /*        datepicker: {
         deps: ['css!js/bootstrap-datepicker/css/bootstrap-datepicker3.min'],
         exports: 'datepicker'
         },
         datepickerLang: {
         deps: ['datepicker'],
         exports: 'datepickerLang'
         },*/
        datetimepicker: {
            deps: ['css!js/bootstrap-datetimepicker/css/bootstrap-datetimepicker.min', 'moment'],
            exports: 'datetimepicker'
        },
        'switch': {
            deps: ['css!js/bootstrap-switch/css/bootstrap-switch.min'],
            exports: 'switch'
        },
        typeahead: {
            exports: 'typeahead'
        },
        /*        datetimepickerLang: {
         deps: ['datetimepicker'],
         exports: 'datetimepickerLang'
         },*/
        validationEngine: {
            deps: ['css!js/validation/validationEngine.jquery.min', 'jquery'],
            exports: 'validationEngine'
        },
        validation: {
            deps: ['jquery', 'validationEngine'],
            exports: 'validation'
        },
        wangEditor: {
            deps: ['css!js/wangEditor/css/wangEditor.min'],
            exports: 'wangEditor'
        },
        plupload: {
            exports: 'plupload'
        },
        pluploadLang: {
            deps: ['plupload'],
            exports: 'pluploadLang'
        },
        json: {
            exports: 'json'
        },
        md5: {
            exports: 'md5'
        },
        toastr: {
            deps: ['css!js/toastr/toastr'],
            exports: 'toastr'
        },
        popover: {
            deps: ['css!js/popover/jquery.webui-popover.min'],
            exports: 'popover'
        },
        keySwitch: {
            deps: ['jquery'],
            exports: 'keySwitch'
        },
        scroll: {
            deps: ['jquery'],
            exports: 'scroll'
        },
        multiselect: {
            deps: ['css!js/multiselect/css/style'],
            exports: 'multiselect'
        },
        chart: {
            deps: ['moment'],
            exports: 'chart'
        },
        util: {
            exports: 'util'
        },
        Const: {
            exports: 'Const'
        },
        bvStatic: {
            exports: 'bvStatic'
        },
        bvButton: {
            exports: 'bvButton'
        },
        bvTextfield: {
            exports: 'bvTextfield'
        },
        bvTextarea: {
            exports: 'bvTextarea'
        },
        bvHidden: {
            exports: 'bvHidden'
        },
        bvDate: {
            deps: ['datetimepicker'],
            exports: 'bvDate'
        },
        bvSelect: {
            exports: 'bvSelect'
        },
        bvRadio: {
            exports: 'bvRadio'
        },
        bvCheckbox: {
            exports: 'bvCheckbox'
        },
        bvAuto: {
            deps: ['typeahead'],
            exports: 'bvAuto'
        },
        bvToggle: {
            deps: ['switch'],
            exports: 'bvToggle'
        },
        bvModal: {
            exports: 'bvModal'
        },
        bvTree: {
            deps: ['ztreeCheck'],
            exports: 'bvTree'
        },
        bvTabs: {
            exports: 'bvTabs'
        },
        bvForm: {
            exports: 'bvForm'
        },
        bvBetween: {
            exports: 'bvBetween'
        },
        bvTable: {
            deps: ['bvBetween', 'bvOperate'],
            exports: 'bvTable'
        },
        bvChart: {
            deps: ['chart'],
            exports: 'bvChart'
        },
        bvUpload: {
            deps: ['plupload'],
            exports: 'bvUpload'
        },
        bvEditor: {
            deps: ['plupload', 'wangEditor'],
            exports: 'bvEditor'
        },
        bvImport: {
            deps: ['bvUpload'],
            exports: 'bvImport'
        },
        bvExport: {
            exports: 'bvExport'
        },
        bvGrant: {
            deps: ['multiselect'],
            exports: 'bvGrant'
        }
    }
});