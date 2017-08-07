var __baseResourcePath = '/mobile';
var __Const = {
    rest: {
        baseUrl: window.location.protocol + "//" + window.location.host + '/api/payment',
        headers: {
            channel: 'xx',
            channelNo: 'yy'
        }
    },
    route: {
        baseUrl: window.location.protocol + "//" + window.location.host,
        baseLocation: '/sgbt',
        versionLocation: '',
        htmlLocation: '/modules'
    },
    dicts: {
        education: {
            '00': '硕士及以上',
            '10': '本科',
            '20': '大专',
            '30': '高中',
            '40': '初中及以下'
        },
        maritalStatus: {
            '10': '未婚',
            '20': '已婚',
            '40': '离异',
            '50': '丧偶'
        },
        relationType: {
            '01': '父母',
            '02': '子女及兄弟姐妹',
            '06': '夫妻',
            '99': '其他'
        }
    }
}
