define('util', ['vue', 'jquery', 'Const', 'moment', 'toastr', 'json'], function(vue, $, Const, moment, toastr) {
    function InvalidCharacterError(message) {
        this.message = message;
    }
    InvalidCharacterError.prototype = new Error;
    InvalidCharacterError.prototype.name = 'InvalidCharacterError';

    var rword = /[^, ]+/g;
    var class2type = {};
    'Boolean Number String Function Array Date RegExp Object Error'.replace(rword, function (name) {
        class2type['[object ' + name + ']'] = name.toLowerCase()
    });
    var rhyphen = /([a-z\d])([A-Z]+)/g;
    var rcamelize = /[-_][^-_]/g;

    function formatNumber (number, decimals, point, thousands) {
        number = (number + '').replace(/[^0-9+\-Ee.]/g, '');
        var n = !isFinite(+number) ? 0 : +number,
            prec = !isFinite(+decimals) ? 3 : Math.abs(decimals),
            sep = thousands || ",",
            dec = point || ".",
            s = '',
            toFixedFix = function (n, prec) {
                var k = Math.pow(10, prec);
                return '' + (Math.round(n * k) / k)
                        .toFixed(prec);
            };
        // Fix for IE parseFloat(0.55).toFixed(0) = 0;
        s = (prec ? toFixedFix(n, prec) : '' + Math.round(n)).split('.');
        if (s[0].length > 3) {
            s[0] = s[0].replace(/\B(?=(?:\d{3})+(?!\d))/g, sep);
        }
        if ((s[1] || '').length < prec) {
            s[1] = s[1] || '';
            s[1] += new Array(prec - s[1].length + 1).join('0');
        }
        return s.join(dec);
    }

    return {
        /******************************************* 基础工具 *********************************************************/
        // 数据类型
        type: function (obj) {
            if (obj == null) {
                return String(obj);
            }
            // 早期的webkit内核浏览器实现了已废弃的ecma262v4标准，可以将正则字面量当作函数使用，因此typeof在判定正则时会返回function
            return typeof obj === 'object' || typeof obj === 'function' ? class2type[Object.prototype.toString.call(obj)] || 'object' : typeof obj;
        },
        // 判断是否为空，可判断空数组，空json
        isEmpty: function(v) {
            var type = this.type(v);
            if (type === 'undefined' || v === null) {
                return true;
            } else if (type === 'number') {
                return !v && v !== 0;
            } else if (type === 'array') {
                return v.length === 0;
            } else if (type === 'object') {
                for (var t in v)  {
                    return false;
                }
                return true;
            }
            return !v;
        },
        isTrue: function(check, defaultTrueOrFalse) {
            if (this.type(check) === 'undefined') {
                if (this.type(defaultTrueOrFalse) === 'undefined') {
                    return false;
                }
                return defaultTrueOrFalse;
            } else if (this.type(check) === 'function') {
                return check.call(null);
            }
            return this.getBoolean(check);
        },
        // 正则表达式判断
        check: function(v, regex) {
            return new RegExp(regex).test(v);
        },
        isRequired: function(validate, attr) {
            return (!this.isEmpty(validate) && validate.required) || (attr && attr['data-validation-engine'] && attr['data-validation-engine'].indexOf('required') > 0);
        },
        /******************************************* 数据转换 *********************************************************/
        // 获取boolean结果
        getBoolean: function(value) {
            if (value === false || value === 'false') {
                return false;
            }
            if (!value) {
                return false;
            }
            return true;
        },
        // 转number型
        toNumber: function(v) {
            return parseInt(v, 10);
        },
        toString: function(v) {
            if (this.isEmpty(v)) {
                return v;
            }
            return v + '';
        },
        toByte: function(v) {
            // byte:-128~127
            if (v < -128) {
                v += 256;
            } else if (v > 127) {
                v -= 256;
            }
            return v;
        },
        nvl: function(s, defaultValue) {
            return s === null ? defaultValue : s;
        },
        random: function (min, max) {
            return Math.floor(Math.random() * (max - min + 1) + min);
        },
        arrayToString: function (arr, seprate) {
            if (seprate) {
                var str = '';
                for (var i=0; i<arr.length; i++) {
                    if (str) {
                        str += seprate;
                    }
                    str += arr[i];
                }
                return str;
            }
        },
        stringToArray: function (str, seprate) {
            if (str && seprate) {
                return str.split(seprate);
            }
        },
        orderBy: function(array, criteria, reverse) {
            var order = (reverse && reverse < 0) ? -1 : 1;

            if (typeof criteria === 'string') {
                var key = criteria;
                criteria = function (a) {
                    return a && a[key]
                };
            }
            $.each(array, function (el) {
                el.order = criteria(el.value, el.key);
            });
            array.sort(function (left, right) {
                var a = left.order;
                var b = right.order;
                /* istanbul ignore if */
                if (Number.isNaN(a) && Number.isNaN(b)) {
                    return 0;
                };
                return a === b ? 0 : a > b ? order : -order;
            })
            var target = [];
            return this.recovery(target, array, function (el) {
                target.push(el.value);
            });
        },

        recovery: function (ret, array, callback) {
            for (var i = 0, n = array.length; i < n; i++) {
                callback(array[i]);
            }
            return ret;
        },
        // string -> array
        transOrder: function (orderList, orders) {
            if (!orderList) {
                orderList = [];
            }
            if (orders) {
                var ordersArr = orders.split(',');
                for (var i=0; i<ordersArr.length; i++) {
                    var order = $.trim(ordersArr[i]);
                    if (order) {
                        var arr = order.replace(/\s+/g, ' ').split(' ');
                        if (arr.length === 1) {
                            orderList.push({
                                name: $.trim(arr[0])
                            });
                        } else if (arr.length === 2) {
                            orderList.push({
                                name: $.trim(arr[0]),
                                sort: $.trim(arr[1])
                            });
                        }
                    }
                }
            }
            return orderList;
        },
        /******************************************* 参数相关 *********************************************************/
        // 取url参数，例如：gup('type', 'http://localhost:8080/demo/xxx?type=123');
        gup: function(name, url) {
            if (!url) url = this.url('modal') || this.url('body') || location.href;

            // http://localhost:port/appCode/areaCode/index.html
            if (name === '#areaCode') {
                var pathname = location.pathname;
                if (pathname) {
                    var arr = pathname.substring(1).split('/');
                    if (arr.length > 1) {
                        return arr[1];
                    }
                }
                return null;
            } else if (name === '#appCode') {
                var pathname = location.pathname;
                if (pathname) {
                    var arr = pathname.substring(1).split('/');
                    if (arr.length > 0) {
                        return arr[0];
                    }
                }
                return null;
            }

            name = name.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");
            var regexS = "[\\?&]" + name + "=([^&#]*)";
            var regex = new RegExp( regexS );
            var results = regex.exec( url );
            return results === null ? null : results[1];
        },
        modalUrl: function() {
            return $('#modal').attr('data-url');
        },
        currentUrl: function(tabs) {
            return this.currentPanel(tabs).attr('data-url');
        },
        /******************************************* 字符相关 *********************************************************/
        contains: function(v, c) {
            if (this.type(v) === 'array') {
                for (var i=0; i<v.length; i++) {
                    if (v[i] === c) {
                        return true;
                    }
                }
                return false;
            }
            return v && v.indexOf(c) >= 0;
        },
        startsWith: function(v, c) {
            return v && v.indexOf(c) === 0;
        },
        endsWith: function(v, c) {
            return v && v.lastIndexOf(c) === v.length - c.length;
        },
        replaceAll: function(s, r, n) {
            if (s && r) {
                return s.replace(new RegExp(r, "gm"), n);
            }
        },
        encode: function(s) {
            return encodeURIComponent(s);
        },
        /******************************************* 通讯相关 *********************************************************/
        // 组织url地址
        // ${root}/xxx开头的返回/xxx,${portal}/xxx开头的返回portal地址
        // @{portal}、@{root}开头的表示要bv-sys-code到head中
        // 其他/开头的返回baseRestUrl+url
        // type: undefind-rest地址，html-页面，timestamp-增加时间戳
        url: function(url, type) {
            if (!url || url === 'modal') {
                return $('#modal').attr('data-url');
            }
            if (url === 'body') {
                return $('.bv-content[data-active="true"]').attr('data-url');
            }
            if (this.startsWith(url, 'http://') || this.startsWith(url, 'https://') || this.endsWith(url, '.json')) {
                return url;
            }
            if (url && (url.indexOf("${root}/") === 0 || url.indexOf("@{root}/") === 0)) {
                return url.substring(7);
            }
            if (type && type === 'html') {
                return url;
            }
            var resultUrl = Const.rest.baseUrl + url;
            if (url && (url.indexOf('${portal}/') === 0 || url.indexOf('@{portal}/') === 0)) {
                resultUrl = Const.portal.baseUrl + url.substring(9);
            }
            if (type && type === 'timestamp') {
                return this.mix(resultUrl, {
                    _: new Date().getTime()
                });
                /*if (resultUrl.indexOf('?') < 0) {
                 resultUrl += '?_=' + new Date().getTime();
                 } else {
                 resultUrl += '&_=' + new Date().getTime();
                 }*/
            }
            return resultUrl;
        },
        json: function(data) {
            if (this.type(data) === 'string') {
                if (!data) {
                    return {};
                }
                if (!this.startsWith(data, '{') && !this.startsWith(data, '[')) {
                    return data;
                }
                return JSON.parse(data);
            }
            return JSON.stringify(data);
        },
        // ajax.get调用
        get: function(param) {
            param.type = 'get';
            this.ajax(param);
        },
        // ajax.post调用
        post: function(param) {
            param.type = 'post';
            param.dataType = 'json';
            if (!param.contentType) {
                param.contentType = 'application/json';
            }
            if (param.data) {
                param.data = this.json(param.data);
            }
            this.ajax(param);
        },
        del: function(param) {
            param.type = 'delete';
            this.ajax(param);
        },
        // ajax.put调用
        put: function(param) {
            param.type = 'put';
            param.dataType = 'json';
            param.contentType = 'application/json';
            if (param.data) {
                param.data = this.json(param.data);
            }
            this.ajax(param);
        },
        // ajax.post
        submit: function(param) {
            param.type = 'post';
            param.dataType = 'json';
            param.contentType = 'application/json';
            if (param.data) {
                param.data = this.json(param.data);
            }
            this.ajax(param);
        },
        // ajax
        // 参数：
        // $element:事件对应按钮，可控制按钮状态防止重复点击
        // close:执行成功后是否自动关闭窗口
        ajax: function(param) {
            if (!param.url) {
                return;
            }
            this.show('hide');
            this.loading();
            var util = this;
            if (!param.headers) {
                param.headers = {};
            }
            if (param.areaCode) {
                param.headers['bv-area-code'] = param.areaCode;
            } else {
                var areaCode = util.gup('#areaCode');
                if (areaCode) {
                    param.headers['bv-area-code'] = areaCode;
                } else if (Const.init && Const.init.areaCode) {
                    param.headers['bv-area-code'] = Const.init.areaCode;
                }
            }
            /*if (param.url.indexOf('@{portal}/') === 0 || param.url.indexOf('@{root}/') === 0 || param.appCode) {
                param.headers['bv-app-code'] = param.appCode || (Const.init && Const.init.appCode);
            }*/
            param.url = this.url(param.url);
            // 禁用按钮且显示按钮文字为执行中，防止重复提交
            if (param.$element) {
                param.$element = util.button(param.$element);
                if (!param.$element.attr('data-loading-text')) {
                    param.$element.attr('data-loading-text', '处理中...');
                }
                param.$element.button('loading');
            }
            if (param.success) {
                param.$success = param.success;
            }

            param.success = function(data) {
                if (util.type(data) === 'string') {
                    data = util.json(data);
                }
                if (util.success(data)) {
                    if (param.$success) {
                        param.$success.call(null, data);
                    }
                    if (param.show) {
                        if (param.show === 'body') {
                            util.show({
                                $element: param.$element,
                                message: util.message(data),
                                position: param.position,
                                level: param.level,
                                title: '处理成功'
                            });
                        } else {
                            util.show({
                                $element: param.$element,
                                message: util.message(data),
                                position: param.position,
                                level: param.level,
                                title: '处理成功'
                            });
                        }
                    } else if (util.message(data)) {
                        util.show({
                            $element: param.$element,
                            message: util.message(data),
                            position: param.position,
                            level: param.level,
                            title: '处理成功'
                        });
                    }
                    if (param.close) {
                        util.modal('hide');
                    }
                    if (param.refresh && param.vm) {
                        param.vm.refresh();
                    }
                    if (param.$element) {
                        util.hide(param.$element);
                    }
                } else {
                    util.show({
                        $element: param.$element,
                        message: util.message(data),
                        position: param.position,
                        level: 'error'
                    });
                }
                /* else if (data && data.head && data.head.retMsg) {
                 util.show(data, param.$element, param.position || 'alert', param.level);
                 }*/
            }
            if (param.error) {
                param.$error = param.error;
            }
            param.error = function(xhr, status, e) {
                if (param.$error) {
                    param.$error.call(null, xhr, status, e);
                }
                if (xhr.status === 401 || xhr.status === 403) {
                    // alert('登录超时，请重新登录');
                    if (Const.init.login) {
                        if (!Const.from || Const.from !== 'login') {
                            util.login();
                        }
                    } else {
                        if ((!Const.from || Const.from !== 'login') && !Const.alerted && Const.init.type !== 'alert') {
                            Const.alerted = true;
                            util.show({
                                $element: param.$element,
                                message: '登录超时，请重新登录',
                                position: param.position,
                                level: 'error'
                            });
                        }
                        // alert('登录超时，请重新登录');
                    }
                } else if (xhr.status === 404) {
                    util.show({
                        $element: param.$element,
                        message: '请求的资源不存在',
                        position: param.position,
                        level: 'error'
                    });
                } else if (xhr.status !== 200) {
                    util.show({
                        $element: param.$element,
                        message: (xhr.responseJSON && xhr.responseJSON.message) || '系统异常',
                        position: param.position,
                        level: 'error'
                    });
                } else {
                    // 200
                    if (param.close) {
                        util.modal('hide');
                    }
                }
            }
            if (param.complete) {
                param.$complete = param.complete;
            }
            param.complete = function() {
                if (param.$complete) {
                    param.$complete.call(null);
                }
                util.loading('hide');
                // 执行完成后恢复按钮状态
                if (param.$element) {
                    setTimeout(function() {
                        param.$element.button('reset');
                    }, 500);
                }
            }
            param.global = false;
            // 不受全局配置影响
            /*if (!Const.rest.head) {
             param.global = false;
             }*/
            $.ajax(param);
        },
        query: function(param) {
            if (!param || this.isEmpty(param.data)) {
                return;
            }
            if (!param.type) {
                param.type = 'post';
            }
            if (!param.url) {
                param.url = Const.url.select.query;
            }
            this[param.type](param);
        },
        queryOne: function(param) {
            if (!param || this.isEmpty(param.data)) {
                return;
            }
            if (!param.type) {
                param.type = 'post';
            }
            if (!param.url) {
                param.url = Const.url.select.init;
            }
            this[param.type](param);
        },
        // 判断是否成功
        success: function(data) {
            if (Const.rest.head) {
                return !data.head || (data.head && data.head.retFlag && data.head.retFlag === '00000');
            }
            return true;
        },
        message: function(data) {
            return data.head && (data.head.showMsg || (!this.success(data) ? data.head.retMsg : ''));
        },
        data: function(data) {
            if (this.type(data) === 'string') {
                if (data === 'modal') {
                    return Const.global.modal && Const.global.modal.data;
                } else if (data === 'body') {
                    return Const.global.param && Const.global.param.body;
                }
            }
            return Const.rest.head && data.head ? data.body : data;
        },
        /******************************************* 弹窗相关 *********************************************************/
        // 打开弹出窗口，不支持多级窗口级联打开
        // 参数
        // $trigger:触发元素
        // url:弹出窗口数据加载地址
        // onClose:关闭窗口后回调
        // refresh:关闭窗口后是否刷新数据
        modal: function(param) {
            var util = this;
            if (typeof(param) === 'string') {
                if (param === 'hide') {
                    $('.modal').modal('hide');
                }
            } else {
                var element = param.element || '#modal';
                if (param.hide) {
                    $(element).modal('hide');
                    return;
                }
                if (param.$trigger) {
                    param.$trigger.tooltip('hide');
                }
                if (param.type) {
                    if (param.type === 'confirm') {
                        Const.global.confirm = {
                            title: param.title || '请确认',
                            content: param.content || '确定删除选择的数据？',
                            extra: param.extra,
                            url: param.url,
                            data: param.data,
                            buttonText: param.buttonText || '确定删除'
                        }
                    } else if (param.type === 'alert') {
                        Const.global.alert = {
                            level: param.level,
                            size: param.size,
                            title: param.title,
                            content: param.content
                        };
                    } else if (param.type === 'login') {
                    }
                } else {
                    Const.global.modal = {
                        title: param.title,
                        content: param.content,
                        url: param.url,
                        data: param.data,
                        vm: param.vm,
                        callback: param.callback
                    }
                }
                this.replace($(element), param.confirm || param.url, function(response, status, xhr) {
                    if (status === 'success') {
                        // 调试信息
                        util.debug('弹窗地址:', param.confirm || param.url);
//                $(element).attr('data-url', param.confirm || param.url);
                        $(element).modal({
                            backdrop: 'static',
                            keyboard: false
                        }).off('hidden.bs.modal').on('hidden.bs.modal', function () {
                            if (param.type) {
                                if (param.type === 'confirm') {
                                    Const.global.confirm = {};
                                } else if (param.type === 'alert') {
                                    Const.global.alert = {};
                                }
                            } else {
                                Const.global.modal = {};
                            }
                            if ($('#modal').is(':visible') && !$('body').hasClass('modal-open')) {
                                $('body').addClass('modal-open');
                            }
                            $(element).removeAttr('data-url');
                            if (Const.global.modalVm) {
                                Const.global.modalVm.$destroy();
                            }
                            util.empty($(element));
                            if (param.onClose) {
                                param.onClose.call(null);
                            }
                            if (param.refresh && param.vm) {
                                if (util.type(param.refresh) === 'function') {
                                    param.refresh.call(null, param.vm);
                                } else {
                                    param.vm.refresh();
                                }
                            }
                        });
                    }
                });
            }
        },
        // 打开确认对话框
        confirm: function(param) {
            if (this.type(param) === 'string') {
                param = {
                    hide: true
                };
            }
            if (param) {
                if (param.type) {
                    if (param.type === 'delete') {
                        param.title = param.title || '请确认';
                        if (param.from && param.from === 'form') {
                            param.content = param.content || '确定该记录吗？';
                            param.url = param.url || Const.url.form.deleteById;
                            if (param.vm) {
                                var keyValues = param.vm.handleKeyValues();
                                param.data = param.data || {
                                        entityName: param.vm.entityName,
                                        keyValuesList: [keyValues]
                                    };
                            }
                        } else {
                            param.content = param.content || '确定删除选中的记录吗？';
                            param.url = param.url || Const.url.table.deleteByIds;
                            param.refresh = true;
                            if (param.vm) {
                                if (param.url === Const.url.table.deleteByIds) {
                                    // 默认删除
                                    param.data = param.data || {
                                        entityName: param.vm.entityName,
                                        keyValuesList: this.selected(param.vm, true)
                                    };
                                } else {
                                    // 自定义
                                    param.data = param.data || this.selected(param.vm, true);
                                }
                            }
                        }
                    }
                }
                param.element = '#confirm';
                param.confirm = Const.url.template.confirm;
                param.type = 'confirm';
            }
            this.modal(param);
        },
        alert: function(param) {
            if (param) {
                param.element = '#alert';
                param.url = Const.url.template.alert;
                param.type = 'alert';
            }
            this.modal(param);
        },
        loading: function(type) {
            if (type && type === 'hide') {
                if (!Const.global.loading || Const.global.loading === 1) {
                    Const.global.loading = 0;
                    $('#loading').hide();
                } else {
                    Const.global.loading--;
                }
            } else {
                if (!Const.global.loading) {
                    Const.global.loading = 1;

                    //加载中
                    $('#loading').show();
                } else {
                    Const.global.loading++;
                }
            }
        },
        /******************************************* 页面布局 *********************************************************/
        // dialog-弹窗-body
        layout: function($element) {
            if ($element) {
                if ($element.closest('.bv-modal').length === 1) {
                    return 'body';
                } else if ($element.closest('.modal').length === 1) {
                    return 'modal';
                } else if ($element.closest('.bv-body').length === 1 || $element.closest('.bv-simple').length === 1) {
                    return 'body';
                }
            } else {
                if ($('#modal').is(':visible')) {
                    if ($('.bv-modal', $('#modal')).length === 1) {
                        return 'body';
                    }
                    return 'modal';
                }
                return 'body';
            }
        },
        isCurrent: function($element) {
            return $element.closest('.bv-content').attr('data-active') === 'true';
        },
        editType: function() {
            var type = this.gup('type');
            if (!type) {
                return 'insert';
            }
            return type;
        },
        isEdit: function() {
            if (!root || !Const.global.modal || !Const.global.modal.data) {
                return false;
            }
            return true;
        },
        /******************************************* 缓存相关 *********************************************************/
        cache: function(param, value) {
            if (this.type(value) !== 'undefined') {
                Const.global[param] = value;
            } else if (this.type(param) === 'undefined') {
                return this.clone(Const.global.param) || {};
            } else {
                if (param === 'modal') {
                    return Const.global.modal.data;
                } else if (param === 'row') {
                    return Const.global.currentTableRow;
                } else if (this.type(param) === 'string') {
                    return Const.global[param];
                } else if (param.$vnode && param.$vnode.key) {
                    Const.global.vm[param.$vnode.key] = param;
                } else {
                    Const.global.param = this.clone(param);
                }
            }
        },
        session: function(key) {
            return root && root[key];
        },
        storage: function(key, value, id) {
            if (key) {
                var storeKey;
                if (id) {
                    storeKey = Const.init.appCode + '_' + id + '_' + key;
                } else {
                    storeKey = Const.init.appCode + '_' + key;
                }
                if (this.type(value) === 'undefined') {
                    // 查询
                    return localStorage.getItem(storeKey);
                } else {
                    // 设置
                    localStorage.setItem(storeKey, value);
                }
            }
        },
        /******************************************* 日期相关 *********************************************************/
        date: function(pattern, date, period) {
            if (!pattern || pattern === 'date' || pattern === 'yyyy-mm-dd' || pattern === 'yyyy-MM-dd') {
                pattern = 'YYYY-MM-DD';
            } else if (pattern === 'datetime' || pattern === 'timestamp' || pattern === 'yyyy-MM-dd hh:mm:ss') {
                pattern = 'YYYY-MM-DD HH:mm:ss';
            }
            if (!period) {
                if (!date) {
                    date = new Date();
                }
                // moment支持'YYYY-MM-DD HH:mm:ss'
                return moment(date).format(pattern);
            }
            var arrs = period.split(',');
            return moment(date, pattern).add(arrs[0], arrs[1]).format(pattern);
        },
        time: function(pattern, date) {
            if (!pattern) {
                pattern = 'HH:mm:ss';
            }
            if (!date) {
                date = new Date();
            }
            return moment(date).format(pattern);
        },
        /******************************************* 数据处理 *********************************************************/
        clone: function(value, origin) {
            if (value !== undefined && origin !== undefined) {
                for (var p in origin) {
                    vue.set(value, p, origin[p]);
                }
                return;
            }
            var type = this.type(value);
            if (type === 'undefined') {
                return;
            } else if (type === 'object') {
                return this.mix({}, value);
            } else if (type === 'array') {
                return value.slice(0);
            } else {
                return value;
            }
        },
        /*
        功能1：json串拷贝，deep设置为true时为深度拷贝
        功能2：url参数拼接
        */
        mix: function(url, param, deep) {
            if (!url || !param) {
                return url;
            }
            if (this.type(url) === 'object') {
                return $.extend(deep, {}, url, param);
            }
            for (var p in param) {
                if (url.indexOf('?') < 0) {
                    url += '?';
                } else {
                    url += '&';
                }
                url += p + '=' + param[p];
            }
            return url;
        },
        /******************************************* DOM 操作 *********************************************************/
        empty: function($element) {
            if ($element) {
                $('*', $element).remove();
                $element.empty();
            }
        },
        remove: function($element) {
            if ($element) {
                $('*', $element).remove();
                $element.remove();
            }
        },
        // 替换加载页面
        replace: function($element, url, callback) {
            this.empty();
            this.loading();
//            this.scroll($element);
            url = this.url(url, 'html');
            // $element.load(url, callback);
            var util = this;
            $element.load(url, function(response, status, xhr) {
                util.loading('hide');
                if (util.type(callback) === 'function') {
                    callback.call(null, response, status, xhr);
                }
            });
            $element.attr('data-url', url);
        },
        redirect: function(url, type) {
            if (!type) {
                window.location.href = url;
            } else if (type === 'body') {
                this.debug('跳转地址:', url);
                this.replace(this.currentPanel(), url);
            }
        },
        // 打开主页面
        home: function(init) {
            Const.global.mainTabs.innerCurrentIndex = 0;
            /*if (init) {
                this.loadTab(Const.global.mainTabs,  Const.global.mainTabs.tabs[0]);
            }*/
        },
        width: function($elements) {
            if ($elements && $elements.length > 0) {
                if ($elements.length == 1) {
                    return $elements.outerWidth(true);
                }
                var w = 0;
                $elements.each(function(index, element) {
                    w += $(element).outerWidth(true);
                });
                return w;
            }
            return 0;
        },
        click: function(container, selector, callback) {
            $('[data-container="' + container + '"]').off('click', selector).on('click', selector, callback);
        },
        open: function(url) {
            window.open(url);
        },
        // 下载文件
        download: function(url, $trigger, checkBeforeDownload) {
            if (this.startsWith(url, '#')) {
                url = Const.url.file.download + '/' + url.substring(1);
            }
            if ($trigger) {
                $trigger.tooltip('hide');
            }
            var util = this;
            if (util.type(url) === 'string') {
                if (checkBeforeDownload) {
                    this.get({
                        url: url + (url.indexOf('?') < 0 ? '?' : '&') + 'type=check',
                        success: function() {
                            $('iframe#download').attr('src', util.url(url));
                        }
                    });
                } else {
                    $('iframe#download').attr('src', util.url(url));
                }
            } else {
                this.post({
                    $element: url.$element,
                    url: url.url,
                    data: url.data,
                    success: function(data) {
                        var data = util.data(data);
                        $('iframe#download').attr('src', util.url(Const.url.excel.download + '?fileName=' + data.fileName + '&originName=' + data.originName));
                    }
                });
            }
        },
        // 获取或设置editor的内容
        // TODO:
        editor: function(editor, value) {
            if (editor) {
                /*var editor;
                if (this.type($id) === 'string') {
                    editor = this.vm($id).$editor;
                } else {
                    editor = $id;
                }*/
                if (!value) {
                    return editor.$txt.html();
                }
                editor.$txt.html(value);
            }
        },
        /*
         创建标签页
         tabs: 标签页定义
         tab: 单个标签定义，有以下属性：
         id: tab页id
         target: tab页地址
         prop: tab页属性
         */
        // config如果为字符串表示为菜单，否则表示为页面
        mainTabs: function(config, titles, data) {
            if (this.type(config) === 'string') {
                var param = titles || 'id';
                // 点开某个菜单
                // 查询菜单对应节点
                var treeNode = Const.global.menuTree.localTree.getNodeByParam(param, config);
                if (treeNode) {
                    this.mainTabs(treeNode);
                    this.selectNode(treeNode);
                }
            } else if (this.type(config) === 'object') {
                if (config.entity) {
                    // config为treeNode
                    var titles = [];
                    titles.splice(0, 0, {
                        text: config.name
                    });
                    var menuNode = config;
                    while (menuNode.level > 0) {
                        menuNode = menuNode.getParentNode();
                        titles.splice(0, 0, {
                            text: menuNode.name
                        });
                    }
                    this.mainTabs({
                        id: 'menu-' + config.entity.menuId,
                        text: config.entity[Const.menu.name],
                        target: config.entity[Const.menu.url] || Const.url.template.todo,
                        menuId: config.entity[Const.menu.id],
                        prop: {
                            userOperates: config.entity.userOperateList,
                            titles: titles
                        }
                    });
                } else {
                    // config为自定义属性
                    if (!config.menuId) {
                        config.menuId = config.id;
                    }
                    if (!config.prop && titles) {
                        if (this.type(titles) === 'array') {
                            // TODO:暂不处理
                            config.prop = {titles: []};
                        } else if (this.type(titles) === 'string') {
                            config.prop = {titles: []};
                            var titleArr = titles.split("->");
                            for (var i=0; i<titleArr.length; i++) {
                                config.prop.titles.push({
                                    text: titleArr[i]
                                });
                            }
                        }
                    }
                    //// root.position.titles = config.prop.titles;
                    this.checkTab(Const.global.mainTabs, config);
                    if (data) {
                        root.$tabsData = data;
                    }
                }
            }
        },
        // 初始化form校验
        validateInit: function($form) {
            if ($form.is('form')) {
                $form.validationEngine({validationEventTrigger: 'blur change', autoPositionUpdate: true, fadeDuration: 0});
            } else {
                $form.on("click", ".formError", function() {
                    $(this).fadeOut(150, function() {
                        // remove prompt once invisible
                        $(this).closest('.formError').remove();
                    });
                });
            }
        },
        // 合并validate属性
        // 'data-validation-engine': 'validate[required,equals[#prev]]'
        // 'data-errormessage-value-missing': '确认密码不能为空'
        // 'data-errormessage-pattern-mismatch': '确认密码与新密码不一致'
        validateMix: function(validate) {
            if (this.isEmpty(validate)) {
                return;
            }
            var result = {};
            var validateStr = '';
            if (validate.required) {
                if (validateStr) {
                    validateStr += ',';
                }
                validateStr += 'required';
                if (validate.required !== true) {
                    result['data-errormessage-value-missing'] = validate.required;
                }
            }
            if (validate.equals) {
                if (validateStr) {
                    validateStr += ',';
                }
                if (validate.equals.code) {
                    validateStr += 'equals[' + validate.equals.code + ']';
                } else {
                    validateStr += 'equals[' + validate.equals + ']';
                }
                if (validate.equals.desc) {
                    result['data-errormessage-pattern-mismatch'] = validate.equals.desc;
                }
            }
            if (validate.minSize !== undefined) {
                if (validateStr) {
                    validateStr += ',';
                }
                if (this.type(validate.minSize) === 'number') {
                    validateStr += 'minSize[' + validate.minSize + ']';
                } else if (this.type(validate.minSize) === 'object') {
                    if (validate.minSize.code) {
                        validateStr += 'minSize[' + validate.minSize.code + ']';
                    }
                    if (validate.minSize.desc) {
                        result['data-errormessage-range-underflow'] = validate.minSize.desc;
                    }
                }
            }
            if (validate.maxSize !== undefined) {
                if (validateStr) {
                    validateStr += ',';
                }
                if (this.type(validate.maxSize) === 'number') {
                    validateStr += 'maxSize[' + validate.maxSize + ']';
                } else if (this.type(validate.maxSize) === 'object') {
                    if (validate.maxSize.code) {
                        validateStr += 'maxSize[' + validate.maxSize.code + ']';
                    }
                    if (validate.maxSize.desc) {
                        result['data-errormessage-range-overflow'] = validate.maxSize.desc;
                    }
                }
            }
            if (validate.min !== undefined) {
                if (validateStr) {
                    validateStr += ',';
                }
                if (this.type(validate.min) === 'number') {
                    validateStr += 'min[' + validate.min + ']';
                } else if (this.type(validate.min) === 'object') {
                    if (validate.min.code) {
                        validateStr += 'min[' + validate.min.code + ']';
                    }
                    if (validate.min.desc) {
                        result['data-errormessage-range-underflow'] = validate.min.desc;
                    }
                }
            }
            if (validate.max !== undefined) {
                if (validateStr) {
                    validateStr += ',';
                }
                if (this.type(validate.max) === 'number') {
                    validateStr += 'max[' + validate.max + ']';
                } else if (this.type(validate.max) === 'object') {
                    if (validate.max.code) {
                        validateStr += 'max[' + validate.max.code + ']';
                    }
                    if (validate.max.desc) {
                        result['data-errormessage-range-overflow'] = validate.max.desc;
                    }
                }
            }
            if (validate.custom !== undefined) {
                if (validateStr) {
                    validateStr += ',';
                }
                if (this.type(validate.custom) === 'string') {
                    validateStr += 'custom[' + validate.custom + ']';
                } else if (this.type(validate.custom) === 'object') {
                    if (validate.custom.code) {
                        validateStr += 'custom[' + validate.custom.code + ']';
                    }
                    if (validate.custom.desc) {
                        result['data-errormessage-custom-error'] = validate.custom.desc;
                    }
                }
            }
            if (validate.future !== undefined) {
                if (validateStr) {
                    validateStr += ',';
                }
                if (this.type(validate.future) === 'string') {
                    validateStr += 'future[' + validate.future + ']';
                } else if (this.type(validate.future) === 'object') {
                    if (validate.future.code) {
                        validateStr += 'future[' + validate.future.code + ']';
                    }
                    if (validate.future.desc) {
                        result['data-errormessage-type-mismatch'] = validate.future.desc;
                    }
                }
            }
            if (validate.past !== undefined) {
                if (validateStr) {
                    validateStr += ',';
                }
                if (this.type(validate.past) === 'string') {
                    validateStr += 'past[' + validate.past + ']';
                } else if (this.type(validate.past) === 'object') {
                    if (validate.past.code) {
                        validateStr += 'past[' + validate.past.code + ']';
                    }
                    if (validate.past.desc) {
                        result['data-errormessage-type-mismatch'] = validate.past.desc;
                    }
                }
            }

            result['data-validation-engine'] = 'validate[' + validateStr + ']';

            return result;
        },
        // 校验form表单
        validate: function($form) {
            if ($form.is("form")) {
                return $form.validationEngine('validate');
            } else if ($form.attr('data-validation-engine')) {
                $form.validationEngine('validate');
            } else {
                $form.validationEngine('hide');
            }
        },
        tooltip: function($element, titleName) {
            if (!titleName) {
                titleName = 'title';
            }
            $('[' + titleName + ']', $element).each(function() {
                if ($(this).attr(titleName)) {
                    $(this).tooltip({
                        html: true,
                        title: $(this).attr(titleName)
                    });
                } else {
                    $(this).tooltip('destroy');
                }
            });
        },
        checked: function($element, value) {
            if (this.type(value) === 'undefined') {
                return $element.prop('checked');
            }
            $element.prop('checked', value);
            return value;
        },
        /******************************************* 数组相关 *********************************************************/
        // arrs: 二维数组
        // values: 数组
        index: function(arrs, values, code, jsonValue) {
            if (values != undefined && values != null) {
                if (this.type(values) === 'string' || this.type(values) === 'number') {
                    for (var i=0; i<arrs.length; i++) {
                        if (code) {
                            if (arrs[i][code] === values) {
                                return jsonValue ? arrs[i] : i;
                            }
                        } else {
                            if (arrs[i] === values) {
                                return jsonValue ? arrs[i] : i;
                            }
                        }
                    }
                } else if (this.type(values) === 'array') {
                    for (var i=0; i<arrs.length; i++) {
                        var subArr = arrs[i];
                        for (var j=0; j<subArr.length; j++) {
                            if (subArr[j] !== values[j]) {
                                break;
                            }
                            if (j === subArr.length - 1) {
                                return jsonValue ? arrs[i] : i;
                            }
                        }
                    }
                }
            }
            return jsonValue ? null : -1;
        },
        find: function(rows, keys, values) {
            for (var i=0; i<rows.length; i++) {
                var row = rows[i];
                for (var j=0; j<keys.length; j++) {
                    if (row[keys[j]] !== values[j]) {
                        break;
                    }
                    if (j === keys.length - 1) {
                        return i;
                    }
                }
            }
            return -1;
        },

        /******************************************* 虚拟vm   *********************************************************/
        bind: function(param) {
            if (param.container && !param.el) {
                param.el = '[data-container=' + param.container + ']';
                delete param.container;
            }
            if (this.layout() === 'modal') {
                var vm = new vue(param);
                Const.global.modalVm = vm;
                return vm;
            } else {
                param.parent = Const.global.currentVm;
                return new vue(param);
            }
        },
        vm: function(parent, key, sub) {
            var vm;
            if (!parent) {
                vm = Const.global.modal.vm;
            }
            if (parent && key) {
                for (var i=0; i<parent.$children.length; i++) {
                    if (key === (parent.$children[i].name || parent.$children[i].$vnode.key)) {
                        vm = parent.$children[i];
                        break;
                    }
                }
            } else {
                vm = parent;
            }
            if (vm) {
                if (sub) {
                    for (var i=0; i<vm.$children.length; i++) {
                        if (sub === (vm.$children[i].name || vm.$children[i].$vnode.key)) {
                            return vm.$children[i];
                        }
                    }
                }
                return vm;
            }
        },
        refresh: function(param) {
            if (param && param.vm) {
                var tagType = param.vm.$options._componentTag;
                if (tagType) {
                    if (tagType === 'bv-table') {
                        if (param.checked === false) {
                            param.vm.onHeadCheck(undefined, false, true);
                            return;
                        }
                        if (param.query) {
                            param.vm.innerFilterMore = true;
                            param.vm.localQuery = param.query;
                        }
                        if (param.initParam) {
                            var paramList = [];
                            for (var p in param.initParam) {
                                paramList.push({
                                    name: p,
                                    operate: '=',
                                    value: param.initParam[p]
                                });
                            }
                            param.vm.innerInitParamList = paramList;
                        }
                        if (param.initParamList) {
                            param.vm.innerInitParamList = param.initParamList;
                        }
                        if (param.initRows) {
                            param.vm.innerInitRows = param.initRows;
                        }
                        if (param.chooseResult) {
                            param.vm.innerChooseResult = param.chooseResult;
                        }
                        // type支持load-仅加载一次，空或refresh-每次都做加载
                        if (!param.type || param.type !== 'load' || !param.vm.innerInited) {
                            param.vm.refresh();
                        }
                    } else if (tagType === 'bv-form') {
                        if (this.type(param.clazz) !== 'undefined') {
                            param.vm.innerClass = param.clazz;
                        }
                        if (param.title) {
                            param.vm.innerTitle = param.title;
                        }
                        if (param.entity) {
                            this.clone(param.vm.innerEntity, param.entity);
                            /// param.vm.innerEntity = this.mix(param.vm.innerEntity, param.entity);
                        }
                        if (param.editType) {
                            param.vm.innerEditType = param.editType;
                        }
                        if (param.show) {
                            if (param.show.name) {
                                param.vm.showColumn(param.show.name);
                            }
                            if (param.show.names) {
                                var names = param.show.names.split(',');
                                for (var i=0; i<names.length; i++) {
                                    param.vm.showColumn($.trim(names[i]));
                                }
                            }
                            if (param.show.operate) {
                                param.vm.showOperate(param.show.operate);
                            }
                        }
                        if (param.hide) {
                            if (param.hide.name) {
                                param.vm.hideColumn(param.hide.name);
                            }
                            if (param.hide.names) {
                                var names = param.hide.names.split(',');
                                for (var i=0; i<names.length; i++) {
                                    param.vm.hideColumn($.trim(names[i]));
                                }
                            }
                            if (param.hide.operate) {
                                param.vm.hideOperate(param.hide.operate);
                            }
                        }
                        if (param.name) {
                            if (param.initRows) {
                                this.refresh({
                                    id: param.id + '-' + param.name,
                                    innerInitRows: param.initRows
                                });
                            }
                        } else if (param.name && this.type(param.value) !== 'undefined') {
                            param.vm.innerEntity[param.name] = param.value;
                        }
                    } else if (tagType === 'bv-tree') {
                        if (param.initParamList) {
                            param.vm.innerInitParamList = param.initParamList;
                        }
                        if (param.initEntity !== undefined) {
                            param.vm.innerInitEntity = param.initEntity;
                        }
                        param.vm.init();
                    } else if (tagType === 'bv-chart') {
                        if (param.type) {
                            if (param.vm.innerType !== param.type) {
                                param.vm.innerType = param.type;
                                param.vm.localChart = null;
                            }
                        }
                        if (param.labels) {
                            param.vm.innerLabels = param.labels;
                        }
                        if (param.datasets) {
                            param.vm.innerDatasets = param.datasets;
                        }
                        param.vm.refresh();
                    } else if (tagType === 'bv-textfield') {
                        if (param.value !== undefined) {
                            param.vm.innerEntity[param.vm.name] = param.value;
                        }
                    }
                } else if (param.value !== undefined) {
                    param.vm.innerEntity[param.vm.name] = param.value;
                }
            }
        },
        expand: function(treeId, treeNode) {
            var menuTree = $.fn.zTree.getZTreeObj(treeId);
            menuTree.expandNode(treeNode, null, null, null, true);
        },
        reset: function(id, names) {
            if (!id) {
                return;
            }
            /*var vm = this.vm(id);
            if (vm) {
                if (vm.$is) {
                    if (vm.$is === 'bv-form') {
                        if (names) {
                            var nameArr = names.split(',');
                            for (var i=0; i<nameArr.length; i++) {
                                vm.innerEntity[nameArr[i].trim()] = null;
                            }
                        } else {
                            vm.innerEntity = vm.innerInitEntity;
                        }
                    }
                }
            }*/
        },
        value: function(id, name) {
            if (!id || !name) {
                return;
            }
            /*var vm = this.vm(id);
            if (vm) {
                if (vm.$is) {
                    if (vm.$is === 'bv-table') {
                        if (name === 'chooseResult') {
                            return vm.chooseResult;
                        }
                    }
                }
            }*/
        },
        // 显示错误提示信息
        show: function(param) {
            if (param) {
                if (param.vm) {
                    // form表单显示隐藏
                    // var _vm = this.vm(param.id);
                    if (param.name) {
                        param.vm.showColumn(param.name);
                    }
                    if (param.operate) {
                        param.vm.showOperate(param.operate);
                    }
                } else if (param.message) {
                    if (Const.init.show.type === 'alert') {
                        // 弹窗提示
                        if (!param.size) {
                            if (param.message) {
                                if (param.message.length > Const.init.show.lg) {
                                    param.size = 'lg';
                                } else if (param.message.length > Const.init.show.md) {
                                    param.size = 'md';
                                }
                            }
                        }
                        this.alert({
                            size: param.size || 'sm',
                            level: param.level || 'info',
                            title: param.title || '提示',
                            content: param.message
                        });
                    }/* else if (param.$element) {
                     // 判断是否table的按钮
                     if (param.$element.closest('.bv-table-operate').length === 1) {
                     // 浮动提示置于按钮下方
                     param.position = 'bottomLeft';
                     } else {
                     param.position = 'topRight';
                     }
                     param.$element = this.button(param.$element);
                     param.$element.validationEngine('showPrompt', param.message, 'error', param.position || 'topRight', true);
                     }*/ else {
                        // error-warn-info-success
                        var level = param.level || 'info';
                        if (level === 'warn') {
                            level = 'warning';
                        }
                        var conf = {};
                        if (param.$element) {
                            conf.target = param.$element.closest('form');
                        } else {
                            var layout = param.layout || this.layout();
                            if (layout === 'modal') {
                                conf.target = $('#modal .modal-body');
                            } else if (layout === 'body') {
                                conf.target = $('.bv-content[data-active=true]');
                            }
                            if (!conf.target || conf.target.length === 0) {
                                conf.target = $('body');
                            }
                        }
                        if (level === 'error' || level === 'info') {
                            conf.timeOut = 0;
                        }
                        toastr[level](param.message, param.title, conf);
                    }
                } else if (param === 'hide') {
                    toastr.hide();
                }
            }
        },
        hide: function(param) {
            if (param) {
                if (param.vm) {
                    // form表单显示隐藏
                    // var _vm = this.vm(param.id);
                    if (param.name) {
                        param.vm.hideColumn(param.name);
                    }
                    if (param.operate) {
                        param.vm.hideOperate(param.operate);
                    }
                } else {
                    param.validationEngine('hide');
                }
            }
        },
        /******************************************* 辅助工具 *********************************************************/
        guid: function() {
            var d = new Date().getTime();
            var uuid = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
                var r = (d + Math.random()*16)%16 | 0;
                d = Math.floor(d/16);
                return (c=='x' ? r : (r&0x3|0x8)).toString(16);
            });
            return uuid;
        },
        debug: function(title, content) {
            if (Const.init.debug) {
                console.log(title, content);
            }
        },
        heredoc: function(fn) {
            return fn.toString().replace(/^[^\/]+\/\*!?\s?/, '').replace(/\*\/[^\/]+$/, '').trim().replace(/>\s*</g, '><');
        },
        /******************************************* 内部使用 *********************************************************/
        // 根据页码、每页显示记录数获取偏移量
        offset: function(page, limit) {
            if (page !== undefined && limit !== undefined) {
                return (page -1) * limit;
            }
            return 0;
        },
        // row: {col1: 'xx', col2: 'xx', ..}
        // keys: ['col1', ...]
        keyValue: function(row, keys) {
            var values = [];
            for (var i=0; i<keys.length; i++) {
                values.push(row[keys[i]]);
            }
            return values;
        },
        // 滚动条美化
        scroll: function($element) {
            if (Const.global.browserVersion > 8 && !Const.global.isMobile) {
                if ($element.getNiceScroll().length === 0) {
                    $element.niceScroll({
                        cursorcolor: '#7B7C7E',
                        horizrailenabled: false,
                        //                    touchbehavior: false,
                        //                    cursoropacitymin: 0.5,
                        //                    cursoropacitymax: 1,
                        //                    autohidemode: false,
                        cursorwidth: 10,
                        cursorborder: "1px solid #868688"
                    });
                } else {
                    // TODO:
                    $element.getNiceScroll().resize();
                }
            }
        },
        // 触发事件
        resize: function() {
            $(window).resize();
        },
        // tab用
        checkTab: function(tabs, tab) {
            for (var i=0; i<tabs.tabs.length; i++) {
                if (tabs.tabs[i].id === tab.id) {
                    tabs.innerCurrentIndex = i;
                    tabs.refresh();
                    return;
                }
            }
            tabs.tabs.push(tab);
            tabs.innerCurrentIndex = tabs.tabs.length - 1;
        },
        currentTab: function(tabs) {
            if (tabs) {
                return $('[data-target=' + tabs.tabs[tabs.innerCurrentIndex].id + ']', tabs.$element);
            } else {
                return $('[data-target=' + Const.global.mainTabs.tabs[tabs.innerCurrentIndex].id + ']', tabs.$element);
            }
        },
        currentPanel: function(tabs) {
            if (tabs) {
                return $('#' + tabs.tabs[tabs.innerCurrentIndex].id, tabs.$element);
            } else {
                return $('#' + Const.global.mainTabs.tabs[Const.global.mainTabs.innerCurrentIndex].id, Const.global.mainTabs.$element);
            }
        },
        // 限制输入
        fix: function(event, type) {
            var r = '';
            if (type) {
                if (type === 'integer') {
                    r = /\D+/;
                } else if (type === 'number') {
                    r = /[^\d.-]/;
                } else if (type === '+number') {
                    r = /[^\d.]/;
                }
            }
            if (r) {
                event.target.value = event.target.value.replace(r, '');
            }
            return event.target.value;
        },
        // 根据字典值取对应的名称
        trans: function(value, config) {
            if ((!value && value != 0) || !config) {
                return value;
            }
            var result = undefined;
            if (config.choose) {
                var seprate = ',';
                if (config.seprate) {
                    seprate = config.seprate;
                }
                if (config.preset && !config.code) {
                    config.code = Const.init.preset[config.preset].code;
                    config.desc = Const.init.preset[config.preset].desc;
                }
                if (this.type(value) === 'number') {
                    value = this.toString(value);
                    for (var i=0; i<config.choose.length; i++) {
                        if (value === config.choose[i][config.code]) {
                            return config.choose[i][config.desc];
                        }
                    }
                } else {
                    var values = value.split(seprate);
                    for (var index=0; index<values.length; index++) {
                        for (var i=0; i<config.choose.length; i++) {
                            if (values[index] === config.choose[i][config.code]) {
                                if (result) {
                                    result += seprate;
                                } else {
                                    result = '';
                                }
                                result += config.choose[i][config.desc];
                                break;
                            }
                        }
                    }
                }
            }
            return result === undefined ? value : result;
        },
        // 数据格式化
        // 支持:
        // format('xxx', 'file')
        // format('xxx')
        // format('xxx', 'time')
        // format('xxx', 'truncate(10)')
        // format('xxx', 'xxx|yyy') -> xxx.filters.xxx = function(v, yyy) {}
        format: function(v, type) {
            if (!v) {
                return v;
            }
            if (!type || type === 'dict') {
                return v;
            }
            if (this.type(v) === 'number') {
                v = new Number(v);
            }

            var formatType = type;
            var params = null;
            if (type.indexOf('(') > 0 && type.indexOf(')') > 0) {
                type = this.replaceAll($.trim(type), ', ', ',');
                formatType = type.substring(0, type.indexOf('('));
                params = type.substring(type.indexOf('(') + 1, type.indexOf(')')).split(',');
            }

            var param = null;
            var types;
            if (type.indexOf('|') > 0) {
                types = type.split('|');
                if (types.length > 1) {
                    type = types[0];
                    param = types[1];
                }
            } else {
                types = type;
            }

            switch (formatType) {
                case 'number':
                    // number, decimals, point, thousands
                    var number = v;
                    var decimals;
                    var point;
                    var thousands;
                    if (params !== null) {
                        if (params.length > 0) {
                            decimals = params[0];
                        }
                        if (params.length > 1) {
                            point = params[1];
                        }
                        if (params.length > 2) {
                            thousands = params[2];
                        }
                    }
                    return formatNumber(number, decimals, point, thousands);
                case 'file':
                    return formatNumber(v, 0);
                case 'currency':
                    // amount, symbol, fractionSize
                    var amount = v;
                    var symbol;
                    var fractionSize;
                    if (params !== null) {
                        if (params.length > 0) {
                            symbol = params[0];
                        }
                        if (params.length > 1) {
                            fractionSize = params[1];
                        }
                    }
                    return (symbol || '\u00a5') + formatNumber(amount, isFinite(fractionSize) ? fractionSize : 2);
                case 'currencyFen':
                    var amount = this.toNumber(v) / 100;
                    var symbol;
                    var fractionSize;
                    if (params !== null) {
                        if (params.length > 0) {
                            symbol = params[0];
                        }
                        if (params.length > 1) {
                            fractionSize = params[1];
                        }
                    }
                    return (symbol || '\u00a5') + formatNumber(amount, isFinite(fractionSize) ? fractionSize : 2);
                case 'date':
                    if (this.type(v) === 'string' && v.length >= 10) {
                        return v.substring(0, 10);
                    }
                    /// return this.filter.date(v, 'yyyy-MM-dd');
                case 'datetime':
                    if (this.type(v) === 'string' && v.length >= 19) {
                        return v.substring(0, 19);
                    }
                    /// return this.filter.date(v, 'yyyy-MM-dd HH:mm:ss');
                case 'time':
                    if (this.type(v) === 'string' && v.length >= 10) {
                        return v.substring(10, 19);
                    }
                    /// return this.filter.date(v, 'HH:mm:ss');
                case 'timestamp':
                    if (this.type(v) === 'string' && v.length >= 19) {
                        return v.substring(0, 19);
                    }
                    /// return this.filter.date(v, 'yyyy-MM-dd HH:mm:ss');
                case 'truncate':
                    var str = v;
                    var length;
                    var end;
                    if (params !== null) {
                        if (params.length > 0) {
                            length = params[0];
                        }
                        if (params.length > 1) {
                            end = params[1];
                        }
                    }
                    //length，新字符串长度，truncation，新字符串的结尾的字段,返回新字符串
                    if (!str) {
                        return '';
                    }
                    str = String(str);
                    if (isNaN(length)) {
                        length = 30;
                    }
                    end = typeof end === "string" ? end : "...";
                    return str.length > length ? str.slice(0, length - end.length) + end : str;
                    /// return this.filter.truncate(v, this.toNumber(param));
                case 'hyphen':
                    //转换为连字符线风格
                    return v.replace(rhyphen, '$1-$2').toLowerCase();
                case 'camelize':
                    //提前判断，提高getStyle等的效率
                    if (!v || v.indexOf('-') < 0 && v.indexOf('_') < 0) {
                        return v;
                    }
                    //转换为驼峰风格
                    return v.replace(rcamelize, function (match) {
                        return match.charAt(1).toUpperCase()
                    });
                default:
                    // TODO: xxxxx
                    return this.filter[formatType].call(null, v, param);
            }
            return v;
        },
        // 获取当前选中行数据,如果onlyId为true则仅返回id对应的数组,否则返回该行数据对象对应的数组
        // table用
        selected: function(vm, onlyId) {
            if (vm) {
                ///var _vm = this.vm(vm);
                if (vm.localKeys && vm.innerRows && vm.localBodyChecked && vm.localBodyChecked.length > 0) {
                    var result = [];
                    for (var i=0; i<vm.localBodyChecked.length; i++) {
                        // var index = this.find(_vm.innerRows, _vm.localKeys, _vm.localBodyChecked[i]);
                        var index = vm.localBodyChecked[i];
                        if (index >= 0 && vm.innerRows.length > index) {
                            if (onlyId) {
                                result.push(this.id(vm.localKeys, vm.innerRows[index]));
                            } else {
                                result.push(vm.innerRows[index]);
                            }
                        }
                        /*                            if (_vm.innerRows[i][_vm.id].toString() === _vm.localBodyChecked[j]) {
                         if (onlyId) {
                         result.push(_vm.innerRows[i][_vm.id]);
                         } else {
                         result.push(_vm.innerRows[i]);
                         }
                         }*/
                    }
                }
                return result;
            }
            return [];
        },
        id: function(keys, row) {
            /*if (this.type(keys) === 'string') {
                keys = this.vm(keys).localKeys;
            }*/
            if (this.type(row) === 'array') {
                var result = [];
                for (var r=0; r<row.length; r++) {
                    result.push(this.id(keys, row[r]));
                }
                return result;
            }
            if (this.type(keys) === 'string') {
                return row[keys];
            }
            var t = {};
            for (var i=0; i<keys.length; i++) {
                t[keys[i]] = row[keys[i]];
            }
            return t;
        },
        attr: function(name, presetName, attr) {
            if (!attr) {
                attr = {};
            }
            var preset;
            if (this.type(presetName) != 'undefined') {
                // presetName可以为false，表示不取预设信息
                if (presetName) {
                    preset = Const.rule[presetName];
                } else {
                    preset = Const.rule[name];
                }
            }
            if (preset) {
                // 预设属性跟现有属性合并
                /*

                 */
                if (attr['data-validation-engine'] && preset['data-validation-engine']) {
                    // 合并验证规则
                    // 要求格式为'validate[required,maxSize[32],custom[onlyChar]]'
                    var presetRule = preset['data-validation-engine'];
                    var validateRule = attr['data-validation-engine'];
                    var resultRule = '';
                    if (this.startsWith(presetRule, 'validate[') && this.endsWith(presetRule, ']')
                        && this.startsWith(validateRule, 'validate[') && this.endsWith(validateRule, ']')) {
                        presetRule = presetRule.substring(9, presetRule.length - 1);
                        validateRule = validateRule.substring(9, validateRule.length - 1);
                        if (presetRule && validateRule) {
                            resultRule = 'validate[';
                            var presetArr = presetRule.split(',');
                            var validateArr = validateRule.split(',');
                            for (var i=0; i<presetArr.length; i++) {
                                if (!this.contains(validateArr, presetArr[i])) {
                                    validateArr.push(presetArr[i]);
                                }
                            }
                            for (var i=0; i<validateArr.length; i++) {
                                resultRule += validateArr[i] + ',';
                            }
                            presetArr = [];
                            validateArr = [];
                            resultRule = resultRule.substring(0, resultRule.length - 1) + ']';
                        }
                    }
                    attr['data-validation-engine'] = resultRule;
                }
                if (preset.maxlength && !attr.maxlength) {
                    attr.maxlength = preset.maxlength;
                }
            }
            return attr;
        },

        initDefault: function(vm) {
            if (!vm.innerAttr) {
                vm.innerAttr = {};
            }
            if (vm.id) {
                vm.innerAttr.id = vm.id;
            }
            if (vm.name) {
                vm.innerAttr.name = vm.name;
            }
            if (!this.isEmpty(vm.defaultValue) && this.isEmpty(vm.innerValue)) {
                if (vm.defaultValue === '$sysdate') {
                    vm.defaultValue = this.date();
                }
                vm.innerValue = vm.defaultValue;
            }
            if (!this.isEmpty(vm.innerValue) && vm.innerEntity && vm.name) {
                vm.innerEntity[vm.name] = vm.innerValue;
            }
            if (this.isEmpty(vm.innerValue) && vm.innerEntity && vm.name) {
                vm.innerValue = vm.innerEntity[vm.name];
            }
            if (!vm.innerCode && vm.preset) {
                vm.innerCode = Const.init.preset[vm.preset].code;
                vm.innerDesc = Const.init.preset[vm.preset].desc;
            }
            if (!this.isEmpty(vm.initParam)) {
                if (this.isEmpty(vm.initParamList)) {
                    vm.initParamList = [];
                }
                var data;
                if (this.type(vm.initParam) === 'function') {
                    // TODO: !!!!
                    data = vm.initParam.call(null, '');
                } else {
                    data = vm.initParam;
                }
                for (var p in data) {
                    vm.initParamList.push({
                        name: p,
                        operate: '=',
                        value: data[p]
                    });
                }
            }
            if (!this.isEmpty(vm.validate)) {
                /*if (!vm.validateJson) {
                    vm.validateJson = [];
                }*/
                vm.innerAttr = this.mix(vm.innerAttr, this.validateMix(vm.validate));
            }
            // 处理默认排序
            if (vm.orders) {
                var initOrders = vm.orders.split(',');
                for (var i=0; i<initOrders.length; i++) {
                    var order = $.trim(initOrders[i]);
                    if (order) {
                        var arr = order.replace(/\s+/g, ' ').split(' ');
                        if (arr.length === 1) {
                            vm.localOrderList.push({
                                name: $.trim(arr[0])
                            });
                        } else if (arr.length === 2) {
                            vm.localOrderList.push({
                                name: $.trim(arr[0]),
                                sort: $.trim(arr[1])
                            });
                        }
                    }
                }
            }
        },
        initId: function(vm) {
            if (!vm.innerAttr.id) {
                vm.innerAttr.id = vm.name;
            }
        },

        /*
         config: {
            initOptions: [],
            extraOptions: [],
            excludeOptions: [],
            preset: '',
            code: '',
            desc: '',
            label: '',
            trans: '',
            type: 'select-radio-checkbox-auto',
            show: function() {}
        }
        返回格式: {
            options: [],
            groups: []
        }
        */
        transOptions: function(config) {
            if (!config) {
                config = {};
            }
            if (!config.type) {
                config.type = 'select';
            }
            var results = {};

            var initOptions = config.initOptions;
            if (!initOptions) {
                initOptions = [];
            }
            /*var presets = Const.init[config.preset || 'default'];
            if (!presets) {
                return results;
            }*/
            var options = [];
            if (this.type(initOptions) === 'string' && this.startsWith(initOptions, '#')) {
                var $dicts = Const.dicts[initOptions.substring(1)];
                if ($dicts == null) {
                    initOptions = [];
                } else {
                    initOptions = this.clone(Const.dicts[initOptions.substring(1)]);
                }
            }
            if (this.type(initOptions) === 'object') {
                for (var p in initOptions) {
                    options.push({
                        code: this.toString(p),
                        name: initOptions[p]
                    });
                }
            } else if (config.preset === 'json') {
                for (var i=0; i<initOptions.length; i++) {
                    for (p in initOptions[i]) {
                        options.push({
                            code: p,
                            name: initOptions[i][p]
                        });
                    }
                }
            } else if (config.preset === 'simple') {
                for (var i=0; i<initOptions.length; i++) {
                    options.push({
                        name: initOptions[i]
                    });
                }
            } else {
                options = initOptions;
            }

            if (config.type !== 'auto' && config.show && this.type(config.show) === 'function') {
                for (var i=0; i<options.length; i++) {
                    options[i][config.desc] = config.show.call(null, options[i]);
                }
            }

            if (config.desc && !this.isEmpty(config.trans)) {
                if (this.type(config.trans) === 'array') {
                    for (var i=0; i<options.length; i++) {
                        for (var j=0; j<config.trans.length; j++) {
                            if (options[i][config.code] === config.trans[j][config.code]) {
                                options[i][config.desc] = config.trans[j][config.desc];
                            }
                        }
                    }
                } else if (this.type(config.trans) === 'object') {
                    for (var i=0; i<options.length; i++) {
                        options[i][config.desc] = config.trans[options[i][config.code]];
                    }
                }
            }
            // 处理excludes
            if (config.excludeOptions && config.excludeOptions.length > 0) {
                for (i=0; i<config.excludeOptions.length; i++) {
                    var index = this.index(options, config.excludeOptions[i], config.code);
                    if (index >= 0) {
                        options.splice(index, 1);
                    }
                }
            }

            // 仅select支持group分组
            if (config.type === 'select' && config.label) {
                var groups = [];

                var currentGroup = '';
                var currentSub = [];
                for (var i=0; i<options.length; i++) {
                    var option = options[i];
                    if (option[config.label]) {
                        if (currentGroup && currentGroup !== option[config.label]) {
                            groups.push({
                                label: option[config.label],
                                options: this.clone(currentSub)
                            });
                            currentSub = [];
                        }
                        currentGroup = option[config.label];
                        currentSub.push(option);

                        if (i === options.length - 1) {
                            groups.push({
                                label: option[config.label],
                                options: this.clone(currentSub)
                            });
                            currentGroup = '';
                            currentSub = [];
                        }
                    } else {
                        options.push(option);
                    }
                }
                results.groups = groups;
            }
            if (options && config.extraOptions && config.extraOptions.length > 0) {
                for (var i=0; i<config.extraOptions.length; i++) {
                    options.push(config.extraOptions[i]);
                }
            }
            // auto支持$map属性，不支持options属性
            if (config.type === 'auto') {
                var shows = [];
                var map = {};
                for (var i=0; i<options.length; i++) {
                    var desc = this.transAutoShow({
                        show: config.show,
                        preset: config.preset,
                        code: config.code,
                        desc: config.desc,
                        data: options[i]
                    });
                    shows.push(desc);
                    map[desc] = options[i];
                }
                // return results;
                results.shows = shows;
                results.map = map;
            }
            results.options = options;
            return results;
        },
        doAutoProcess: function(vm, query, process) {
            if (this.isEmpty(vm.innerChoose) && vm.url) {
                var util = this;
                if (vm.entityName) {
                    util.post({
                        url: vm.url,
                        data: {
                            entityName: vm.entityName,
                            distinct: vm.distinct,
                            columns: (vm.innerDesc ? (vm.innerCode + "," + vm.innerDesc) : vm.innerCode) + (vm.extraColumns ? ',' + vm.extraColumns : ''),
                            initParamList: util.transInitParam(vm),
                            orderList: vm.localOrderList,
                            q: query,
                            limit: vm.limit
                        },
                        success: function(res) {
                            var results = util.transOptions({
                                type: 'auto',
                                initOptions: util.data(res).data || util.data(res),
                                extraOptions: vm.$extras,
                                excludeOptions: vm.$excludes,
                                preset: vm.preset,
                                code: vm.innerCode,
                                desc: vm.innerDesc,
                                label: vm.label,
                                trans: vm.trans,
                                show: vm.show
                            });
                            if (results.options) {
                                vm.innerOptions = results.options;
                            }
                            if (results.shows) {
                                vm.localShows = results.shows;
                            }
                            if (results.map) {
                                vm.localMap = results.map;
                            }
                            process(vm.localShows);
                        }
                    });
                } else {
                    if (vm.method === 'post') {
                        util.post({
                            url: vm.url,
                            data: {
                                initParamList: util.transInitParam(vm),
                                orderList: vm.localOrderList,
                                q: query,
                                limit: vm.limit
                            },
                            success: function(res) {
                                var results = util.transOptions({
                                    type: 'auto',
                                    initOptions: util.data(res).data || util.data(res),
                                    extraOptions: vm.$extras,
                                    excludeOptions: vm.$excludes,
                                    preset: vm.preset,
                                    code: vm.innerCode,
                                    desc: vm.innerDesc,
                                    label: vm.label,
                                    trans: vm.trans,
                                    show: vm.show
                                });
                                if (results.options) {
                                    vm.innerOptions = results.options;
                                }
                                if (results.shows) {
                                    vm.localShows = results.shows;
                                }
                                if (results.map) {
                                    vm.localMap = results.map;
                                }
                                process(vm.localShows);
                            }
                        });
                    } else if (vm.method === 'get') {
                        util.get({
                            url: vm.url,
                            data: util.mix(util.transParam(util.transInitParam(vm)), {
                                q: query,
                                limit: vm.limit
                            }),
                            success: function(res) {
                                var results = util.transOptions({
                                    type: 'auto',
                                    initOptions: util.data(res).data || util.data(res),
                                    extraOptions: vm.$extras,
                                    excludeOptions: vm.$excludes,
                                    preset: vm.preset,
                                    code: vm.innerCode,
                                    desc: vm.innerDesc,
                                    label: vm.label,
                                    trans: vm.trans,
                                    show: vm.show
                                });
                                if (results.options) {
                                    vm.innerOptions = results.options;
                                }
                                if (results.shows) {
                                    vm.localShows = results.shows;
                                }
                                if (results.map) {
                                    vm.localMap = results.map;
                                }
                                process(vm.localShows);
                            }
                        });
                    }
                }
            } else {
                process(vm.localShows);
            }
        },
        /*
        config: {
            show: function() {},
            preset: '',
            data: ''
        }
        */
        transAutoShow: function(config) {
            if (config.show && this.type(config.show) === 'function') {
                return config.show.call(null, config.data);
            } else if (config.preset === 'simple') {
                return config.data;
            } else {
                /*var presets = Const.init.preset[config.preset || 'default'];
                if (!presets) {
                    return null;
                }*/
                if (config.desc) {
                    return this.toString(config.data[config.desc]);
                } else {
                    return this.toString(config.data[config.code]);
                }
            }
        },

        transInitParam: function(vm) {
            if (vm.initParamList) {
                for (var i=0; i<vm.initParamList.length; i++) {
                    var initParam = vm.initParamList[i];
                    if (initParam.from) {
                        if (initParam.duplex) {
                            initParam.value = vm[initParam.duplex][initParam.from];
                        } else {
                            initParam.value = vm.innerEntity[initParam.from];
                        }
                        if (initParam.operate && initParam.operate === 'like') {
                            initParam.value = '%' + initParam.value + '%';
                        }
                        vm.initParamList[i] = initParam;
                    }
                }
                return vm.initParamList;
            }
        },
        // select,radio,checkbox,auto公用
        initSelectParam: function(vm) {
            if (!this.isEmpty(vm.multiple)) {
                vm.innerAttr.multiple = vm.multiple;
            }

            /*if (this.isEmpty(vm.innerEntity[vm.name])) {
                vm.innerEntity[vm.name] = this.toString(vm.defaultValue) || '';
            } else {
                vm.innerEntity[vm.name] = this.toString(vm.innerEntity[vm.name]) || '';
            }*/
        },
        initSelectData: function(vm, type, trigger) {
            var util = this;
            if (type !== 'auto' && vm.url && util.isEmpty(vm.innerChoose)) {
                util.transInitParam(vm);
                if (vm.entityName) {
                    var columnNames = vm.innerCode;
                    if (util.isEmpty(vm.trans)) {
                        if (vm.innerDesc) {
                            columnNames += ',' + vm.innerDesc;
                        }
                        if (vm.label) {
                            columnNames += ',' + vm.label;
                        }
                        if (vm.extraColumns) {
                            columnNames += ',' + vm.extraColumns;
                        }
                    }
                    if (vm.method === 'post') {
                        util.post({
                            url: vm.url,
                            data: {
                                entityName: vm.entityName,
                                columns: columnNames,
                                initParamList: vm.initParamList,
                                orderList: vm.localOrderList
                            },
                            success: function(data) {
                                util.initSelectOptions(vm, util.data(data), type, trigger);
                            }
                        });
                    } else if (vm.method === 'get') {
                        var data = {
                            entityName: vm.entityName,
                            columns: columnNames
                        };
                        if (!util.isEmpty(vm.initParamList)) {
                            data = util.mix(data, util.transParam(vm.initParamList));
                        }
                        util.get({
                            url: vm.url,
                            data: data,
                            success: function(data) {
                                util.initSelectOptions(vm, util.data(data), type, trigger);
                            }
                        });
                    }
                } else if (vm.localCustomLoad) {
                    if (vm.method === 'post') {
                        util.post({
                            url: vm.url,
                            data: {
                                initParamList: vm.initParamList,
                                orderList: vm.localOrderList
                            },
                            success: function(data) {
                                util.initSelectOptions(vm, util.data(data), type, trigger);
                            }
                        });
                    } else if (vm.method === 'get') {
                        util.get({
                            url: vm.url,
                            data: util.transParam(vm.initParamList),
                            success: function(data) {
                                util.initSelectOptions(vm, util.data(data), type, trigger);
                            }
                        });
                    }
                }
            } else if (type === 'auto' && vm.initUrl && util.isEmpty(vm.innerChoose) && !util.isEmpty(vm.innerEntity[vm.name])) {
                util.transInitParam(vm);
                var initParamList;
                if (vm.initParamList) {
                    initParamList = util.clone(vm.initParamList);
                } else {
                    initParamList = [];
                }
                initParamList.push({
                    name: vm.innerCode,
                    value: vm.innerEntity[vm.name],
                    operate: '='
                });
                if (vm.method === 'post') {
                    util.post({
                        url: vm.initUrl,
                        data: {
                            entityName: vm.entityName,
                            columns: (vm.innerDesc ? (vm.innerCode + "," + vm.innerDesc) : vm.innerCode) + (vm.extraColumns ? ',' + vm.extraColumns : ''),
                            distinct: vm.distinct,
                            initParamList: initParamList
                        },
                        success: function(data) {
                            if (util.data(data)) {
                                $('input', vm.$el).val(util.transAutoShow({
                                    show: vm.show,
                                    preset: vm.preset,
                                    code: vm.innerCode,
                                    desc: vm.innerDesc,
                                    data: util.data(data)
                                }));
                            }
                            if (util.type(vm.onItemInit) === 'function') {
                                vm.onItemInit.call(null, util.data(data));
                            }
                        }
                    });
                } else if (vm.method === 'get') {
                    util.get({
                        url: vm.initUrl,
                        data: util.transParam(initParamList),
                        success: function(data) {
                            if (util.data(data)) {
                                $('input', vm.$el).val(util.transAutoShow({
                                    show: vm.show,
                                    preset: vm.preset,
                                    code: vm.innerCode,
                                    desc: vm.innerDesc,
                                    data: util.data(data)
                                }));
                            }
                            if (util.type(vm.onItemInit) === 'function') {
                                vm.onItemInit.call(null, util.data(data));
                            }
                        }
                    });
                }
            } else {
                if (util.type(vm.innerChoose) === 'object' && !util.isEmpty(vm.innerEntity[vm.load])) {
                    util.initSelectOptions(vm, vm.innerChoose ? util.clone(vm.innerChoose[vm.innerEntity[vm.load]]) : vm.innerChoose, type, trigger);
                } else {
                    util.initSelectOptions(vm, vm.innerChoose ? util.clone(vm.innerChoose) : vm.innerChoose, type, trigger);
                }
                // TODO: 不加延迟会出js错误，不好定位，暂时处理一下
                /*setTimeout(function() {

                }, 300);*/
            }
        },
        transParam: function(paramList) {
            var param = {};
            if (this.type(paramList) === 'array' && paramList.length > 0) {
                for (var i=0; i<paramList.length; i++) {
                    param[paramList[i].name] = paramList[i].value;
                }
            }
            return param;
        },
        initSelectOptions: function(vm, initOptions, type, trigger) {
            if (trigger && vm.innerEntity[vm.name]) {
                vm.innerEntity[vm.name] = null;
                // TODO:此处可能有问题
                /*                    for (var i=0; i<options.length; i++) {
                 if (options[i][vm.innerCode] === vm.innerEntity[vm.name]) {
                 return options;
                 }
                 }*/
            }

            var results = this.transOptions({
                type: type,
                initOptions: initOptions,
                extraOptions: vm.extras,
                excludeOptions: vm.excludes,
                preset: vm.preset,
                code: vm.innerCode,
                desc: vm.innerDesc,
                label: vm.label,
                trans: vm.trans,
                show: vm.show
            });
            if (results.options) {
                vm.innerOptions = results.options;
                if (type === 'auto') {
                    for (var i=0; i<vm.innerOptions.length; i++) {
                        if (vm.innerOptions[i][vm.innerCode] === vm.innerEntity[vm.name] || vm.innerOptions[i][vm.innerDesc] === vm.innerEntity[vm.name]) {
                            $('input', vm.$el).val(this.transAutoShow({
                                show: vm.show,
                                preset: vm.preset,
                                code: vm.innerCode,
                                desc: vm.innerDesc,
                                data: vm.innerOptions[i]
                            }));
                            break;
                        }
                    }
                } else if (type === 'select') {
                    if (!vm.innerEntity[vm.name] && !vm.initOption && vm.innerOptions.length > 0) {
                        vm.innerEntity[vm.name] = !this.isEmpty(vm.innerOptions[0][vm.innerDesc]) ? vm.innerOptions[0][vm.innerDesc] : (!this.isEmpty(vm.innerOptions[0][vm.innerCode]) ? vm.innerOptions[0][vm.innerCode] : vm.innerOptions[0]);
                    }
                }
            }
            if (vm.innerGroups) {
                vm.innerGroups = results.groups;
            }
            if (vm.localShows) {
                vm.localShows = results.shows;
            }
            if (vm.localMap) {
                vm.localMap = results.map;
            }
        },
        // 内部使用
        datepicker: function($element, format) {
            if (format) {
                format = format.replace('yyyy', 'YYYY').replace('dd', 'DD').replace('hh', 'HH');
            }
            return $element.datetimepicker({
                locale: 'zh-cn',
                dayViewHeaderFormat: 'YYYY年MMM',
                format: format,
                useCurrent: false,
                showTodayButton: true,
                showClear: true,
                allowInputToggle: true,
                tooltips: {
                    today: '今日',
                    clear: '清除',
                    close: '关闭',
                    selectMonth: '选择月份',
                    prevMonth: '上月',
                    nextMonth: '下月',
                    selectYear: '选择年',
                    prevYear: '上一年',
                    nextYear: '下一年',
                    selectDecade: '十年',
                    prevDecade: '上一个十年',
                    nextDecade: '下一个十年',
                    prevCentury: '上个世纪',
                    nextCentury: '下个世纪',
                    pickHour: '时',
                    incrementHour: '加一小时',
                    decrementHour: '减一小时',
                    pickMinute: '分',
                    incrementMinute: '加一分',
                    decrementMinute: '减一分',
                    pickSecond: '秒',
                    incrementSecond: '加一秒',
                    decrementSecond: '减一秒',
                    togglePeriod: '时期',
                    selectTime: '选择时间'
                }
            });
        },
        isModern: function() {
            return ie > 9;
        },
        selectNode: function(treeNode) {
            if (treeNode) {
                // Const.global.menuTree.localTree.expandNode(treeNode.getParentNode(), true, true, null, true);
                if (!treeNode.isParent && treeNode.level > 1) {
                    var node = treeNode;
                    while (node.level > 0) {
                        node = node.getParentNode();
                        if (!node.open) {
                            var expandedNodes = Const.global.menuTree.localTree.getNodesByParam('open', true, node.getParentNode());
                            for (var i = expandedNodes.length - 1; i >= 0; i--) {
                                if (expandedNodes[i].level !== 0) {
                                    Const.global.menuTree.localTree.expandNode(expandedNodes[i], false);
                                }
                            }
                        }
                    }
                }
                Const.global.menuTree.localTree.selectNode(treeNode);
            } else {
                Const.global.menuTree.localTree.cancelSelectedNode();
            }
        },
        // 内部调用
        initConfig: function() {
            var util = this;
            this.get({
                url: Const.url.cache.config,
                async: false,
                success: function(data) {
                    if (data) {
                        Const.init = util.mix(Const.init, util.data(data));
                    }
                }
            });
        },
        // 初始化枚举类定义
        initEnums: function() {
            var util = this;
//            Const.enums = {};
            this.get({
                url: Const.url.cache.enums,
                success: function(data) {
                    if (data) {
                        Const.enums = util.mix(Const.enums, util.data(data));
                    }
                }
            });
        },
        // 初始化实体类定义
        /*initEntities: function() {
            var util = this;
            this.get({
                url: Const.url.cache.entities,
                success: function(data) {
                    if (data) {
                        Const.entities = util.mix(Const.entities, util.data(data));
                    }
                }
            });
        },*/
        // 初始化字典
        initDicts: function() {
//            Const.dicts = {};
            var util = this;
            this.get({
                url: Const.url.cache.dicts,
                success: function(data) {
                    if (data) {
                        Const.dicts = util.mix(Const.dicts, util.data(data));
//                        $.extend(Const.dicts, util.data(data));
                    }
                }
            });
        },
        // 初始化参数
        initParam: function() {
//            Const.params = {};
            var util = this;
            this.get({
                url: '/param/selectAllFromCache',
                success: function(data) {
                    if (data) {
                        Const.params = util.mix(Const.params, util.data(data));
//                        $.extend(Const.params, util.data(data));
                    }
                }
            });
        },
        // 内部用，table用
        userOperates: function($element) {
            if ($element.closest('.bv-content').data('tab-prop')) {
                return $element.closest('.bv-content').data('tab-prop').userOperates;
            }
            return null;
        },
        // 内部使用
        login: function(param) {
            if (root && !root.doLogin) {
                if (!param) {
                    param = {};
                }
                param.element = '#login';
                param.url = Const.url.template.login;
                param.type = 'login';
                param.onClose = function() {
                    root.doLogin = false;
                }
                root.doLogin = true;
                this.modal(param);
            }
        },
        // 内部使用
        button: function($element) {
            if ($element) {
                if ($element.is("i") || $element.is("span")) {
                    $element = $element.closest('.btn');
                }
                return $element;
            }
        },

        toUtf8: function(str) {
            var result = [];
            for (var i = 0; i < str.length; i++) {
                var code = str.charCodeAt(i);
                if (code > 0x0000 && code <= 0x007F) {
                    // 单字节，这里并不考虑0x0000，因为它是空字节
                    // U+00000000 – U+0000007F  0xxxxxxx
                    result.push(this.toByte(code));
                } else if (code >= 0x0080 && code <= 0x07FF) {
                    // 双字节
                    // U+00000080 – U+000007FF  110xxxxx 10xxxxxx
                    // 110xxxxx
                    result.push(this.toByte(0xC0 | ((code >> 6) & 0x1F)));
                    // 10xxxxxx
                    result.push(this.toByte(0x80 | (code & 0x3F)));
                } else if (code >= 0x0800 && code <= 0xFFFF) {
                    // 三字节
                    // U+00000800 – U+0000FFFF  1110xxxx 10xxxxxx 10xxxxxx
                    // 1110xxxx
                    result.push(this.toByte(0xE0 | ((code >> 12) & 0x0F)));
                    // 10xxxxxx
                    result.push(this.toByte(0x80 | ((code >> 6) & 0x3F)));
                    // 10xxxxxx
                    result.push(this.toByte(0x80 | (code & 0x3F)));
                } else if (code >= 0x00010000 && code <= 0x001FFFFF) {
                    // 四字节
                    // U+00010000 – U+001FFFFF  11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
                } else if (code >= 0x00200000 && code <= 0x03FFFFFF) {
                    // 五字节
                    // U+00200000 – U+03FFFFFF  111110xx 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx
                } else /** if (code >= 0x04000000 && code <= 0x7FFFFFFF)*/ {
                    // 六字节
                    // U+04000000 – U+7FFFFFFF  1111110x 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx
                }
            }
            return result;
        },
        toUtf16: function(bytes) {
            var res = [];
            var i = 0;
            for (var i=0; i<bytes.length; i++) {
                if (bytes[i] < 0) {
                    bytes[i] += 256;
                }
                var code = bytes[i];
                // 对第一个字节进行判断
                if (((code >> 7) & 0xFF) == 0x0) {
                    // 单字节
                    // 0xxxxxxx
                    res.push(String.fromCharCode(bytes[i]));
                } else if (((code >> 5) & 0xFF) == 0x6) {
                    // 双字节
                    // 110xxxxx 10xxxxxx
                    var code2 = bytes[++i];
                    var byte1 = (code & 0x1F) << 6;
                    var byte2 = code2 & 0x3F;
                    var utf16 = byte1 | byte2;
                    res.push(String.fromCharCode(utf16));
                } else if (((code >> 4) & 0xFF) == 0xE) {
                    // 三字节
                    // 1110xxxx 10xxxxxx 10xxxxxx
                    var code2 = bytes[++i];
                    var code3 = bytes[++i];
                    var byte1 = (code << 4) | ((code2 >> 2) & 0x0F);
                    var byte2 = ((code2 & 0x03) << 6) | (code3 & 0x3F);
                    utf16 = ((byte1 & 0x00FF) << 8) | byte2
                    res.push(String.fromCharCode(utf16));
                } else if (((code >> 3) & 0xFF) == 0x1E) {
                    // 四字节
                    // 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
                } else if (((code >> 2) & 0xFF) == 0x3E) {
                    // 五字节
                    // 111110xx 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx
                } else /** if (((code >> 1) & 0xFF) == 0x7E)*/ {
                    // 六字节
                    // 1111110x 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx
                }
            }

            return res.join('');
        },

        base64Chars: 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=',
        base64Encode: function(bytes) {
            // var chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=';
            // var str = String(bytes);
            var block, charCode, map = this.base64Chars, output = '';
            var position;
            for (
                var idx = 0;
                // initialize result and counter
                // if the next str index does not exist:
                //   change the mapping table to "="
                //   check if d has no fractional digits
                bytes[this.toNumber(idx | 0)] || (map = '=', idx % 1);
                // "8 - idx % 1 * 8" generates the sequence 2, 4, 6, 8
                output += map.charAt(position)
            ) {
                charCode = bytes[this.toNumber(idx += 3/4)];
                if (charCode < 0) {
                    charCode += 256;
                }
                if (charCode > 0xFF) {
                    throw new InvalidCharacterError("'btoa' failed: The string to be encoded contains characters outside of the Latin1 range.");
                }
                block = block << 8 | charCode;

                position = 63 & (block >> (8 - (idx % 1 * 8)));
            }
            return output;
        },
        base64Decode: function(str) {
            if (!str) {
                return '';
            }

            str = String(str).replace(/=+$/, '');
            if (str.length % 4 == 1) {
                throw new InvalidCharacterError("'atob' failed: The string to be decoded is not correctly encoded.");
            }
            var res = [];
            // var chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=';
            for (
                // initialize result and counters
                var bc = 0, bs, buffer, idx = 0;
                // get next character
                buffer = str.charAt(idx++);
                // character found in table? initialize bit storage and add its ascii value;
                ~buffer && (bs = bc % 4 ? bs * 64 + buffer : buffer,
                    // and if not first of each 4 characters,
                    // convert the first 8 bits to one ascii character
                bc++ % 4) ? res.push(255 & bs >> (-2 * bc & 6)) : 0
            ) {
                // try to find character in table (0-63, not found => -1)
                buffer = this.base64Chars.indexOf(buffer);
            }
            return res;
        },
        simpleEncrypt: function(str) {
            var bytes = this.toUtf8(str);

            var half;
            for (half=0; half<bytes.length; half++) {
                bytes[half] = this.toByte(~bytes[half]);
            }

            half = parseInt(bytes.length / 2);

            for (var i=0; i<half; i++) {
                if (i % 2 === 1) {
                    var b = bytes[i];
                    bytes[i] = bytes[i + half];
                    bytes[i + half] = b;
                }
            }
            return this.base64Encode(bytes);
        },
        simpleDecrypt: function(str) {
            var bytes = this.base64Decode(str);
            var half = parseInt(bytes.length / 2);

            for (var i=0; i<half; i++) {
                if (i % 2 === 1) {
                    var b = bytes[i];
                    bytes[i] = bytes[i + half];
                    bytes[i + half] = b;
                }
            }

            for (var i=0; i<bytes.length; i++) {
                bytes[i] = ~bytes[i];
            }

            return this.toUtf16(bytes);
        },

        // 仅限内部调用
        init: function(from) {
            if (!Const.inited) {
                Const.inited = true;
                Const.from = from;
                Const.init = this.mix(Const.init, __Const.init, true);
                Const.url = this.mix(Const.url, __Const.url, true);
                Const.rest = this.mix(Const.rest, __Const.rest);
                Const.portal = this.mix(Const.portal, __Const.portal);
                Const.menu = this.mix(Const.menu, __Const.menu);
                Const.dicts = this.mix(Const.dicts, __Const.dicts);
                // Const.entities = this.mix(Const.entities, __Const.entities);
                Const.rule = this.mix(Const.rule, __Const.rule);
                Const.width = this.mix(Const.width, __Const.width);
                var util = this;
                $.ajaxSetup ({
                    cache: false
                });
                toastr.options = {
                    closeButton: true,
                    // timeOut: 0,
                    /// preventDuplicates: true,
                    single: true,
                    closeOnHover: false,
                    positionClass: 'toast-' + Const.init.show.position
                };
                $(document).ajaxError(function(event, xhr, options, exc) {
                    var message = xhr.responseJSON && xhr.responseJSON.appId ? xhr.responseJSON.message : '';
                    switch (xhr.status) {
                        case 500:
                            util.show({
                                message: message || '服务器系统内部异常',
                                level: 'error'
                            });
                            break;
                        case 401:
                            if (Const.init.login) {
                                if (!from || from !== 'login') {
                                    util.login();
                                }
                            } else {
                                if ((!from || from !== 'login') && !Const.alerted && Const.init.type !== 'alert') {
                                    Const.alerted = true;
                                    util.show({
                                        message: '登录超时，请重新登录',
                                        level: 'error'
                                    });
                                }
                                // alert('登录超时，请重新登录');
                            }
                            break;
                        case 403:
                            if (Const.init.login) {
                                if (!from || from !== 'login') {
                                    util.login();
                                    // util.show(message || '登陆超时，请重新登录', undefined, 'alert', 'error');
                                }
                            } else {
                                if ((!from || from !== 'login') && !Const.alerted) {
                                    Const.alerted = true;
                                    util.show({
                                        message: '登录超时，请重新登录',
                                        level: 'error'
                                    });
                                }
                                // alert('登录超时，请重新登录');
                            }
                            break;
                        case 404:
                            util.show({
                                message: message || '找不到资源',
                                level: 'error'
                            });
                            break;
                        case 405:
                            util.show({
                                message: message || '不支持该调用方法',
                                level: 'error'
                            });
                            break;
                        case 408:
                            util.show({
                                message: message || '请求超时',
                                level: 'error'
                            });
                            break;
                        case 200:
                            Const.alerted = false;
                            break;
                        default:
                            util.show({
                                message: message || '未知错误',
                                level: 'error'
                            });
                    }
                });

                if (Const.init.config) {
                    util.initConfig();
                }
            }
        }
    }
});
