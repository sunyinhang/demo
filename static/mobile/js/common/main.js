require(['avalon', 'zepto', 'util'], function(avalon, $, util, Const) {
    util.init();
    $(document).on('click', '[data-redirect][data-target][data-title]', function() {	
    	if(true){
            document.title = $(this).attr('data-title');
            util.replace($(this).closest($(this).attr('data-target')), $(this).attr('data-redirect'));
    	}

    });
    
    console.log(util.simpleEncrypt('13793231906中国'))
    console.log(util.simpleDecrypt('zs/IG8xSzGTGzMnGR80azkI='))
});