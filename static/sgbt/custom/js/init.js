require(['vue', 'jquery', 'util', 'Const', 'bridge', 'weui', 'hammer', 'validation'], function(vue, $, util, Const, bridge) {
    if ($('.bv-main').length > 1) {
        util.redirect({
            title: '404',
            url: '/404.html',
            back: false
        });
        return;
    }
    util.init();

    var path = util.path();
    if (util.isEmpty(path) || path === '/') {
        path = '/applyQuota/checkIdCard.html';
    }
    window.addEventListener("popstate", function() {
        if (!history.state || history.state.back === undefined) {
            var path = util.path();
            if (util.isEmpty(path) || path === '/') {
                path = '/home.html';
            }
            util.redirect({
                // location.href, '', undefined, true
                // title: '',
                url: path,
                ignore: true
            });
        } else if (util.type(history.state.back) === 'string') {
            util.redirect({
                // history.state.back, '', false, true
                title: history.state.title,
                url: history.state.back,
                back: false,
                ignore: true
            });
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
        util.redirect({
            title: $(this).attr('data-title'),
            url: url,
            $target: $(this).closest(target),
            back: $(this).attr('data-redirect-back')
        });
    });
    $(document).on('click', '[data-href]', function() {
        window.location.href = $(this).attr('data-href');
    });
    $(document).on('click', '[data-prompt]', function() {
        util.alert($(this).attr('data-prompt'));
    });

    util.redirect({
        title: '实名绑卡',
        url: path,
        back: false
    });
});