/*
* @Author: 程慧梅
* @Date:   2017-07-20 14:38:28
* @Last Modified by:   程慧梅
* @Last Modified time: 2017-07-25 09:00:02
*/

'use strict';
require(['avalon', 'jquery', 'util', 'Const', 'layer'], function(avalon, $, util, Const) {
	var vm=avalon.redefine({
		$id: "payFail",
        lengths: Const.lengths,
        
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

                }
            }
        }
	});
    
    avalon.scan(document.body);
    
})
