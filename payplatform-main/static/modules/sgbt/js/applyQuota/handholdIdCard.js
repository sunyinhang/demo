require(['avalon', 'jquery', 'util', 'Const', 'layer', 'msPhotoUgrade'], function(avalon, $, util) {

	//身份确认
	var vm = avalon.redefine({
		$id: "handheldIdCard",
		config: {
			defaultImage: '../mobile/themes/default/imgsb/tdzl.png',
			file_id: 'identityCard',
			file_title: '手持身份证正面自拍',
			containerId: 'uploadPhoto',
			// url: 'data/GetOCRIdentityServlet.do',
			path: ''
		},
		nextFn: function(event) {
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
        			util.upload({
        				url: 'attachUploadPersonServlet.do',
        				data: new FormData(document.getElementById('handForm')),
        				success: function(res) {
        					util.redirect('applyQuota/setPayPsd.html');
        				}
        			});
                }
            }
        }
	});
});