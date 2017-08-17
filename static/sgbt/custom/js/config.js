var __baseResourcePath = '/mobile';
var __Const = {
    rest: {
        baseUrl: window.location.protocol + "//" + window.location.host + '/api/payment',
        headers: {
            channel: '11',
            channelNo: '46'
        }
    },
    url: {
        cache: {
            get: '/cache',
            set: '/cache'
        }
    },
    route: {
        baseUrl: window.location.protocol + "//" + window.location.host,
        baseLocation: '/sgbt',
        imageLocation: '/sgbt/images',
        versionLocation: '',
        htmlLocation: '/html',
        scriptLocation: '/js'
    },
    params: {
        maritalStatusUnknown: '60',
        maritalStatusMarried: '20',
        relationTypeCouple: '06'
    },
    dicts: {
        education: [
            {
                '00': '硕士及以上'
            },
            {
                '10': '本科'
            },
            {
                '20': '大专'
            },
            {
                '30': '高中'
            },
            {
                '40': '初中及以下'
            }
        ],
        maritalStatus: [
            {
                '10': '未婚'
            },
            {
                '20': '已婚'
            },
            {
                '40': '离异'
            },
            {
                '50': '丧偶'
            },
            {
                '60': '未知'
            }
        ],
        relationType: [
            {
                '01': '父母'
            },
            {
                '02': '子女及兄弟姐妹'
            },
            {
                '06': '夫妻'
            },
            {
                '99': '其他'
            }
        ]
    },
    messages: {
        agree: '请先同意相关协议',
        card: '此卡为默认放款卡和还款卡,如果想更换默认还款卡，可以在个人中心-个人资料-银行卡中绑定并设置',
        btDefin: '"顺逛白条"是顺逛商城联合海尔消费金融为其家电购买用户提供的一款"先消费，后付款"的在线支付产品服务'
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
        issue: 90,
        //身份证
        idCardNo: 18
    },
    cache: {
        type: 'both'
    }
}
