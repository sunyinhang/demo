var __baseResourcePath = 'public';
var __Const = {
    init: {
        debug: false,
        appId: 'acquirer',
        sysCode: false,
        login: false,
        show: {
            position: 'alert'
        },
        tabs: {
            menuRefresh: true
        }
    },
    url: {
        template: {
            todo: 'public/template/todo.html',
            confirm: 'public/template/modal/confirm.html',
            alert: 'public/template/modal/alert.html',
            exports: 'public/template/modal/export.html',
            imports: 'public/template/modal/import.html'
        },
        authority: {
            info: '${root}/user',
            menus: '${portal}/mMenus/authMenus?appId=acquirer'
        },
        cache: {
            entities: '/cache/entities',
            dicts: '/dict/selectAllFromCache',
            params: '/param/selectAllFromCache'
        },
        upload: {
            upload: '${root}/app/acquirer/file/uploadImg'
        },
        editor: {
            view: '${root}/app/acquirer/file/viewImg'
        }
    },
    rest: {
        head: true,
        baseUrl: window.location.protocol + "//" + window.location.host + '/acquirer/api'
    },
    portal: {
        appId: 'portal',
        baseUrl: window.location.protocol + "//" + window.location.host
    },
    menu: {
        parentId: 'parentMenu',
        url: 'menuUrl'
    },
    dicts: {
        logLevel: ['ERROR', 'WARN', 'INFO', 'DEBUG', 'TRACE']
    }
};
