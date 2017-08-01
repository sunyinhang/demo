/*
* @Author: 程慧梅
* @Date:   2017-07-20 14:38:28
* @Last Modified by:   程慧梅
* @Last Modified time: 2017-07-24 17:44:25
*/

'use strict';
require(['avalon', 'jquery', 'util', 'Const', 'layer'], function(avalon, $, util, Const) {
	var vm=avalon.redefine({
		$id: "btInstalment",
        lengths: Const.lengths,
        definitionFn: function(){
            util.alert('"顺逛白条"是顺逛商城联合海尔消费金融为其家电购买用户提供的一款"先消费，后付款"的在线支付产品服务');
        },
        payFn:function(){
            layer.open({
                type: 1
                ,content: $('#enterPassword').html()
                ,anim: 'scale'
                ,closeBtn: 2
                ,style: 'position:absolute; left:50%; top:50%; margin-top:-115px; margin-left:-42.5%; width:85%; height:230px; border: none; -webkit-animation-duration: .5s; animation-duration: .5s;'
            });
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

                }
            }
        }
	});
    //确认支付
    $(document).on('click', '#confirmPay', function(event) {
        var regpayPwd=/^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,20}$/;
        var obj=$('.layui-m-layercont .pwd-text');
        if(!regpayPwd.test(obj.val())){
            util.alert('支付密码以6-20位字母和数字组成');
        }else{
            /*util.post({
                url: '',
                data:'',
                success: function(res) {
                
                }
            });*/
            util.redirect('paySuccess.html');
            layer.closeAll();
        }

    });
    //忘记密码
    $(document).on('click','#getPayPwd',function(){
        util.redirect('getPayPsd/getPayPsdWay.html');
        layer.closeAll();

    });

    avalon.scan(document.body);
    
})
