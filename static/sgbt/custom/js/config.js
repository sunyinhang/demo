var __baseResourcePath = '/mobile';
var __Const = {
    rest: {
        baseUrl: window.location.protocol + "//" + window.location.host + '/api/payment',
        headers: {
            channel: '11',
            channelNo: '46'
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
    },
    messages: {
    },
    lengths: {
        userId: 11,
        name: 90,
        mobile: 11,
        phone: 30,
        password: 20,
        payPassword: 20,
        verifyNo: 6,
        // 工作单位
        officeName: 300,
        // 所在部门
        officeDept: 300,
        // 地址
        address: 300,
        // 分期金额
        applyMoney: 8,
        // 银行卡号
        bankCard: 19,
        idCard: 60,
        // 发证机关
        issue: 90
    }
}
