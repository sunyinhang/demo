define('avalon', ['Promise', 'avalonOrigin', 'jquery', 'util', 'Const'], function(Promise, avalon, $, util, Const) {
    if (typeof window.Promise !== 'function') {
        window.Promise = Promise;
    }
    // 文件大小格式化过滤器
    avalon.filters.file = function(v) {
        return avalon.filters.number(v, 0);
    }
    avalon.filters.card3 = function(v) {
        return v && v.length >= 3 && v.substr(0, 3);
    }
    avalon.filters.card4 = function(v) {
        return v && v.length >= 4 && v.substr(-4, 4);
    }
    avalon.filters.phone4 = function(v) {
        return v && v.length == 11 && (v.substring(0, 3) + '****' + v.substring(7, 11));
    }
    var mobileReg = /^(1[0-9])\d{9}$/;
    avalon.validators.mobile = {
        message: '请输入正确的手机号码',
        get: function (value, field, next) {
            //console.log(value, field,next);
            //var ok =regPassword.test(value)
            var ok = (mobileReg.exec(value) != null)
            next(ok)
            return value
        }
    }
    var regPassword=/^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,20}$/;
    avalon.validators.password = {
        message: '登录密码需以6-20位字母和数字组成',
        get: function (value, field, next) {
            //console.log(value, field,next);
            //var ok =regPassword.test(value)
            var ok=(regPassword.exec(value)!=null)
            next(ok)
            return value
        }
    }
    var payPassword=/^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,20}$/;
    avalon.validators.payPassword = {
        message: '支付密码需以6-20位字母和数字组成',
        get: function (value, field, next) {
            //console.log(value, field,next);
            //var ok =regPassword.test(value)
            var ok=(payPassword.exec(value)!=null)
            next(ok)
            return value
        }
    }
    var regIdcard=/(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/;
    avalon.validators.idCheck = {
        message: '请输入正确的证件号码',
        get: function (value, field, next) {
            var ok = (regIdcard.exec(value)!=null)
            next(ok)
            return value
        }
    }
    //金额校验
    avalon.validators.money = {
        message: '请输入正确的金额。',
        get: function (value, field, next) {
            var len = 15;
            if (field.data && field.data.money && field.data.money !== true) {
                len = field.data.money - 1;
            }
            var regx = new RegExp('^([1-9]\\d{0,' + len + '})+(\.[0-9]{2})?$');
            //var regx = new RegExp('^[0-9]{0,'+len+'}+(\.[0-9]{1,2})?$');
            
            // var moneyReg = /^(([1-9]\d{0,5})|(0))(\.\d{1,2})?$/;
            //console.log(value, field,next);  && value <= 200000
            var ok = regx.test(value);
            next(ok);
            return value;
        }
    }
    //金额校验结束
    var regUnitTele=/^\d{10,13}$/;
    avalon.validators.phone = {
        message: '请输入正确的联系电话',
        get: function (value, field, next) {
            var ok=(regUnitTele.exec(value)!=null)
            next(ok)
            return value
        }
    }
    var bankCardNumber=/^([0-9]{16}|[0-9]{19})$/;
    avalon.validators.bankCard = {
        message: '请输入16位或19位银行卡号码',
        get: function (value, field, next) {
            //console.log(value, field,next);
            //var ok =regPassword.test(value)
            var ok=(bankCardNumber.exec(value)!=null);
            next(ok);
            return value;
        }
    }

    avalon.validators.checkAgree = {
        message: '请同意个人信息使用授权书',
        get: function (value, field, next) {
            var ok = value;
            next(ok);
            return value;
        }
    }

    // 重定义avalon，避免页面重新加载造成avalon重复定义
    avalon.redefine = function (param) {
        var $element = $('[ms-controller="' + param.$id + '"]');
        // TODO: 仅处理ms-text,ms-duplex,ms-html几种绑定，最多支持三级@attr.attr.attr
        $('[ms-text], [ms-html], [ms-duplex]', $element).each(function() {
            var bind = $(this).attr('ms-text') || $(this).attr('ms-html') || $(this).attr('ms-duplex');
            if (bind) {
                bind = bind.replace(/[-+,' ]/g, '');
                if (util.startsWith(bind, '@')) {
                    bind = bind.substring(1);
                }
                var arr = bind.split('@');
                for (var i=0; i<arr.length; i++) {
                    util.initVm(param, undefined, arr[i]);
                }
            }
        });
        // avalon.vmodels[param.$id] = null;
        if (param.$id) {
            util.clearVmCache(param.$id);
        } else {
            param.$id = new Date().getMilliseconds();
        }
        if (param.$tags) {
             for (var name in param.$tags) {
                 util.clearVmCache(param.$tags[name]);
             }
         }
        var vm = avalon.define(param);
        avalon.scan($element[0]);
        $('.ms-controller', $(vm.$element)).removeClass('ms-controller');
        return vm;
    }
    return avalon;
});
