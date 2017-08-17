require(['jquery', 'util', 'Const'], function($, util, Const) {
    var vm = util.bind({
        container: 'amountActive',
        data: {
            stateConfig:{
                imgSrc: 'custom/themes/default/images/amtNotTopBg.png',
                title: '海尔白条',
                amtNum: 'amtNum',
                //num: '暂无',
                whetherBtn: true,
                activBtn: 'activAmuntBtn',
                btnText: '激活额度'
            }
        },
        methods: {
            activeAmountFn: function(param){
                util.post({
                    url: "/CreditLineApply",
                    success: function (res) {
                        //1：通过人脸识别，并已设置支付密码
                        //2：通过人脸识别，但没有设置支付密码
                        //3. 未通过人脸识别，剩余次数为0，不能再做人脸识别，录单终止
                        //4：未通过人脸识别，剩余次数为0，不能再做人脸识别，但可以上传替代影像
                        //5. 跳转人脸识别
                        //6.未实名认证
                        //7.个人扩展信息未完整
                        if(res.body.flag == '1'){
                            util.redirect({
                                title: '确认支付密码',
                                url: '/applyQuota/confirmPayPsd.html',
                                back: false
                            });
                        }else if(res.body.flag == '2'){
                            util.redirect({
                                title: '设置支付密码',
                                url: '/applyQuota/setPayPsd.html',
                                back: false
                            });
                        }else if(res.body.flag == '3'){
                            //TODO

                        }else if(res.body.flag == '4'){
                            util.redirect({
                                title: '手持身份证',
                                url: '/applyQuota/handholdIdCard.html',
                                back: false
                            });
                        }else if(res.body.flag == '5'){
                            util.redirect({
                                title: '人脸识别',
                                url: '/applyQuota/identityVrfic.html',
                                back: false
                            });
                        }else if(res.body.flag == '6'){
                            util.redirect({
                                title: '实名绑卡',
                                url: '/applyQuota/checkIdCard.html',
                                back: false
                            });
                        }else if(res.body.flag == '7'){
                            util.redirect({
                                title: '个人资料',
                                url: '/applyQuota/personalInform.html',
                                back: false
                            });
                        }
                    }
                });
            }
        }
    });
});