require(['vue', 'jquery', 'util'], function(vue, $, util) {
    vue.component('tab-applyQuota', {
        props: {
            active: {
                default: 'realName'
            }
        },
        data: function () {
            return {
                innerTabs: [
                    {
                        text: '实名',
                        clazz: 'realName'
                    },
                    {
                        text: '个人资料',
                        clazz: 'personal'
                    },
                    {
                        text: '人脸识别',
                        clazz: 'face'
                    },
                    {
                        text: '密码设置',
                        clazz: 'password'
                    }
                ]
            }
        },
        /****** 模板定义 ******/
        template: util.heredoc(function() {
            /*!
            <div class="tab-applyQuota">
                <ul>
                    <li v-for="el in innerTabs" :class="[{'active': active.indexOf(el.clazz) >= 0}, el.clazz]" v-text="el.text"></li>
                </ul>
            </div>
            */
        })
    });
    vue.component('quotaState',{
        template: util.heredoc(function(){
            /*!
             <div class="amtSituTopPrnt">
                <div class="btmBg"><img v-bind:src="imgSrc"/></div>
                <div class="cvgCont">
                     <div class="amtNotTitle"><a href="javascript:void(0);" @click='redirectFn' v-if="whetherRedirect"></a><span>{{title}}</span></div>
                     <div class="amtNotSubTitle" v-if="whether">{{subTitle}}</div>
                     <div :class="amtNum"><span class="rmb" v-if="numUnit">&yen;</span>{{num}}</div>
                     <div class="amtReson" v-if="whetherDesc">您暂不符合申请条件，请稍后再试</div>
                     <div class="amtActivBtn" v-if="whetherBtn"><a href="javascript:void(0);" :class="activBtn" v-on:click='btnClickFn'>{{btnText}}</a></div>
                 </div>
             </div>
             */
        }),
        props:['whetherRedirect','imgSrc','title','whether','subTitle','amtNum','numUnit','num','whetherBtn','activBtn','btnText','whetherDesc'],
        data: function(){
            return{
            }
        },
        methods:{
            redirectFn: function(){
                this.$emit('on-redirect');
            },
            btnClickFn: function(){
                this.$emit('on-click');
            }
        }
    });
    vue.component('staticText',{
        template: util.heredoc(function(){
            /*!
            <div class="b-downloadTips">
             <template v-for="el in items">
                 <div class="downloadTips bluefont margt24"><div class="blueCircle"></div><div class="ft">{{el.title}}</div><div class="clear"></div></div>
                 <div class="downloadTips2 grayfont" v-for="desc in el.descs">{{ desc }}</div>
             </template>
            </div>
             */
        }),
        data: function(){
            return {
                items: [
                    {
                        title: '下载海尔消费金融app“嗨付”',
                        descs: ['可获得更高额度，可查询审批进度，详细还款计划，查看借款合同，进行主动还款']
                    },{
                        title: '下载方式',
                        descs: ['ios通过app store搜索“嗨付”','安卓通过各大市场搜索“嗨付”']
                    },{
                        title: '关注海尔会员中心官微',
                        descs: ['在个人中心-现金额度中查看额度，并申请贷款']
                    }
                ]
            }
        },
    });
});
var __areaCache = {};
function cacheArea(code, items) {
    if (!items) {
        if (__areaCache.inits) {
            if (!code) {
                // 省
                return __areaCache.inits;
            } else {
                if (__areaCache[code]) {
                    return __areaCache[code];
                }
            }
        }
        return null;
    } else {
        if (!code) {
            __areaCache.inits = items;
        } else {
            __areaCache[code] = items;
        }
    }
}
var __loading = false;
function getArea (code, callback) {
    var items = cacheArea(code);
    if (items) {
        callback.call(null, items);
        return;
    }
    if (__loading) {
        setTimeout(function () {
            getArea(code, callback);
        }, 500);
        return;
    }
    __loading = true;
    // var items = [];
    util.get({
        url: '/getArea',
        data: !util.isEmpty(code) ? {areaCode: code} : '',
        // async: false,
        cache: true,
        success: function(res) {
            cacheArea(code, util.data(res));
            __loading = false;
            callback.call(null, util.data(res));
            // items = util.data(res);
        },
        error: function () {
            __loading = false;
        }
    });
    // return items;
}
//1：通过人脸识别，并已设置支付密码
//2：通过人脸识别，但没有设置支付密码
//3. 未通过人脸识别，剩余次数为0，不能再做人脸识别，录单终止
//4：未通过人脸识别，剩余次数为0，不能再做人脸识别，但可以上传替代影像
//5. 跳转人脸识别
//6.未实名认证
//7.个人扩展信息未完整
function initRedirect (flag) {
    if (flag == '1') {
        util.redirect({
            // title: '确认支付密码',
            url: '/applyQuota/confirmPayPsd.html'
        });
    } else if (flag == '2') {
        util.redirect({
            // title: '设置支付密码',
            url: '/applyQuota/setPayPsd.html'
        });
    } else if (flag == '3') {
        //TODO

    } else if (flag == '4') {
        util.redirect({
            // title: '手持身份证',
            url: '/applyQuota/handholdIdCard.html'
        });
    } else if (flag == '5') {
        util.redirect({
            // title: '人脸识别',
            url: '/applyQuota/identityVrfic.html'
        });
    } else if (flag == '6') {
        util.redirect({
            // title: '实名绑卡',
            url: '/applyQuota/checkIdCard.html'
        });
    } else if (flag == '7') {
        util.redirect({
            // title: '个人资料',
            url: '/applyQuota/personalInform.html'
        });
    }
}
window.app = {
    cacheArea: cacheArea,
    getArea: getArea
}
