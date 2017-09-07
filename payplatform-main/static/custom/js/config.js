/**
__appCode定义应用编码，注意：portal为特殊编码不能占用，指内网门户
本文件的__Const变量会覆盖public/common/const.js中相关配置
__Const.init.debug：是否开启调试，设置为true后会在浏览器控制台输出部分调试信息，调用输出信息的方式为util.debug(title, content)
__Const.init.login：session超时后是否弹出登录窗口，设置为true会弹出，否则仅提示错误信息
 */
var __appPath = location.pathname.replace(new RegExp('/', "gm"), '');
var __appCode = 'payplatform';
var __baseResourcePath = '/public';
var __Const = {
    init: {
        debug: true
    },
    url: {
        authority: {
            info: '${root}/user',
            menus: '${portal}/mMenus/authMenus?appId=acquirer'
        }
    },
    rest: {
        baseUrl: window.location.protocol + "//" + window.location.host + '/api'
    },
    menu: {
        parentId: 'parentMenu',
        url: 'menuUrl'
    }
};