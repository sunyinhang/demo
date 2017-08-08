require(['avalon', 'jquery', 'util', 'Const', 'layer','mobileAreaSelect','msPhoto'], function(avalon, $, util, Const) {

	//个人信息、单位信息和紧急联系人显示与隐藏
	$('.tab').click(function(){
		if($(this).next('.list-group3').css('display')=='block'){
			$(this).next('.list-group3').hide();
			$(this).removeClass("tab-actvie");
			$('.tab').find('em').css('display','none');
		}else{
			$(this).siblings().removeClass("tab-actvie");
			$(this).addClass("tab-actvie");
			$('.list-group3').hide();
			$('.tab').find('em').css('display','none');
			$(this).next('.list-group3').show();
			$('.tab-actvie').find('em').css('display','block');
		}
	});

	//单位地址
	$('#officeaddress').mobileAreaSelect({
		code: 'areaCode',
		name: 'areaName',
		data: function(code) {
			var result = [];
			util.post({
				url: 'getAreaServlet.do',
				data: !util.isEmpty(code) ? {areaCode: code} : '',
				async: false,
				cache: true,
				success: function(res) {
					result = res.retObj;
				}
			});
			return result;
		}
	});

	//居住地址
	$('#address').mobileAreaSelect({
		code: 'areaCode',
		name: 'areaName',
		data: function(code) {
			var result = [];
			util.post({
				url: 'getAreaServlet.do',
				data: !util.isEmpty(code) ? {areaCode: code} : '',
				async: false,
				cache: true,
				success: function(res) {
					result = res.retObj;
				}
			});
			return result;
		}
	});

	var vm=avalon.redefine({
		$id: "personalInform",
		positionOptions: util.options('position'),
		//educationOptions: util.options('education'),
		maritalStatusOptions: util.options('maritalStatus'),
		relationType1Options: [],
		relationType2Options: [],
		config: {
			//defaultImage: '../mobile/themes/default/imgsb/cameraWhite.png',
			file_id: 'identityCard',
			file_title: '收款确认单',
            containerId: 'uploadPhoto',
			//url: 'data/GetOCRIdentityServlet.do',
			path: '',
			prompt:'必传影像不能为空'
		},
		uploadPhoto: function(event){
			/*var obj=$(event.currentTarget);
			obj.find("img").attr('src',"images/applyQuota/add_photo.png");*/
		},
		nextFn: function(event){
			this.validate.onManual();
		},
		validate: {
            onSuccess: function (reasons) {
            },
            onError: function (reasons) {
            },
            onValidateAll: function (reasons) {
            	if (!util.isEmpty(reasons)) {
                    util.alert(reasons[0]);
                } else {
        	        var addrCodeArr=vm.personal.officeAddressCode.split(",");
        	        var addrCodeArr2=vm.personal.liveAddressCode.split(",");
        			if(addrCodeArr[1]==0){
        				util.alert('请选择单位地址市/区');
        			}
        			else if(addrCodeArr[2]==0){
        				util.alert('请选择单位地址区/县');
        			}else if(addrCodeArr2[1]==0){
        				util.alert('请选择居住地址市/区');
        			}
        			else if(addrCodeArr2[2]==0){
        				util.alert('请选择居住地址区/县');
        			}
        			else{
                   	 	var url="saveCustExtInfoServlet.do?edflage=1"; //后台区分是额度激活跳转的个人资料还是取现
                   	 	var data = new FormData($('#mainForm', vm.$element)[0]);
                   	    util.upload({
                   			url: url,
                   			data: data,
                   			success:function(obj){
                            	util.upload({
                           			url: 'ifDoFaceServlet.do',
                           			success: function(res) { 
                           				if(res.retObj.faceFlag === '0'){
                           					//未设置支付密码
                           					util.redirect('setPayPsd.html');
                           				}else if (res.retObj.faceFlag === '1') {
                           					//已设置支付密码
                           					util.redirect('enterPayPsd.html');
                                        }else if (res.retObj.faceFlag === '2') {
                                        	// 手持身份证
                           					util.redirect('handholdIdCard.html');
                                        }
                           				else if (res.retObj.faceFlag === '3') {
                           					//人脸识别
                           					util.redirect('identityVrfic.html');;
                                        }else{
                                        	return;
                                        }
                           			}  
                           		})
                   			}
                   		});
        			}       
                }
            }
        }
	});
	vm.$watch('personal.maritalStatus', function(v) {

		if (v && v === Const.params.maritalStatusMarried) {
			vm.relationType1Options = util.options('relationType', undefined, Const.params.relationTypeCouple);
			vm.relationType2Options= util.options('relationType', Const.params.relationTypeCouple);
		} else {
			vm.relationType1Options = util.options('relationType', Const.params.relationTypeCouple);
			vm.relationType2Options = util.options('relationType', Const.params.relationTypeCouple);
		}
		vm.personal.lxr1.relationType = '';
		vm.personal.lxr2.relationType = '';
	});	
	
	//初始化个人信息
	var url="getAllCustExtInfo";
	util.post({
		url: url,
		type: 'post',
		success:function(obj){
			debugger
			//单位地址和居住地址
			vm.personal.officeAddress=obj.retObj.officeProvinceName+obj.retObj.officeCityName+obj.retObj.officeAreaName;
			vm.personal.officeAddressCode=obj.retObj.officeProvince+","+obj.retObj.officeCity+","+obj.retObj.officeArea; //单位地址编码officeArea
			vm.personal.liveAddress=obj.retObj.liveProvinceName+obj.retObj.liveCityName+obj.retObj.liveAreaName;
			vm.personal.liveAddressCode=obj.retObj.liveProvince+","+obj.retObj.liveCity+","+obj.retObj.liveArea; //居住地址编码
			
			//紧急联系人
			if(obj.retObj.lxrList.length >= 2){
				vm.personal.lxr1=obj.retObj.lxrList[0];
				vm.personal.lxr2=obj.retObj.lxrList[1];
			}else if(obj.retObj.lxrList.length == 1){
				vm.personal.lxr1=obj.retObj.lxrList[0];
				vm.personal.lxr2='';
			}else{
				vm.personal.lxr1='';
				vm.personal.lxr2='';
			}
			
			//必传影像
			if (obj.retObj.picId) {
				vm.config.defaultImage = Const.rest.baseUrl + 'initAttacheOtherServlet.do?token=' + util.token() + '&picId=' + obj.retObj.picId;
			}	
			
			vm.personal=avalon.mix({}, vm.personal, obj.retObj);
			//紧急联系人关系
			if (vm.personal.maritalStatus && vm.personal.maritalStatus === Const.params.maritalStatusMarried) {
				vm.relationType1Options = util.options('relationType', undefined, Const.params.relationTypeCouple);
				vm.relationType2Options = util.options('relationType', Const.params.relationTypeCouple);							
			} else {
				vm.relationType1Options = util.options('relationType', Const.params.relationTypeCouple);
				vm.relationType2Options = util.options('relationType', Const.params.relationTypeCouple);		
			}
			if(obj.retObj.lxrList.length >= 2){
				vm.personal.lxr1.relationType = obj.retObj.lxrList[0].relationType;
				vm.personal.lxr2.relationType = obj.retObj.lxrList[1].relationType;
			}else if(obj.retObj.lxrList.length == 1){
				vm.personal.lxr1.relationType = obj.retObj.lxrList[0].relationType;
				vm.personal.lxr2.relationType = '';
			}else{
				vm.personal.lxr1.relationType = '';
				vm.personal.lxr2.relationType = '';
			}
			
		}
	});
	//初始化个人信息 end

	///avalon.scan(document.body);
});