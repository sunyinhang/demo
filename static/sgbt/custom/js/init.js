require(['vue', 'jquery', 'util', 'Const', 'bridge', 'framework', 'validation', 'bvLayout'], function(vue, $, util, Const, bridge) {
    util.init();

    Const.global.f = new Framework7({
        router: false,
        modalTitle: '',
        modalButtonOk: '确定',
        modalButtonCancel: '取消',
        modalTemplate: util.heredoc(function() {
            /*!
            <div class="modal {{cssClass}} {{#unless buttons}}modal-no-buttons{{/unless}}">
                <div class="modal-inner">
                {{#if title}}
                    <div class="modal-title">{{title}}</div>
                {{/if}}
                {{#if text}}
                    <div class="modal-text">{{text}}</div>
                {{/if}}
                {{#if afterText}}
                    {{afterText}}
                {{/if}}
                </div>
                {{#if buttons}}
                    <div class="modal-buttons {{#if verticalButtons}}modal-buttons-vertical{{/if}}">
                {{#each buttons}}
                    <span class="modal-button {{#if cssClass}}modal-button-{{cssClass}}{{/if}}">{{text}}</span>
                {{/each}}
                </div>
                {{/if}}
            </div>
            */
        })
        /*pushState: true,
        onAjaxStart: function (xhr) {
            Const.global.f.showIndicator();
        },
        onAjaxComplete: function (xhr) {
            Const.global.f.hideIndicator();
        }*/
    });
    util.loading();
    $('.bv-preloader-container').remove();
    $('.bv-overlay').remove();
    $('.agree-popup').removeClass('hide');
    $('body').height($(window).height());
    // #!/

    /*if ($('#mainDiv').length > 1) {
        util.redirect({
            title: '404',
            url: '/404.html',
            back: false
        });
        return;
    }*/
    window.addEventListener("popstate", function() {
        if (!history.state || history.state.back === undefined) {
            var path = util.path();
            if (util.isEmpty(path) || path === '/' || path === '/index.html') {
                path = '/applyQuota/checkIdCard.html';
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
    $(document).on('click', 'a[href]', function (event) {
        event.preventDefault();
    });
    $('.agree-popup div#content').height($(window).height() - 45);
    $(document).on('open', '.agree-popup', function () {
        util.loading();
        $('#title span', this).text( $(this).attr('data-title'));
        $('iframe', this).attr('src', $(this).attr('data-url'));
    });
    /*$(document).on('opened', '.agree-popup', function () {
    });*/
    $('.agree-popup iframe').on('load', function () {
        $(this).height($('body', $(this).contents()).height());
        util.loading('hide');
    });
    $(document).on('closed', '.agree-popup', function () {
        $(this).removeAttr('data-title').removeAttr('data-url');
        $('iframe', this).attr('src', 'about:blank');
    });

    var path = util.path();
    if (!util.isEmpty(path) && path !== '/' && path !== '/index.html') {
        /*path = '/applyQuota/checkIdCard.html';
         isDefault = true;*/
        util.redirect({
            url: path,
            ignore: true
        });
    }

    setTimeout(function () {
        util.loading('hide');
    }, 200);

    window.util = util;
});