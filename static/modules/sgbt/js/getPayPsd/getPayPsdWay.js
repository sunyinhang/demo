
require(['avalon', 'jquery', 'util', 'Const', 'layer'], function(avalon, $, util, Const) {


    vm=avalon.redefine({
		$id: "getPayPsdWay",
		nextFn: function(event){
			this.validate.onManual();
		},
		validate: {			
            onSuccess: function (reasons) {
            },
            onError: function (reasons) {
            },
            onValidateAll: function (reasons) {
            }            
        }
	});
	avalon.scan(document.body);
});
