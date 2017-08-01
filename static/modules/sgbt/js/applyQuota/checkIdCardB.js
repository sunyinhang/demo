require(['avalon', 'jquery', 'util', 'Const', 'layer','msPhotoUgrade'], function(avalon, $, util, Const) {
	
	var tk = util.gup('token'); //获取token

	var vm=avalon.redefine({
		$id: "checkIdCardB",
		config: {
			// imgPath: '../mobile/themes/default/imgsb/camera.png',
			file_id: 'identityCard',
			file_title: '身份证反面',
            containerId: 'uploadPhoto',
			url: 'ocrIdentifyServlet.do?token='+tk,
			path: ''
		},
		onFileUploaded: function(obj) {
			$('#cover').hide();;
 			//vm.cards = util.mix(vm.cards.$model, obj.retObj.cards);
			vm.cards = obj.retObj.cards;
		},
		checkIfPhoto: function(){
			if (util.isEmpty(vm.config.path)) {
            	util.alert('#noScan');
			}
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
                }else {
                	util.redirect("regist/reg_tiedBnkCrd.html?token="+tk);
            	}
            }
        }
	});

	avalon.scan(document.body);
});
