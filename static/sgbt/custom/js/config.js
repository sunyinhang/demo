var __baseResourcePath = '/mobile';
// 支持dev,run
var __env = 'dev';
var __Const = {
    init: {
        auth: 'h5'
    },
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
        },
        report: '/report'
    },
    route: {
        baseUrl: window.location.protocol + "//" + window.location.host,
        baseLocation: '/sgbt',
        imageLocation: '/sgbt/images',
        versionLocation: '',
        htmlLocation: '/html',
        scriptLocation: (__env === 'run' ? '/run' : '') + '/js'
    },
    params: {
        maritalStatusUnknown: '60',
        maritalStatusMarried: '20',
        relationTypeCouple: '06',
        // 百度地图api
        mapKey: 'vUz58Gv8yMI0LuDeIzE37GnETZlLhAGm'
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
        alertOk: '我知道了',
        unknownError: '网络通讯异常',
        error500: '网络通讯异常',
        error401: '登录超时，请重新登录',
        error403: '登录超时',
        error404: '网络通讯异常',
        error405: '网络通讯异常',
        error408: '请求超时',
        locationFail: '获取地理位置失败，无法继续操作，请确认GPS定位是否打开或网络是否正常！',
        agreement: '请先同意相关协议',
        cardDescribe: '此卡为默认放款卡和还款卡,如果想更换默认还款卡，可以在个人中心-个人资料-银行卡中绑定并设置',
        cardBind: '为了您的账户安全，请您绑定本人银行卡',
        cardSupport: '由于银行扣款要求，现已支持中国工商银行，中国邮政储蓄银行，中国农业银行，中国银行，中国建设银行，广东发展银行，兴业银行， 招商银行，交通银行，中信银行，中国光大银行，华夏银行，中国民生银行，平安银行，上海浦东发展银行，北京银行，上海银行，青岛银行',
        reservedMobile: '银行预留手机号码是办理该银行卡是所填写的手机号码。没有预留、手机号码忘记或者已停用，请联系银行客服进行处理。',
        needCapture: '请使用扫描功能上传信息',
        faceCapture: '人脸识别失败，请再使用摄像头拍摄一张人脸照片',
        faceTerminate: '不能再做人脸识别，录单终止',
        btDescribe: '"顺逛白条"是顺逛商城联合海尔消费金融为其家电购买用户提供的一款"先消费，后付款"的在线支付产品服务',
        cardMatch: '请先输入银行卡号,卡类型会自动匹配！',
        payPwdInform: '支付密码输入错误，请重新输入！',
        payPwdError: '支付密码不能为空'
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
