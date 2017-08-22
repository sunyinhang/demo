var __baseResourcePath = '/mobile';
// 支持dev,run
var __env = 'dev';
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
        // imageLocation: '/sgbt/images',
        versionLocation: '',
        htmlLocation: '/html',
        scriptLocation: (__env === 'run' ? '/run' : '') + '/js'
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
        ],
        outSts: [
            {
                '1': '待提交'
            },
            {
                '2': '待确认'
            },
            {
                '3': '被退回'
            },
            {
                '01': '审批中'
            },
            {
                '02': '贷款被拒绝'
            },
            {
                '03': '贷款已取消'
            },
            {
                '04': '合同签订中'
            },
            {
                '05': '审批通过，等待放款'
            },
            {
                '06': '已放款'
            },
            {
                '20': '待放款'
            },
            {
                '22': '审批退回'
            },
            {
                '23': '合同签章中'
            },
            {
                '24': '放款审核中'
            },
            {
                '25': '额度申请被拒'
            },
            {
                '26': '额度申请已取消'
            },
            {
                '27': '已通过'
            },
            {
                'AA': '取消放款'
            },
            {
                'OD': '逾期'
            },
            {
                'WS': '待发货/待取货'
            },
            {
                '30': '已付款待发货'
            },
            {
                '31': '已发货'
            },
            {
                '92': '退货中'
            },
            {
                '93': '已退货'
            }
        ]
    },
    messages: {
        agree: '请先同意相关协议',
        card: '此卡为默认放款卡和还款卡,如果想更换默认还款卡，可以在个人中心-个人资料-银行卡中绑定并设置',
        btDefin: '"顺逛白条"是顺逛商城联合海尔消费金融为其家电购买用户提供的一款"先消费，后付款"的在线支付产品服务',
        card: '此卡为默认放款卡和还款卡,如果想更换默认还款卡，可以在个人中心-个人资料-银行卡中绑定并设置',
        cardholder: '为了您的账户安全，请您绑定本人银行卡',
        cardtype: '由于银行扣款要求，现已支持中国工商银行，中国邮政储蓄银行，中国农业银行，中国银行，中国建设银行，广东发展银行，兴业银行， 招商银行，交通银行，中信银行，中国光大银行，华夏银行，中国民生银行，平安银行，上海浦东发展银行，北京银行，上海银行，青岛银行'
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
