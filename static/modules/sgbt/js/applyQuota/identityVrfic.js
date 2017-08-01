require(['avalon', 'jquery', 'util', 'Const', 'layer','msPhotoUgrade'], function(avalon, $, util, Const) {

	var vm=avalon.redefine({
		$id: "identityVrfic",
		config: {
			defaultImage: 'images/face.png',
			file_id: 'identityCard',
			file_title: '请将脸部正对镜头，保持光线充足',
      containerId: 'uploadPhoto',
			path: ''
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
                if (reasons.length) {
                    util.alert("#faceIdent");
                } else {                 	
                  util.upload({
               			url: 'uploadFacePicServlet.do',
               			data: new FormData($('#faceForm', vm.$element)[0]),
               			success: function(res) { 
               				if(res.retObj.faceFlag === '0'){
               					//未设置支付密码
               					util.redirect('setPayPsd.html');
               				}else if (res.retObj.faceFlag === '1') {
               					//已设置支付密码
               					util.redirect('payPsdValidcode.html');
                            }else if (res.retObj.faceFlag === '2') {
               					//人脸识别失败，跳转手持身份证
               					util.redirect('handholdIdCard.html');
                            }
               				else if (res.retObj.faceFlag === '3') {
               					//人脸识别失败，再拍摄一遍
                                util.alert('人脸识别失败，请再使用摄像头拍摄一张人脸照片');
                            }else{
                            	return;
                            }
               			}  
               		})
                }
            }
        }
	});
	avalon.scan(document.body);
});
