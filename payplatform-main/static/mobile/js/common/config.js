var $global = {};
require.config({
    urlArgs: function(moduleName, url) {
        return '?_=' + (_version[url] || _version[moduleName] || _version._);
    },
    baseUrl: __baseResourcePath,
    map: {
        '*': {
          'css': 'js/require-css/css.min',
          'text': 'js/require-text/text.min'
        }
    },
    paths: {
        Promise: 'js/avalon-2/promise',
        avalonOrigin: 'js/avalon-2/avalon.modern.min',
        avalon: 'js/avalon-2/avalon.extend',
        zepto: 'js/zepto/zepto.min',
        zeptoSelector: 'js/zepto/zepto-selector',
        jquery:'js/jquery/jquery-2.1.4.min',
        suiBase: 'js/sui/js/sm.min',
        suiExtend: 'js/sui/js/sm-extend.min',
        sui: 'js/sui/js/sm-city-picker.min',
        chart: 'js/chart/Chart.min',
        layer: 'js/layer/layer',

        util: 'js/common/util',
        Const: 'js/common/const',

        msTextfield: 'js/tag/ms-textfield',
        msTextarea: 'js/tag/ms-textarea',
        msDate: 'js/tag/ms-date',
        msSelect: 'js/tag/ms-select',
        msToggle: 'js/tag/ms-toggle',
        msPicker: 'js/tag/ms-picker',
        msBars: 'js/tag/ms-bars',
        msTabs: 'js/tag/ms-tabs',
        msList: 'js/tag/ms-list',
        msForm: 'js/tag/ms-form',
        msChart: 'js/tag/ms-chart',
        circleChart:'js/circle/circleChart.min',
        msCirclePgBar:'js/tag/ms-circlepgbar',
        msPhoto: 'js/tag/ms-photo',
        msPhotoUgrade : 'js/tag/ms-photoUpgrade',
        mobileAreaSelect:'js/mobile-area-select/mobile-area-select',

        getUrlPara:'js/getUrlPara/getUrlPara',
        common:'js/common/common',
        mui:'js/mui/mui.min',
        muiPicker:'js/mui/mui.picker.min',
        muiPop:'js/mui/mui.poppicker',
        iscroll:'js/iscroll/iscroll',
        myiscroll:'js/tag/ms-iscroll',
        billheadTabs:'js/tag/ms-billheadTabs'
    },
    shim: {
        Promise: {
            exports: 'Promise'
        },
        avalonOrigin: {
            exports: 'avalonOrigin'
        },
        avalon: {
            deps: ['avalonOrigin'],
            exports: 'avalon'
        },
        zepto: {
            exports: 'zepto'
        },
        zeptoSelector: {
            deps: ['zepto'],
            exports: 'zeptoSelector'
        },
        suiBase: {
            deps: ['zepto'],
            exports: 'suiBase'
        },
        suiExtend: {
            deps: ['suiBase'],
            exports: 'suiExtend'
        },
        sui: {
            deps: ['zepto', 'suiBase', 'suiExtend'],
            exports: 'sui'
        },
        chart: {
            exports: 'chart'
        },
        layer: {
            deps: ['jquery','css!../mobile/js/layer/skin/layer.css'],
            exports: 'layer'
        },
        util: {
            exports: 'util'
        },
        Const: {
            exports: 'Const'
        },
        msTextfield: {
            exports: 'msTextfield'
        },
        msTextarea: {
            exports: 'msTextarea'
        },
        msDate: {
            exports: 'msDate'
        },
        msSelect: {
            exports: 'msSelect'
        },
        msToggle: {
            exports: 'msToggle'
        },
        msPicker: {
            exports: 'msPicker'
        },
        msBars: {
            exports: 'msBars'
        },
        msTabs: {
            exports: 'msTabs'
        },
        msList: {
            exports: 'msList'
        },
        msForm: {
            exports: 'msForm'
        },
        msChart: {
            deps: ['chart'],
            exports: 'msChart'
        },
        circleChart: {
            deps: ['jquery'],
            exports:'circleChart'
        },
        common:{
        	 deps: ['jquery'],
             exports:'common'
        },
        msPhoto: {
            deps:['jquery','avalon'],
            exports:'msPhoto'
        },
        msPhotoUpgrade: {
            deps:['jquery','avalon'],
            exports:'msPhotoUpgrade'
        },
        mobileAreaSelect: { 
            deps:['zepto','zeptoSelector','css!../mobile/js/mobile-area-select/mobile-area-select.css'],
            exports:'mobileAreaSelect'
        },
        msCirclePgBar:{
        	 deps:['jquery','circleChart'],
             exports:'msCirclePgBar'
        },
	    getUrlPara:{
	    	 deps:['jquery'],
	    	 exports:'getUrlPara'
		},
		mui:{
			   deps:['css!../mobile/js/mui/mui.poppicker.css','css!../mobile/js/mui/mui.picker.css','css!../mobile/js/mui/myMui.css'],
	           exports:'mui'
			},
		muiPicker:{		
			   deps:['mui'],
	           exports:'muiPicker'
			},	
		muiPop:{
			   deps:['mui'],
	           exports:'muiPop'
			},
		iscroll:{
			exports:'iscroll'
		},
		myiscroll:{
			deps:['iscroll'],
	        exports:'myiscroll'
		},
		billheadTabs:{	
	        exports:'billheadTabs'
		}
    }
});