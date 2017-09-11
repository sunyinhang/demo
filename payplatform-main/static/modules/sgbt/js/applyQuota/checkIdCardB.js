require(['avalon', 'jquery', 'util', 'Const', 'layer','msPhotoUgrade'], function(avalon, $, util, Const) {
	
	var tk = util.gup('token'); //获取token

	var vm=avalon.redefine({
		$id: "checkIdCardB",
		config: {
			// imgPath: '../mobile/themes/default/imgsb/camera.png',
			file_id: 'identityCard',
			file_title: '身份证反面',
            containerId: 'uploadPhoto',
			url: 'ocrIdentity?token=7a22536b-9096-4bf5-94c6-079d92bf48b0',
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
					util.redirect('applyQuota/tiedBnkCrd.html', undefined, false);
            	}
            }
        }
	});

	avalon.scan(document.body);
});
