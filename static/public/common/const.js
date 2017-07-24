/*
常量定义
*/
define({
    // 是否已完成初始化，内部用
    inited: false,
    // 是否已弹窗，内部用，避免重复弹窗（比如登录超时后，调用多个ajax接口，避免弹多个错误提示）
    alerted: false,
    init: {
        // 是否开启DEBUG模式
        debug: false,
        // 是否支持市县集中模式
        multiArea: false,
        // 应用编码
        appCode: __appCode,
        // 是否初始化配置，调用后台接口
        config: false,
        // 错误提示配置，type支持：alert-弹窗toastr-浮动提示
        show: {
            type: 'toastr',
            position: 'top-center',
            // lg md弹窗用，错误内容超过指定大小后调整弹窗大小，超过100弹窗大小为大，超过50弹窗大小为中
            lg: 100,
            md: 50
        },
        // 字典参数配置初始化定义
        preset: {
            'default': {
                code: 'code',
                desc: 'desc'
            },
            enums: {
                code: 'name',
                desc: 'desc'
            },
            dict: {
                code: 'dictCode',
                desc: 'dictShow'
            },
            simple: {
                code: 'name',
                desc: 'name'
            },
            json: {
                code: 'code',
                desc: 'name'
            }
        },
        // 标签页相关配置
        tabs: {
            menuRefresh: true,       // 通过菜单点击时，刷新标签页内容
            clickRefresh: false     // 点击标签页时，刷新标签页内容（暂不支持）
        }
    },
    // 后台rest接口地址，默认带/api
    rest: {
        baseUrl: window.location.protocol + "//" + window.location.host + (__appCode === 'portal' ? '' : ('/' + __appPath)) + '/api'
    },
    // 后台portal接口地址，默认带/api
    portal: {
        baseUrl: window.location.protocol + "//" + window.location.host + '/api'
    },
    // 部分地址配置
    url: {
        // home页
        home: 'modules/home.html',
        // html模板
        template: {
            todo: __baseResourcePath + '/template/todo.html',
            confirm: __baseResourcePath + '/template/modal/confirm.html',
            alert: __baseResourcePath + '/template/modal/alert.html',
            login: __baseResourcePath + '/template/modal/login.html',
            source: __baseResourcePath + '/template/modal/source.html',
            exports: __baseResourcePath + '/template/modal/export.html',
            imports: __baseResourcePath + '/template/modal/import.html'
        },
        table: {
            page: '/common/table/page',
            select: '/common/table/select',
            deleteByIds: '/common/table/deleteByIds'
        },
        form: {
            init: '/common/form/init',
            insert: '/common/form/insert',
            update: '/common/form/update',
            deleteById: '/common/form/delete'
        },
        select: {
            query: '/common/select/query',
            init: '/common/select/init'
        },
        auto: {
            query: '/common/auto/query',
            init: '/common/auto/init'
        },
        authority: {
            check: '${portal}/authority/check',
            info: '${portal}/authority/info',
            changePassword: '${portal}/authority/changePassword',
            generateCode: '${portal}/authority/generateCode',
            login: '${portal}/authority/login',
            logout: '${portal}/authority/logout',
            menus: '@{portal}/authority/menus',
            apps: '${portal}/authority/apps',
            roles: '${portal}/authority/userRights',
            roleRights: '@{portal}/authority/roleRights'
        },
        file: {
            download: '${portal}/file'      // /{fileSign}
        },
        excel: {
            upload: '${portal}/file/upload',
            download: '${portal}/file/download',
            template: '${portal}/file/template'
        },
        cache: {
            config: '/cache/config',
            enums: '/cache/enums',
            dicts: '/cache/dicts',
            params: '/cache/params'
        },
        tree: {
            query: '/common/tree/query'
        },
        upload: {
            upload: '${portal}/file/upload',
            swf: '${root}/js/plupload-2/Moxie.swf'
        },
        editor: {
            view: '${root}/pub/file'
        }
    },
    // 菜单属性配置id-编号name-名称parentId-父菜单编号url-访问地址
    menu: {
        id: 'menuId',
        name: 'menuName',
        parentId: 'parentMenu',
        url: 'menuAddress'
    },
    // 字典配置
    dicts: {},
    // 规则定义，支持最大长度和数据校验（目前基本未用）
    rule: {
        cityCode: {
            maxlength: 6,
            validate: 'number'
        },
        cityName: {
            maxlength: 30
        },
        sysCode: {
            maxlength: 2
        },
        sysName: {
            maxlength: 30
        },
        shortName: {
            maxlength: 30
        },
        abbrName: {
            maxlength: 10
        },
        sysUrl: {
            maxlength: 50
        },
        dbType: {
            maxlength: 10
        },
        dbUser: {
            maxlength: 15
        }
    },
    width: {
        checkbox: '40px',
        radio: '40px',
        // 序号
        linenumber: '50px',
        date: '100px',
        time: '80px',
        datetime: '150px'
    },
    // 图表相关初始化配置
    chart: {
        backgroundColors: ['#7cb5ec', '#434348', '#90ed7d', '#f7a35c',
            '#8085e9', '#f15c80', '#e4d354', '#8085e8', '#8d4653', '#91e8e1']
    },
    global: __global
});
