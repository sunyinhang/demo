define('util', ['avalonOrigin', 'jquery', 'Const', 'layer'], function(avalon, $, Const, layer) {
    return {
        gup: function(name, url) {
            if (!url) url = location.href;
            if (name && name === '#') {
                if (url.indexOf('#') >= 0) {
                    return url.substring(url.indexOf('#') + 1);
                }
                return '';
            }
            name = name.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");
            var regexS = "[\\?&]" + name + "=([^&#]*)";
            var regex = new RegExp( regexS );
            var results = regex.exec( url );
            return results === null ? null : results[1];
        },
        cache: function(param, value) {
            if (!this.isEmpty(value)) {
                $global[param] = value;
            } else if (avalon.type(param) === 'string') {
                return $global[param];
            } else if (avalon.type(param) === 'object') {
                $global.param = this.clone(param);
            } else if (avalon.type(param) === 'undefined') {
                if (!$global.param) {
                    $global.param = {};
                    if (this.contains(location.href, '?')) {
                        var paramUrl = location.href.substring(location.href.indexOf('?') + 1);
                        var arr = paramUrl.split('&');
                        for (var i=0; i<arr.length; i++) {
                            if (this.contains(arr[i], '=')) {
                                var keyValue = arr[i].split('=');
                                $global.param[keyValue[0]] = keyValue[1];
                            }
                        }
                    }
                }
                return $global.param;
            }
        },
        cacheServer: function(data) {
            var result;
            if (data) {
                if (avalon.type(data) === 'string') {
                    // get
                    this.post({
                        url: 'cacheServlet.do',
                        data: {
                            params: data
                        },
                        async: false,
                        success: function(res) {
                            result = res.retObj;
                        }
                    });
                } else if (avalon.type(data) === 'object') {
                    // set
                    if (!data.params) {
                        var params = '';
                        for (var p in data) {
                            if (p !== 'type') {
                                if (params) {
                                    params += ',';
                                }
                                params += p;
                            }
                        }
                        data.params = params;
                    }
                    if (!data.type) {
                        data.type = 'set';
                    }
                    this.post({
                        url: 'cacheServlet.do',
                        data: data,
                        async: false,
                        success: function(res) {
                            result = res.retObj;
                        }
                    });
                }
            }
            if (!result) {
                result = {};
            }
            return result;
        },
        token: function(token) {
            if (token) {
                $global.token = token;
            } else {
                if (!$global.token) {
                    $global.token = this.gup('token') || '';
                }
                return $global.token || '';
            }
        },
        moxie: function(moxie) {
            if (moxie) {
                $global.moxie = moxie;
            } else {
                if (!$global.moxie) {
                    $global.moxie = {
                        flag: this.gup('flag') || ''
                    };
                }
                return $global.moxie;
            }
        },
        clone: function(value) {
            var type = avalon.type(value);
            if (type === 'undefined') {
                return;
            } else if (type === 'object') {
                return avalon.mix({}, value);
            } else if (type === 'array') {
                return avalon.slice(value);
            } else {
                return value;
            }
        },
        url: function(url, type, ext) {
            if (!url) {
                return url;
            }
            if (type && type === 'pure') {
                if (this.contains(url, '?')) {
                    url = url.substring(0, url.indexOf('?'));
                }
                if (this.contains(url, '#')) {
                    url = url.substring(0, url.indexOf('#'));
                }
                if (ext) {
                    url += ext;

                    if (!this.gup(url, 'token') && this.token()) {
                        url = this.mix(url, {
                            token: this.token()
                        })
                    }
                }
                return url;
            }
            if ((type && (type === 'html' || type === 'json')) || this.startsWith(url, 'http')) {
                return url;
            }
            if (type === '#') {
                var _current = window.location.href;
                if (this.contains(_current, '#')) {
                    return _current.substring(0, _current.indexOf('#') + 1) + url + '?token=' + this.token();
                }
            }
            var pureUrl = url;
            if (this.contains(url, '?')) {
                pureUrl = url.substring(0, url.indexOf('?'));
            }
            if (pureUrl && this.endsWith(pureUrl, '.html') || this.endsWith(pureUrl, '.json')) {
                return url;
            }
            var resultUrl = Const.rest.baseUrl + url;
            if (type && type === 'timestamp') {
                if (resultUrl.indexOf('?') < 0) {
                    resultUrl += '?_=' + new Date().getTime();
                } else {
                    resultUrl += '&_=' + new Date().getTime();
                }
            }
            return resultUrl;
        },
        mix: function(url, param) {
            if (!url || !param) {
                return url;
            }
            if (avalon.type(url) === 'object') {
                return avalon.mix({}, url, param);
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
        json: function(data) {
            if (avalon.type(data) === 'string') {
                if (!data) {
                    return {};
                }
                return JSON.parse(data);
            }
            return JSON.stringify(data);
        },
        get: function(param) {
            param.type = 'get';
            this.ajax(param);
        },
        // ajax.post调用
        post: function(param) {
            param.type = 'post';
            param.dataType = 'json';
            /// param.contentType = 'application/json';
            this.ajax(param);
        },
        upload: function(param) {
            param.type = 'post';
            /// param.async = false;
            param.cache = false;
            param.contentType = false;
            param.processData = false;
            this.ajax(param);
        },
        ajax: function(param) {
            this.loading();
            if (!param.url) {
                return;
            }
            if (!param.token && this.token()) {
                param.token = this.token();
            }
            if (!param.token) {
                param.token = this.gup('token');
                if (param.token) {
                    this.token(param.token);
                }
            }
            if (param.token && !this.gup('token', param.url)) {
                param.url = this.mix(param.url, {
                    token: param.token
                });
                delete param.token;
            }
            param.url = this.url(param.url, param.urlType);
            /////// 可以增加代码处理点击事件，防止重复点击 /////////////////
            if (param.success) {
                param.$success = param.success;
            }
            param.success = function(data) {
                var isSuccess = false;
                if (!param.check) {
                    isSuccess = (data && data.code === '0000');
                } else if (param.check === true) {
                    isSuccess = true;
                } else if (avalon.type(param.check) === 'function') {
                    isSuccess = param.check.call(null, data);
                }
            	// 增加自己的处理
                if (isSuccess) {
                	if (param.$success && avalon.type(param.$success) === 'function') {
                        param.$success.call(null, data);
                    }
    	 		} else {
    	 			if (param.$error && avalon.type(param.$error) === 'function') {
    	 				param.$error.call(null, data);
    	 			}
                    layer.open({
                        content: data.message,
                        btn: '我知道了'
                    });
    	 		}
            }
            if (param.error) {
                param.$error = param.error;
            }
            if (param.$error !== 'ignore') {
                var util = this;
                param.error = function(xhr, status, e) {
                    // TODO: 记录网络异常到后台
                    /*util.cacheServer({
                        xhr: xhr.status,
                        status: status,
                        e: e,
                        error: 'ignore'
                    });*/
                    if (param.$error && avalon.type(param.$error) === 'function') {
                        param.$error.call(null, xhr, status, e);
                    }
                    layer.open({
                        content: Const.message.ajaxError,
                        btn: '我知道了'
                    });
                }
            }
            if (param.complete) {
                param.$complete = param.complete;
            }
            var util = this;
            param.complete = function() {
                if (param.$complete && avalon.type(param.$complete) === 'function') {
                    param.$complete.call(null);
                }
                util.loading('close');
            }
            $.ajax(param);
        },
        replace: function($element, url) {
            this.empty();
    //            this.scroll($element);
            this.loading();
            url = this.url(url, 'html');
            var util = this;
            var pn=window.location.pathname;// 路径：/PaymentPlatform/page/webapp/a/
            //百度虚拟统计
            var tmurl=url.substring(5);
        	_hmt.push(['_trackPageview', pn+'#'+tmurl]); 
        	//百度虚拟统计结束
            $element.load(url, function() {
                util.loading('close');
            });
            $element.attr('data-url', url);
        },
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
        toNumber: function(v) {
            return parseInt(v, 10);
        },
        toFloat: function(v) {
            return parseFloat(v);
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
        toBoolean: function(v) {
            if (!v || v === 'false') {
                return false;
            }
            return true;
        },
        heredoc: function(fn) {
            return fn.toString().replace(/^[^\/]+\/\*!?\s?/, '').replace(/\*\/[^\/]+$/, '').trim().replace(/>\s*</g, '><');
        },
        //得到图片路径
        getFileName: function(filePath) {
            if (filePath) {
                if (this.contains(filePath, '\\')) {
                    return filePath.substring(filePath.lastIndexOf('\\') + 1);
                } else if (this.contains(filePath, '/')) {
                    return filePath.substring(filePath.lastIndexOf('/') + 1);
                }
                return filePath;
            }
        },
        getImgUrl: function(fileId) {
            var files = document.getElementById(fileId).files;
            if (files && files.length > 0) {
                return window.URL.createObjectURL(files[0]);
            }
        },
        showLocalImage: function(fileId) {
            var imgSrc = this.getImgUrl(fileId);

            if (this.isEmpty(imgSrc)) {
                return false;
            } else{
                var imgPath = this.getFileName($('#' + fileId).val());//当前input[file]的图片名称
                $('#' + fileId).parent().siblings(".path").children("input").val(imgPath);
                $('#' + fileId).parent().siblings(".camera-img").children("img").attr("src", imgSrc);
                return true;
            }
        },
        //判断图片大小
        checkFileSize: function(containerId,size) {
            var fileSizeTemp = true;
            var util = this;
            $("#" + containerId).find('input[type="file"]').each(function() {
                if (util.isEmpty($(this).val())) {
                    fileSizeTemp = false;
                } else {
                    var fileName = util.getFileName($(this).val());//当前input的图片名称
                    var fileSize = this.files[0].size;
                    var sizeTemp = size ==undefined?'20':size;
                    var sizeFile = fileSize / 1024;
                    if (sizeFile > sizeTemp*1024) {
                        layer.open({
                            content: '上传图片不能大于'+sizeTemp+'MB',
                            btn: '我知道了',
                        });
                        fileSizeTemp = false;
                    }
                }
            });
            return fileSizeTemp;
        },
        /*卡号判断卡类型*/
        getBankName: function() {
        	var bankCard = $("#bankNumber").val();
            var cardNumReg = /^([0-9]{16}|[0-9]{19})$/;//卡号正则
            if (cardNumReg.exec(bankCard) == null) {
                layer.open({
                    content: '请输入16位或19位正确的卡号',
                    btn: '我知道了',
                    yes: function(index, layero) {
                        //do something
                        layer.close(index); //如果设定了yes回调，需进行手工关闭
                        $("#bankNumber").focus();
                    }
                });
            } else {
                this.post({
                    url: 'getCardInfoServlet.do',
                    data: {
                        cardNo: bankCard
                    },
                    success: function(res) {
                        $('#cardType').val(res.retObj.bankName + ' ' + res.retObj.cardType);
                    }
                });
            }
        },

    	/*百度统计结束*/
    	baiduStatistics: function(){
        	var _hmt = _hmt || [];
        	var hm = document.createElement("script");
        	hm.src = "https://hm.baidu.com/hm.js?7e18821d82be100f1703d479460cdb79";
        	var s = document.getElementsByTagName("script")[0]; 
        	s.parentNode.insertBefore(hm, s);
    	},
        loading: function(type) {
            if (type && type === 'close') {
                if (!$global.loading || $global.loading === 1) {
                    $global.loading = 0;
                    layer.close($global.loadingIndex);
                } else {
                    $global.loading--;
                }
            } else {
                if (!$global.loading) {
                    $global.loading = 1;

                    //加载中
                    $global.loadingIndex = 	layer.open({
                        content: '',
                        shadeClose: false,
                        shade: 'background-color: rgba(255,255,255,.3)', //自定义遮罩的透明度 
                        style: 'border:0px;width:4rem;height:4rem;background:url(../mobile/themes/default/imgsb/loading.gif) center center no-repeat rgba(255, 255, 255, 0.8); background-size:1.5rem'
                    });
                } else {
                    $global.loading++;
                }
            }
        },
        active: function(id) {
            $('#top li.li-img.active').removeClass('active');
            $('#top li#' + id).addClass('active');
        },
        checkFaceStatus: function(url, form) {
            var util = this;
            this.post({
                url: url,
                data: {
                    backUrl: this.url('frontPhotograph.html', '#')
                },
                success: function(res) {
                	if (res.retObj.payPasswdFlag === '0') {
                        // 未设置支付密码,则跳转设置支付密码界面  //数据授权
                        util.cacheServer({
                            payPasswdFlag: '0'
                        });
                        util.redirect('payPassword.html');
                    } else if (res.retObj.payPasswdFlag === '1') {
                        // 已经设置支付密码，则跳转短信验证页面  //数据授权
                        util.cacheServer({
                            payPasswdFlag: '1'
                        });
                        // util.redirect('confirmPayPwd.html');
                        util.redirect('validationCode.html');
                    } else if (res.retObj.faceFlag === '0') {
                        // 未设置支付密码,则跳转设置支付密码界面 //个人资料
                        util.cacheServer({
                            payPasswdFlag: '0'
                        });
                        util.redirect('payPassword.html');
                    } else if (res.retObj.faceFlag === '1') {
                        // 已经设置支付密码，则跳转短信验证页面  //个人资料
                        util.cacheServer({
                            payPasswdFlag: '1'
                        });
                        // util.redirect('confirmPayPwd.html');
                        util.redirect('validationCode.html');
                    } else if (res.retObj.faceFlag === '2') {
                        // 跳转手持身份证
                        util.redirect('handholdIdCard.html');
                    } else if (res.retObj.faceFlag === '3') {
                        // 可以做人脸
                        $('#token').val(res.retObj.face_token);
                        util.redirect('identityVrfic.html');
                        //form.submit();
                    } else if (res.retObj.faceFlag === '4') {
                        // 跳转完善信息
                        util.redirect('personalInformation.html');
                    }
                }
            });
        },
        //验证码倒计时
        countDown: function(url, param, second, $element) {
            //按钮时间
            var buttonDefaultValue = "获取验证码";
            if (second === 60) {
                this.post({
                    url: url,
                    data: param
                });
            }
            //如果秒数还是大于0，则表示倒计时还没结束
            if (second >= 0) {
                // 按钮置为不可点击状态
                $element.attr("disabled",true);
                // $("#getCheckCode").removeClass("senMessBtn");
                $element.addClass("seding");
                // 按钮里的内容呈现倒计时状态
                $element.val(second + 's后重新获取');
                // 时间减一
                second--;
                // 一秒后重复执行
                var util = this;
                setTimeout(function() {
                    util.countDown(url, param, second, $element);
                }, 1000);
                //否则，按钮重置为初始状态
            } else {
                // 按钮置为可点击状态
                //obj.disabled = false;
                $element.attr("disabled", false);
                // 按钮里的内容恢复初始状态
                //obj.value = buttonDefaultValue;
                $element.val(buttonDefaultValue);
                $element.removeClass("seding");
                $element.addClass("senMessBtn");
            }
        },
        message: function(key) {
            return Const.messages[key];
        },
        // 参数格式
        // string:错误提示
        // object:
        //    element
        //    自定义
        alert: function(message, focus, btn) {
            if (message) {
                if (avalon.type(message) === 'string') {
                    if (message === 'close') {
                        if (avalon.type(focus) !== 'undefined') {
                            layer.close(focus);
                        }
                        return;
                    }
                    if (this.startsWith(message, '#')) {
                        message = Const.messages[message.substring(1)];
                    }
                    if (focus) {
                        if (avalon.type(focus) === 'string') {
                            if (focus === 'html') {
                                if (btn === false) {
                                    layer.open({
                                        type: 1,
                                        content: message,
                                        anim: 'up',
                                        style: 'position:fixed; left:0; top:0; width:100%; height:100%; border: none; -webkit-animation-duration: .5s; animation-duration: .5s; overflow-y: auto;',
                                        yes: function(index, layero) {
                                            layer.close(index);
                                        }
                                    });
                                } else {
                                    layer.open({
                                        type: 1,
                                        content: message,
                                        anim: 'up',
                                        btn: btn || '我知道了',
                                        style: 'position:fixed; left:0; top:0; width:100%; height:100%; border: none; -webkit-animation-duration: .5s; animation-duration: .5s; overflow-y: auto;',
                                        yes: function(index, layero) {
                                            layer.close(index);
                                        }
                                    });
                                }
                            } else {
                                var util = this;
                                layer.open({
                                    content: message,
                                    btn: '我知道了',
                                    yes: function(index, layero) {
                                        layer.close(index);
                                        util.redirect(focus);
                                    }
                                })
                            }
                        } else if (avalon.type(focus) === 'object') {
                            layer.open({
                                content: message,
                                btn: '我知道了',
                                yes: function(index, layero) {
                                    layer.close(index);
                                    focus.focus();
                                }
                            })
                        }
                    } else {
                        layer.open({
                            content: message,
                            btn: '我知道了'
                        })
                    }
                } else if (avalon.type(message) === 'object') {
                    layer.open({
                        content: message.getMessage(),
                        btn: '我知道了',
                        yes: function(index, layero) {
                            layer.close(index);
                            if (avalon.type(focus) === 'undefined' || focus === true) {
                                $(message.element).focus();
                            }
                        }
                    });
                }
            }
        },
        confirm: function(message, btn, yes, no) {
            if (message) {
                if (this.startsWith(message, '#')) {
                    message = Const.messages[message.substring(1)];
                }
                if (avalon.type(btn) === 'array') {
                    // 按钮
                    layer.open({
                        content: message,
                        btn: btn,
                        shadeClose: false,
                        yes: yes,
                        no: no
                    });
                } else if (avalon.type(btn) === 'function') {
                    // yes
                    layer.open({
                        content: message,
                        btn: ['确定', '取消'],
                        shadeClose: false,
                        yes: btn,
                        no: yes
                    });
                }
            }
        },
        options: function(name, excludes, includes) {
            if (avalon.type(name) === 'string') {
                var optionMap = Const.dicts[name];
                if (optionMap && avalon.type(optionMap) === 'object') {
                    if (this.isEmpty(excludes)) {
                        excludes = [];
                    } else if (avalon.type(excludes) === 'string') {
                        excludes = [excludes];
                    }
                    if (this.isEmpty(includes)) {
                        includes = [];
                    } else if (avalon.type(includes) === 'string') {
                        includes = [includes];
                    }

                    var result = [];
                    for (var p in optionMap) {
                        if ((this.isEmpty(excludes) || excludes.indexOf(p) < 0) && (this.isEmpty(includes) || includes.indexOf(p) >= 0)) {
                            result.push({
                                code: p,
                                desc: optionMap[p]
                            });
                        }
                    }
                    result.sort(function(a, b) {
                        return a.code > b.code;
                    });
                    return result;
                }
            }
        },

    //////////////////////////////// 接口数据加密 ///////////////////////////////////////
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
        base64Encode: function(bytes) {
            if (!bytes || bytes.length == 0) {
                return '';
            }
            var base64Table = [
                'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
                'I', 'J', 'K', 'L', 'M', 'N', 'O' ,'P',
                'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
                'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
                'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
                'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
                'w', 'x', 'y', 'z', '0', '1', '2', '3',
                '4', '5', '6', '7', '8', '9', '+', '/'
            ];
            var i = 0; // 遍历索引
            var len = bytes.length;
            var res = [];
            while (i < len) {
                var c1 = bytes[i++] & 0xFF;
                res.push(base64Table[c1 >> 2]);
                // 需要补2个=
                if (i == len) {
                    res.push(base64Table[(c1 & 0x3) << 4]);
                    res.push('==');
                    break;
                }
                var c2 = bytes[i++];
                // 需要补1个=
                if (i == len) {
                    res.push(base64Table[((c1 & 0x3) << 4) | ((c2 >> 4) & 0x0F)]);
                    res.push(base64Table[(c2 & 0x0F) << 2]);
                    res.push('=');
                    break;
                }
                var c3 = bytes[i++];
                res.push(base64Table[((c1 & 0x3) << 4) | ((c2 >> 4) & 0x0F)]);
                res.push(base64Table[((c2 & 0x0F) << 2) | ((c3 & 0xC0) >> 6)]);
                res.push(base64Table[c3 & 0x3F]);
            }

            return res.join('');
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
            var chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=';
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
                buffer = chars.indexOf(buffer);
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
        //////////////////////////////// 接口数据加密 ///////////////////////////////////////

        redirect: function(url, $target, back, ignore) {
            if (this.contains(url, '#')) {
                url = url.substring(url.indexOf('#') + 1);
            }
            if (url.indexOf('?') > 0) {
                document.title = Const.titles[url.substring(0, url.indexOf('?'))];
            } else {
                document.title = Const.titles[url];
            }
            if (!this.gup('t', url)) {
                url = this.mix(url, {
                    t: new Date().getTime()
                });
            }
            if (!$target) {
                $target = $('#mainDiv');
            }
            if (this.startsWith(url, 'http')) {
                this.replace($target, url);
                return;
            }
            this.replace($target, __baseHtmlPath + url);
            if (!this.gup('token', url) && this.token()) {
                url = this.mix(url, {
                    token: this.token()
                });
            }
            if (ignore) {
                if (back === false) {
                    history.pushState({back: false}, '', '#' + url);
                }
                history.replaceState({}, '', '#' + url);
            } else if (back === undefined || back === true || back === 'true') {
                history.pushState({}, '', this.url(location.href, 'pure') + '#' + url);
            } else if (back === false || back === 'false') {
                history.pushState({back: false}, '', '#' + url);
                history.pushState({}, '', '#' + url);
            } else {
                if (!this.gup('token', back) && this.token()) {
                    back = this.mix(back, {
                        token: this.token()
                    });
                }
                history.pushState({back: '#' + back}, '', '#' + back);
                history.pushState({}, '', '#' + url);
            }
            /*if (title) {

            } else {
                window.location.href = url;
            }*/
        },
        location: function(url) {
            window.location.href = url;
        },
        vm: function($id, expr) {
            if (!expr) {
                if (avalon.type($id) === 'string') {
                    return avalon.vmodels[$id];
                }
                return $id;
            }
            // 根据组件vm查询父级vm，监听变量用
            var toppath = expr.split(".")[0];
            var other;
            try {
                if ($id.hasOwnProperty(toppath)) {
                    if ($id.$accessors) {
                        other = $id.$accessors[toppath].get.heirloom.__vmodel__
                    } else {
                        other = Object.getOwnPropertyDescriptor($id, toppath).get.heirloom.__vmodel__
                    }
                }
            } catch (e) {
            }
            return other || $id;
///            return root.$define[$id];
        },
        clearVmCache: function($id) {
            if ($id) {
                if (avalon.type($id) === 'array') {
                    for (var i=0; i<$id.length; i++) {
                        this.clearVmCache($id[i]);
                    }
                } else if (avalon.type($id) === 'string') {
                    if (avalon.scopes && avalon.scopes[$id]) {
                        delete avalon.scopes[$id].vmodel;
                        // delete avalon.scopes[$id];
                    }
                    if (avalon.vmodels[$id]) {
                        delete avalon.vmodels[$id];
                    }
                }
            }
        },
        initVm: function(vm, param, expr) {
            if (expr) {
                if (this.contains(expr, '.')) {
                    var exprArr = expr.split('.');
                    if (avalon.type(vm[exprArr[0]]) === 'undefined') {
                        if (this.endsWith(exprArr[0], '[0]')) {
                            vm[exprArr[0]] = {};
                        } else {
                            vm[exprArr[0]] = {};
                        }
                    }
                    if (exprArr.length === 2) {
                        if (avalon.type(vm[exprArr[0]][exprArr[1]]) === 'undefined') {
                            vm[exprArr[0]][exprArr[1]] = null;
                        }
                    } else if (exprArr.length === 3) {
                        if (this.endsWith(exprArr[1], '[0]')) {
                            if (avalon.type(vm[exprArr[0]][exprArr[1].substring(0, exprArr[1].length - 3)]) === 'undefined') {
                                vm[exprArr[0]][exprArr[1].substring(0, exprArr[1].length - 3)] = [{}];
                            }
                            if (avalon.type(vm[exprArr[0]][exprArr[1].substring(0, exprArr[1].length - 3)][0][exprArr[2]]) === 'undefined') {
                                vm[exprArr[0]][exprArr[1].substring(0, exprArr[1].length - 3)][0][exprArr[2]] = null;
                            }
                        } else {
                            if (avalon.type(vm[exprArr[0]][exprArr[1]]) === 'undefined') {
                                vm[exprArr[0]][exprArr[1]] = {};
                            }
                            if (avalon.type(vm[exprArr[0]][exprArr[1]][exprArr[2]]) === 'undefined') {
                                vm[exprArr[0]][exprArr[1]][exprArr[2]] = null;
                            }
                        }
                    }
                } else {
                    if (avalon.type(vm[expr]) === 'undefined') {
                        vm[expr] = null;
                    }
                }
            }/* else {
             for (var key in param) {
             if (avalon.type(param[key]) === 'object') {
             this.initVm(vm, param[key]);
             }
             }
             }*/
        },
        date: function(pattern, date, period) {
            if (!period) {
                if (!pattern) {
                    pattern = 'yyyy-MM-dd';
                }
                if (!date) {
                    date = new Date();
                }
                return avalon.filters.date(date, pattern);
            }
            if (!pattern || pattern === 'date' || pattern === 'yyyy-mm-dd' || pattern === 'yyyy-MM-dd') {
                pattern = 'YYYY-MM-DD';
            } else if (pattern === 'datetime' || pattern === 'yyyy-MM-dd hh:mm:ss') {
                pattern = 'YYYY-MM-DD HH:mm:ss';
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
            return avalon.filters.date(date, pattern);
        },
        // 判断是否为空，可判断空数组，空json
        isEmpty: function(v) {
            var type = avalon.type(v);
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
        contains: function(v, c) {
            if (avalon.type(v) === 'array') {
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
        // 仅限内部调用
        init: function() {
            if (!Const.inited) {
                Const.inited = true;
                Const.rest = avalon.mix({}, Const.rest, __Const.rest);
                Const.params = avalon.mix({}, Const.params, __Const.params);
                Const.dicts = avalon.mix({}, Const.dicts, __Const.dicts);
                Const.titles = avalon.mix({}, Const.titles, __Const.titles);
                Const.messages = avalon.mix({}, Const.messages, __Const.messages);
                Const.lengths = avalon.mix({}, Const.lengths, __Const.lengths);
                Const.templates = avalon.mix({}, Const.templates, __Const.templates);
            }
        }
    }
});
