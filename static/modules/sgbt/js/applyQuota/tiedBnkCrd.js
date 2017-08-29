require(['avalon', 'jquery', 'util', 'Const', 'layer','mobileAreaSelect'], function(avalon, $, util, Const) {

	//获取客户姓名
	var param = util.cacheServer('name');
	
	var tk = util.gup('token'); //获取token
	
	//开户省市
	 $('#account').mobileAreaSelect({
		 code: 'areaCode',
		 name: 'areaName',
		 data: function(code) {
			 var result = [];
			 var data = avalon.mix({}, {token: 'f294c5ad-1b63-4340-8ddb-7de9d0366ed7',channel:'1',channelNo:'43'}, !util.isEmpty(code) ? {areaCode: code} : '');
			 util.get({
				 url: 'getArea',
				 data: data,
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
		$id: "tiedBnkCrd",
		bankCards: {
			cardholder: param && param.name,
			agreement: true
		},
		// 显示协议
		showTreatyContent: false,
		showTreatyCover: false,
		hideTreatyContent: function() {
			vm.showTreatyContent = false;
			vm.showTreatyCover = true;
		},
		getBankType: function(event){
			//util.getBankName(event.currentTarget);
			if (util.isEmpty(vm.bankCards.cardnumber)) {
                util.alert('#cardMatch');
			}else{
				util.post({
					url: "sendMsg",
					type: 'post',
					data: {
						cardNo: vm.bankCards.cardnumber,
						token: tk
					},
					success:function(res){
						vm.bankCards.cardtype=res.retObj.bankName + ' ' +res.retObj.cardType;
					}
				});
			}
		},
		getBankTypeInfo:function(){
			if (util.isEmpty(vm.bankCards.cardnumber)) {
                util.alert('#cardMatch');
			}
		},
		validateCodeFn: function(event){
			if (!util.isEmpty(vm.bankCards.mobile)) {
				var msgMobile={
					userId: vm.bankCards.mobile,
					token:tk
				};
				util.countDown('sendMsg', msgMobile, 60, $(event.currentTarget));
			}
			else{
				 util.alert('请输入预留手机号码！');
			}	
		},
		viewAgreementFn:function(){
           	util.post({
				url: "treatyShowServlet.do",
				type: 'post',
				data: {
					flag:"person"
				},
				success:function(obj){
					/*debugger
					console.log(obj);
					//$("#load").show();
					$("#load").load("https://testpm.haiercash.com/app/appserver/register?orderNo=238b905a-cd0d-4e8e-9a89-ec1f9a273612&custName=6LW15YWI6bKB",function(responseTxt,statusTxt,xhr){
					    if(statusTxt=="success")
					       console.log($("#load").html())
					       //页面层
						  layer.open({
						    type: 1
						    ,content: $("#load").html()
						    ,anim: 'up'
						    ,btn: '我知道了'
						    ,style: 'position:absolute; top:0px; bottom:0; left:0; width: 100%; min-height: 100%; padding:0px; border:none; overflowauto;'
						  });
					    if(statusTxt=="error")
					      alert("Error: "+xhr.status+": "+xhr.statusText);
					});	*/
						
						
						
						
						
					if (obj.retObj) {
						$('#treatyContainer').attr('src', "http://testpm.haiercash.com/app/appserver/register?orderNo=238b905a-cd0d-4e8e-9a89-ec1f9a273612&custName=6LW15YWI6bKB").show();
						vm.showTreatyCover = false;
						vm.showTreatyContent = true;
					}
				}
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
                    var addrCodeArr = $('#accountCode').val().split(',');
            		if (addrCodeArr[1] == 0) {
                        util.alert('请选择开户市/区');
            		}
            		else if (addrCodeArr[2] == 0) {
                        util.alert('请选择开户区/县');
            		} else {
            			var data=avalon.mix({},{token: tk},vm.bankCards.$model);
	                   	util.post({
	 						url: 'realNaAuthenticationServlert.do',
	 						data: data,
	 						success:function(res) {
	 							util.redirect("getCash.html");
	 							util.cache({
	 								phone:vm.bankCards.mobile
	 							});
	 						}
	 					});
            		}
                }
            }
        }

	});

	avalon.scan(document.body);
});