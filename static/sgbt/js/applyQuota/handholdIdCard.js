require(['jquery', 'util', 'Const', 'bvUpload', 'bvForm'], function($, util, Const) {
    var vm = util.bind({
        container: 'handholdIdCard',
        data: {
            tags: {
                formKey: 'handheldIdCardForm'
            },
            entity: {},
            uploadConfig: {
                name: 'upload',
                type: 'camera',
                clazz: 'handheldIdCamera',
                formName: 'faceImg',
                autoUpload: false,
                url: '/uploadFacePic',
                files: [
                    {
                        name: 'faceImg',
                        head: '手持身份证正面照片'
                    }
                ]
            },
            formConfig: {
                layout: 'inline',
                columns: [],
                operates: [
                    {
                        text: '下一步',
                        layout: 'primary',
                        validate: {
                            before: function (event, editType, entity) {
                                if (!entity.upload || !entity.upload.faceImg) {
                                    util.alert('请使用扫描功能上传信息');
                                    return false;
                                }
                                return true;
                            }
                        },
                        click: function (event, editType, entity) {
                            util.upload({
                                url: util.mix('/uploadPersonPic', {
                                    idNumber: 'C201708110701812339790',
                                    name: '18325423979',
                                    edflag: 1
                                }),
                                data: util.form('faceImg', entity.upload.faceImg),
                                success: function (res) {
                                    if (res.retObj.faceFlag === '0') {
                                        //未设置支付密码
                                        util.redirect({
                                            title: '设置支付密码',
                                            url: '/applyQuota/setPayPsd.html',
                                            back: false
                                        });
                                    } else if (res.retObj.faceFlag === '1') {
                                        //已设置支付密码
                                        util.redirect({
                                            title: '输入短信验证码',
                                            url: '/applyQuota/payPsdValidcode.html',
                                            back: false
                                        });
                                    } else if (res.retObj.faceFlag === '2') {
                                        //人脸识别失败，跳转手持身份证
                                        util.redirect({
                                            title: '手持身份证',
                                            url: '/applyQuota/handholdIdCard.html',
                                            back: false
                                        });
                                    } else if (res.retObj.faceFlag === '3') {
                                        //人脸识别失败，再拍摄一遍
                                        util.alert('人脸识别失败，请再使用摄像头拍摄一张人脸照片');
                                    } else {
                                        return;
                                    }
                                }
                            });
                        }
                    }
                ]
            }
        }
    });
});