require(['vue', 'jquery', 'util'], function(vue, $, util) {
    vue.component('tab-applyQuota', {
        props: {
            active: {
                default: 'realName'
            }
        },
        data: function () {
            return {
                innerTabs: [
                    {
                        text: '实名',
                        clazz: 'realName'
                    },
                    {
                        text: '个人资料',
                        clazz: 'personal'
                    },
                    {
                        text: '人脸识别',
                        clazz: 'face'
                    },
                    {
                        text: '密码设置',
                        clazz: 'password'
                    }
                ]
            }
        },
        /****** 模板定义 ******/
        template: util.heredoc(function() {
            /*!
            <div class="tab-applyQuota">
                <ul>
                    <li v-for="el in innerTabs" :class="[{'active': active.indexOf(el.clazz) >= 0}, el.clazz]" v-text="el.text"></li>
                </ul>
            </div>
            */
        })
    });
});
var __areaCache = {};
function cacheArea(code, items) {
    if (!items) {
        if (__areaCache.inits) {
            if (!code) {
                // 省
                return __areaCache.inits;
            } else {
                if (__areaCache[code]) {
                    return __areaCache[code];
                }
            }
        }
        return null;
    } else {
        if (!code) {
            __areaCache.inits = items;
        } else {
            __areaCache[code] = items;
        }
    }
}
function getArea (code, callback) {
    var items = cacheArea(code);
    if (items) {
        callback.call(null, items);
        return;
    }
    // var items = [];
    util.get({
        url: '/getArea',
        data: !util.isEmpty(code) ? {areaCode: code} : '',
        // async: false,
        cache: true,
        success: function(res) {
            cacheArea(code, util.data(res));
            callback.call(null, util.data(res));
            // items = util.data(res);
        }
    });
    // return items;
}
window.app = {
    cacheArea: cacheArea,
    getArea: getArea
}
