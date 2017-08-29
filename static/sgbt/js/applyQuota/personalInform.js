require(['jquery', 'util', 'Const', 'bvAccordion', 'bvForm', 'bvUpload'], function($, util, Const) {
    // 获取额度重新提交标识
    var edxg = util.gup('edxg');

    var vm = util.bind({
        container: 'personalInform',
        data: {
            tags: {
                accordionKey: 'personalInformAccordion',
                formKey: 'personalInformForm'
            },
            entity: {
            },
            config: {
                items: [
                    {
                        title: '个人信息',
                        type: 'form',
                        config: {
                            clazz: 'form-align-right',
                            layout: 'inline',
                            container: 'div',
                            columns: [
                                {
                                    head: '婚姻状况',
                                    name: 'maritalStatus',
                                    edit: {
                                        type: 'select'
                                    },
                                    config: {
                                        attr: {
                                            placeholder: '请选择'
                                        },
                                        preset: 'json',
                                        choose: '#maritalStatus',
                                        validate: {
                                            // TODO: 非必填？
                                            //required: '婚姻状况不能为空'
                                        }
                                    }
                                },
                                {
                                    head: '居住地址',
                                    name: 'liveAddress',
                                    edit: {
                                        type: 'picker'
                                    },
                                    config: {
                                        attr: {
                                            placeholder: '请选择居住地址'
                                        },
                                        validate: {
                                            required: '居住地址不能为空'
                                        },
                                        code: 'areaCode',
                                        desc: 'areaName',
                                        // cache: true,
                                        initOption: {
                                            areaCode: '',
                                            areaName: '请选择'
                                        },
                                        choose: [
                                            {
                                                items: [],
                                                onInit: function () {
                                                    var _vm = this;
                                                    app.getArea('', function (items) {
                                                        _vm.changeItems(0, items);
                                                    });
                                                },
                                                onChange: function (value, values) {
                                                    if (util.isEmpty(value)) {
                                                        this.changeItems(1, []);
                                                    } else {
                                                        var _vm = this;
                                                        app.getArea(value, function (items) {
                                                            _vm.changeItems(1, items);
                                                        });
                                                    }
                                                }
                                            },
                                            {
                                                items: [],
                                                onChange: function (value, values) {
                                                    if (util.isEmpty(value)) {
                                                        this.changeItems(2, []);
                                                    } else {
                                                        var _vm = this;
                                                        app.getArea(value, function (items) {
                                                            _vm.changeItems(2, items);
                                                        });
                                                    }
                                                }
                                            },
                                            {
                                                items: []
                                            }
                                        ]
                                    }
                                },
                                {
                                    name: 'liveAddr',
                                    config: {
                                        attr: {
                                            placeholder: '居住详细地址',
                                            maxlength: '#address'
                                        },
                                        validate: {
                                            // TODO: 非必填？
                                            //required: '居住详细地址不能为空'
                                        }
                                    }
                                }
                            ]
                        }
                    },
                    {
                        title: '单位信息',
                        type: 'form',
                        config: {
                            clazz: 'form-align-right',
                            layout: 'inline',
                            columns: [
                                {
                                    head: '工作单位',
                                    name: 'officeName',
                                    config: {
                                        attr: {
                                            maxlength: '#officeName',
                                            placeholder: '请输入工作单位'
                                        },
                                        validate: {
                                            required: '请输入工作单位'
                                        }
                                    }
                                },
                                {
                                    head: '单位电话',
                                    name: 'officeTel',
                                    config: {
                                        attr: {
                                            placeholder: '请输入11-12位单位电话',
                                            maxlength: '#phone'
                                        },
                                        validate: {
                                            required: '单位电话不能为空',
                                            custom: {
                                                code: 'phone',
                                                desc: '请输入正确的单位电话'
                                            }
                                        }
                                    }
                                },
                                {
                                    head: '单位地址',
                                    name: 'officeAddress',
                                    edit: {
                                        type: 'picker'
                                    },
                                    config: {
                                        attr: {
                                            placeholder: '请选择单位地址'
                                        },
                                        validate: {
                                            required: '单位地址不能为空'
                                        },
                                        code: 'areaCode',
                                        desc: 'areaName',
                                        // cache: true,
                                        initOption: {
                                            areaCode: '',
                                            areaName: '请选择'
                                        },
                                        choose: [
                                            {
                                                items: [],
                                                onInit: function () {
                                                    var _vm = this;
                                                    app.getArea('', function (items) {
                                                        _vm.changeItems(0, items);
                                                    });
                                                },
                                                onChange: function (value, values) {
                                                    if (util.isEmpty(value)) {
                                                        this.changeItems(1, []);
                                                    } else {
                                                        var _vm = this;
                                                        app.getArea(value, function (items) {
                                                            _vm.changeItems(1, items);
                                                        });
                                                    }
                                                }
                                            },
                                            {
                                                items: [],
                                                onChange: function (value, values) {
                                                    if (util.isEmpty(value)) {
                                                        this.changeItems(2, []);
                                                    } else {
                                                        var _vm = this;
                                                        app.getArea(value, function (items) {
                                                            _vm.changeItems(2, items);
                                                        });
                                                    }
                                                }
                                            },
                                            {
                                                items: []
                                            }
                                        ]
                                    }
                                },
                                {
                                    name: 'officeAddr',
                                    config: {
                                        attr: {
                                            placeholder: '单位详细地址',
                                            maxlength: '#address'
                                        },
                                        validate: {
                                            required: '单位详细地址不能为空'
                                        }
                                    }
                                }
                            ]
                        }
                    },
                    {
                        title: '紧急联系人',
                        type: 'form',
                        config: {
                            name: 'relation',
                            clazz: 'form-align-right',
                            layout: 'inline',
                            container: 'div',
                            columns: [
                                {
                                    head: '联系人关系一',
                                    name: 'relationType1',
                                    edit: {
                                        type: 'select'
                                    },
                                    config: {
                                        attr: {
                                            placeholder: '请选择'
                                        },
                                        preset: 'json',
                                        choose: '#relationType',
                                        validate: {
                                            required: '请选择紧急联系人关系'
                                        }
                                    }
                                },
                                {
                                    head: '姓名',
                                    name: 'contactName1',
                                    config: {
                                        attr: {
                                            placeholder: '请输入联系人姓名',
                                            maxlength: '#name'
                                        },
                                        validate: {
                                            required: '紧急联系人姓名不能为空'
                                        }
                                    }
                                },
                                {
                                    head: '联系电话',
                                    name: 'contactMobile1',
                                    config: {
                                        attr: {
                                            placeholder: '请输入联系电话',
                                            maxlength: '#phone'
                                        },
                                        validate: {
                                            required: '紧急联系人联系电话不能为空',
                                            custom: {
                                                code: 'mobile',
                                                desc: '请输入正确的紧急联系人联系电话'
                                            }
                                        }
                                    }
                                },
                                {
                                    head: '联系人关系二',
                                    name: 'relationType2',
                                    edit: {
                                        type: 'select'
                                    },
                                    config: {
                                        attr: {
                                            placeholder: '请选择'
                                        },
                                        preset: 'json',
                                        choose: '#relationType',
                                        excludes: [Const.params.relationTypeCouple],
                                        validate: {
                                            required: '请选择紧急联系人关系'
                                        }
                                    }
                                },
                                {
                                    head: '姓名',
                                    name: 'contactName2',
                                    config: {
                                        attr: {
                                            placeholder: '请输入联系人姓名',
                                            maxlength: '#name'
                                        },
                                        validate: {
                                            required: '紧急联系人姓名不能为空'
                                        }
                                    }
                                },
                                {
                                    head: '联系电话',
                                    name: 'contactMobile2',
                                    config: {
                                        attr: {
                                            placeholder: '请输入联系电话',
                                            maxlength: '#phone'
                                        },
                                        validate: {
                                            required: '紧急联系人联系电话不能为空',
                                            custom: {
                                                code: 'mobile',
                                                desc: '请输入正确的紧急联系人联系电话'
                                            }
                                        }
                                    }
                                }
                            ]
                        }
                    },
                    {
                        title: '选传影像',
                        create: util.gup('edxg') !== null,
                        type: 'form',
                        config: {
                            name: 'files',
                            columns: [
                            ]
                        }
                    }
                ]
            },
            operateConfig: {
                type: 'form',
                container: 'div',
                operates: [
                    {
                        text: '下一步',
                        layout: 'primary',
                        click: function (event, editType, entity) {
                            //居住地址和单位地址校验
                            var liveAddressAttr = entity.liveAddress.split(",");
                            var officeAddressAttr = entity.officeAddress.split(",");
                            if(util.isEmpty(liveAddressAttr[1])){
                                util.alert('请选择居住地址市/区 ');
                            }else if(util.isEmpty(liveAddressAttr[2])){
                                util.alert('请选择居住地址区/县 ');
                            }else if(util.isEmpty(officeAddressAttr[1])){
                                util.alert('请选择单位地址市/区 ');
                            }else if(util.isEmpty(officeAddressAttr[2])){
                                util.alert('请选择单位地址区/县 ');
                            }else{
                                util.post({
                                    url: "/saveAllCustExtInfo",
                                    data:{
                                        liveAddress_code: entity.liveAddress,
                                        officeAddress_code: entity.officeAddress,
                                        maritalStatus: entity.maritalStatus,
                                        liveAddr: entity.liveAddr,
                                        officeName: entity.officeName,
                                        officeTel: entity.officeTel,
                                        officeAddr: entity.officeAddr,
                                        id_one: entity.id1,
                                        relationType_one: entity.relationType1,
                                        contactName_one: entity.contactName1,
                                        contactMobile_one: entity.contactMobile1,
                                        id_two: entity.id2,
                                        relationType_two: entity.relationType2,
                                        contactName_two: entity.contactName2,
                                        contactMobile_two: entity.contactMobile2
                                    },
                                    success:function(res){
                                        //1：通过人脸识别，并已设置支付密码
                                        //2：通过人脸识别，但没有设置支付密码
                                        //3. 未通过人脸识别，剩余次数为0，不能再做人脸识别，录单终止
                                        //4：未通过人脸识别，剩余次数为0，不能再做人脸识别，但可以上传替代影像
                                        //5. 跳转人脸识别
                                        if(res.body.flag == '1'){
                                            util.redirect({
                                                // title: '确认支付密码',
                                                url: util.mix('/applyQuota/confirmPayPsd.html', {
                                                    edxg: util.gup('edxg')
                                                }, true)
                                            });
                                        }else if(res.body.flag == '2'){
                                            util.redirect({
                                                // title: '设置支付密码',
                                                url: util.mix('/applyQuota/setPayPsd.html', {
                                                    edxg: util.gup('edxg')
                                                }, true)
                                            });
                                        }else if(res.body.flag == '3'){
                                            util.alert('#faceTerminate')

                                        }else if(res.body.flag == '4'){
                                            util.redirect({
                                                // title: '手持身份证',
                                                url: util.mix('/applyQuota/handholdIdCard.html', {
                                                    edxg: util.gup('edxg')
                                                }, true)
                                            });
                                        }else if(res.body.flag == '5'){
                                            util.redirect({
                                                // title: '人脸识别',
                                                url: util.mix('/applyQuota/identityVrfic.html', {
                                                    edxg: util.gup('edxg')
                                                }, true)
                                            });
                                        }
                                    }
                                });
                            }
                        }
                    }
                ]
            }
        },
        watch: {
            'entity.maritalStatus': function (val, oldVal) {
                var relationType1Vm = util.vm(vm, vm.tags.accordionKey, 'relation.relationType1');
                if (relationType1Vm) {
                    if (!val || val === Const.params.maritalStatusUnknown) {
                        util.refresh({
                            vm: relationType1Vm,
                            excludes: []
                        });
                    } else if (val === Const.params.maritalStatusMarried) {
                        util.refresh({
                            vm: relationType1Vm,
                            includes: [Const.params.relationTypeCouple]
                        });
                    } else {
                        util.refresh({
                            vm: relationType1Vm,
                            excludes: [Const.params.relationTypeCouple]
                        });
                    }
                }
            }
        },
        mounted: function () {
            util.post({
                url: "/getAllCustExtInfo",
                success: function(res) {
                    var data = util.data(res);
                    if(!util.isEmpty(data) && !util.isEmpty(data.CustExtInfoMap)){
                        util.refresh({
                            vm: util.vm(vm, vm.tags.formKey),
                            entity: {
                                maritalStatus: data.CustExtInfoMap.maritalStatus,
                                liveAddress: data.CustExtInfoMap.liveProvince+ ','+data.CustExtInfoMap.liveCity+','+data.CustExtInfoMap.liveArea,
                                /// liveAddress: data.CustExtInfoMap.liveProvinceName+ data.CustExtInfoMap.liveCityName+data.CustExtInfoMap.liveAreaName,
                                liveAddr: data.CustExtInfoMap.liveAddr,
                                officeName: data.CustExtInfoMap.officeName,
                                officeTel: data.CustExtInfoMap.officeTel,
                                officeAddr: data.CustExtInfoMap.officeAddr,
                                officeAddress: data.CustExtInfoMap.officeProvince+','+ data.CustExtInfoMap.officeCity+','+data.CustExtInfoMap.officeArea
                                /// officeAddress: data.CustExtInfoMap.officeProvinceName+ data.CustExtInfoMap.officeCity+data.CustExtInfoMap.officeAreaName
                            }
                        });
                        for (var i=0; i<data.CustExtInfoMap.lxrList.length; i++) {
                            var lxr = {};
                            lxr['id' + (i + 1)] = data.CustExtInfoMap.lxrList[i].id;
                            lxr['relationType' + (i + 1)] = data.CustExtInfoMap.lxrList[i].relationType;
                            lxr['contactName' + (i + 1)] = data.CustExtInfoMap.lxrList[i].contactName;
                            lxr['contactMobile' + (i + 1)] = data.CustExtInfoMap.lxrList[i].contactMobile;
                            util.refresh({
                                vm: util.vm(vm, vm.tags.formKey),
                                entity: lxr
                            });
                            if (i === 1) {
                                break;
                            }
                        }
                        for (var i=data.CustExtInfoMap.lxrList.length; i<2; i++) {
                            var lxr = {};
                            lxr['id' + (i + 1)] = null;
                            lxr['relationType' + (i + 1)] = null;
                            lxr['contactName' + (i + 1)] = null;
                            lxr['contactMobile' + (i + 1)] = null;
                            util.refresh({
                                vm: util.vm(vm, vm.tags.formKey),
                                entity: lxr
                            });
                            if (i === 1) {
                                break;
                            }
                        }

                        // 选传影像
                        if (data.docList && data.docList.length > 0) {
                            var filesVm = util.vm(vm, vm.tags.accordionKey, 'files');
                            if (filesVm != null) {
                                for (var i=0; i<data.docList.length; i++) {
                                    var files = [];
                                    if (data.docList[i].urlList && data.docList[i].urlList.length > 0) {
                                        for (var fileIndex=0; fileIndex<data.docList[i].urlList.length; fileIndex++) {
                                            files.push({
                                                id: data.docList[i].urlList[fileIndex].id,
                                                url: Const.rest.baseUrl + '/attachPic?filePath=' + data.docList[i].urlList[fileIndex].filePath   //Const.route.imageLocation + data.docList[i].urlList[fileIndex].filePath
                                            });
                                        }
                                    } else {
                                        files.push({});
                                    }
                                    util.refresh({
                                        vm: filesVm,
                                        columns: [
                                            {
                                                head: data.docList[i].docDesc,
                                                name: data.docList[i].docCde,
                                                edit: {
                                                    type: 'upload'
                                                },
                                                config: {
                                                    clazz: 'upload-multiple',
                                                    url: util.mix('/upIconPic', {
                                                        docCde: data.docList[i].docCde,
                                                        docDesc: data.docList[i].docDesc
                                                    }),
                                                    formName: 'iconImg',
                                                    files: files,
                                                    autoIncrease: true,
                                                    // autoUpload: false,
                                                    delete: function (file, callback) {
                                                        util.post({
                                                            url: '/attachDelete',
                                                            data: {
                                                                id: file.id
                                                            },
                                                            success: function () {
                                                                callback.call(null);
                                                            }
                                                        });
                                                    }
                                                }
                                            }
                                        ]
                                    });
                                }
                            }
                        }
                    }
                }
            });
        }
    });
});