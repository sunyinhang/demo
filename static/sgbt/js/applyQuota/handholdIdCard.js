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
                                    util.alert('#needCapture');
                                    return false;
                                }
                                return true;
                            }
                        },
                        click: function (event, editType, entity) {
                            util.upload({
                                url: util.mix('/uploadPersonPic', {
                                    /*idNumber: 'C201708110701812339790',
                                    name: '18325423979',*/
                                    edflag: 1
                                }),
                                data: util.form('faceImg', entity.upload.faceImg),
                                success: function (res) {
                                    var data = util.data(res);
                                    if( ! util.isEmpty(data)){
                                        if (data.faceFlag === '0') {
                                            //未设置支付密码
                                            util.redirect({
                                                // title: '设置支付密码',
                                                url: util.mix('/applyQuota/setPayPsd.html', {
                                                    edxg: util.gup('edxg')
                                                }, true)
                                            });
                                        } else if (data.faceFlag === '1') {
                                            //已设置支付密码
                                            util.redirect({
                                                // title: '确认支付密码页面',
                                                url: util.mix('/applyQuota/confirmPayPsd.html', {
                                                    edxg: util.gup('edxg')
                                                }, true)
                                            });
                                        } else if (data.faceFlag === '2') {
                                            //人脸识别失败，跳转手持身份证
                                            util.redirect({
                                                // title: '手持身份证',
                                                url: util.mix('/applyQuota/handholdIdCard.html', {
                                                    edxg: util.gup('edxg')
                                                }, true)
                                            });
                                        } else if (data.faceFlag === '3') {
                                            //人脸识别失败，再拍摄一遍
                                            util.alert('#faceCapture');
                                        } else {
                                            return;
                                        }
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