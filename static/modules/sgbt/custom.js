require(['avalon', 'jquery', 'util', 'Const', 'layer'], function(avalon, $, util, Const) {
    util.init();    
    var _current = util.gup('#');
    if (util.isEmpty(_current)) {
        _current = 'applyQuota/checkIdCard.html';
    }
    window.addEventListener("popstate", function() {
        layer.closeAll();

        if (!history.state || history.state.back === undefined) {
            if (location.hash) {
                util.redirect(location.hash, '', undefined, true);
            } else {
                util.redirect(location.href, '', undefined, true);
            }
        } else if (avalon.type(history.state.back) === 'string') {
            util.redirect(history.state.back, '', false, true);
        } else if (history.state.back === false) {
            history.pushState({}, '', location.hash);
        }
    });

    $(document).on('click', '[data-redirect]', function() {
        // document.title = $(this).attr('data-title');
        var target = $(this).attr('data-target');
        if (!target) {
            target = '#mainDiv';
        }
        var params = $(this).attr('data-param');
        var url = $(this).attr('data-redirect');
        if (params) {
            var paramArr = params.split(',');
            var p = '';
            if (util.contains(url, '?')) {
                p = '&';
            } else {
                p = '?';
            }
            for (var i=0; i<paramArr.length; i++) {
                if (p.length > 1) {
                    p += '&';
                }
                p += paramArr[i] + '=' + util.gup(paramArr[i]);
            }
            url += p;
        }
        util.redirect(url, $(this).closest(target), $(this).attr('data-redirect-back'));
    });
    $(document).on('click', '[data-href]', function() {
        window.location.href = $(this).attr('data-href');
    });
    $(document).on('click', '[data-prompt]', function() {
        util.alert($(this).attr('data-prompt'));
    });

    util.redirect(_current, undefined, false);
});