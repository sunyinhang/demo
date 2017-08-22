require(['jquery', 'util', 'Const', 'bvUpload', 'bvForm'], function($, util, Const) {
    var vm = util.bind({
        container: 'identityVrfic',
        data: {
            tags: {
                formKey: 'identityVrficForm'
            },
            entity: {},
            uploadConfig: {
                name: 'upload',
                type: 'camera',
                clazz: 'faceCamera',
                formName: 'faceImg',
                autoUpload: false,
                url: '/uploadFacePic',
                files: [
                    {
                        name: 'faceImg',
                        head: '请将脸部正对镜头，保持光线充足'
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
                                url: util.mix('/uploadFacePic', {
                                    edflag: 1
                                }),
                                data: util.form('faceImg', entity.upload.faceImg),
                                success: function (res) {
                                    var data = util.data(res);
                                    if ( data.faceFlag === '0') {
                                        //未设置支付密码
                                        util.redirect({
                                            title: '设置支付密码',
                                            url: '/applyQuota/setPayPsd.html',
                                            back: false
                                        });
                                    } else if (data.faceFlag === '1') {
                                        //已设置支付密码
                                        util.redirect({
                                            title: '输入短信验证码',
                                            url: '/applyQuota/payPsdValidcode.html',
                                            back: false
                                        });
                                    } else if (data.faceFlag === '2') {
                                        //人脸识别失败，跳转手持身份证
                                        util.redirect({
                                            title: '手持身份证',
                                            url: '/applyQuota/handholdIdCard.html',
                                            back: false
                                        });
                                    } else if (data.faceFlag === '3') {
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