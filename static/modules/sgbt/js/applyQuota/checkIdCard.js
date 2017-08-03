require(['avalon', 'jquery', 'util', 'Const', 'layer','msPhotoUgrade'], function(avalon, $, util, Const) {
	
	var tk = util.gup('token'); //获取token

	var vm=avalon.redefine({
		$id: "checkIdCard",
		lengths: Const.lengths,
		config: {
			//imgPath: '../../images/camera.png',
			file_id: 'identityCard',
			file_title: '身份证正面',
			containerId: 'uploadPhoto',
			url: 'ocrIdentity?token=7a22536b-9096-4bf5-94c6-079d92bf48b0',
			path: ''
		},
		onFileUploaded: function(obj) {
			$('#cover').hide();
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
                	if (util.isEmpty($('.address-r p').text())) {
    	                util.alert('#noAddress');
    				}else{
    					util.post({
    						url: 'savaIdentityInfoServlet.do',
    						data: {
    							token: tk, 
    							name: vm.cards.name
    						},
    						success: function() {
    							util.cache({
    								name: vm.cards.name,
    							});
    							util.redirect('regist/reg_checkIdCardB.html', undefined, false);
    						}
    					});
                	}
                }
            }
        }
	});
	avalon.scan(document.body);
});
