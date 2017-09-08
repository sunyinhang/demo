require(['avalon', 'jquery', 'util', 'Const', 'layer'], function(avalon, $, util, Const) {
	
	var vm=avalon.redefine({
		$id: "loanDetails",
		applyTnrTyp:'',//按日 "D" rlx 
		orderStatus:'',
		applSeq:'',
		repayAccBankName :'',
		repayApplCardNo :'',
		applyDt:'',
		goodsName:'',
		apprvAmt:'',
		apprvTotal:'',
		totfee:'',
		setlPrcpAmt:'',
		repayPrcpAmt:'',
		outSts:'',
		remainDays:'',
		payListArr:'',
		allTerms:'',
		ze:'',
		listchek:function(e){
			layer.open({
                content: "此贷款仅支持全部提前还款，当前全部应还"+vm.ze+"，包括本金、提前还款手续费、息费等",
                btn: '我知道了',
                yes: function(index){
                    layer.close(index); //如果设定了yes回调，需进行手工关闭
                    var data ={};
                    var url="data/QueryApplAmtBySeqAndOrederNoServlet";
                 	ajax(data,url,function succ(obj){
                		if(obj.code=="0000"&&obj.retObj!=null){
                			vm.ze=obj.retObj.ze;
                		}
                		else{
                			layer.open({
                                   content: obj.message,
                                   btn: '我知道了'
                             });
                		}
                 	})
                  }
             });
			$(".selectSpan").removeClass('graySelect');  
			$(".selectSpan").addClass('greenSelect');
			$("#totalSelect").removeClass('totgraySelect');  
			$("#totalSelect").addClass('totgreenSelect');
			
		},
		detailsFn: function(){
			window.location.href="repaymentDetailed.html?token="+tk;
		},
		approvalProgress:function(){
	 		var data ={token:tk,applSeq:vm.applSeq,outSts:vm.outSts};
	 	    var urlapp="data/SaveApplSeqInCacheServlet";
	 		ajax(data,urlapp,function succ(obj){
	 			if(obj.code=="0000"){
	 					window.location.href="approProgres2.html?token="+tk;	
	 				}
	 			else{
	 				layer.open({
	 		               content: obj.message,
	 		               btn: '我知道了'
	 		         });
	 			}
	 		});	
		},
		modiSubmit:function(){
			var data ={applSeq:vm.applSeq,outSts:vm.outSts};
		    var url="data/SaveApplSeqInCacheServlet";
       	    ajax(data,url,function succ(obj){
	       		if(obj.code=="0000"){
	       			 window.location.href="eachApply.html?token="+tk;
	       	      }
	       		else{
	    			layer.open({
	                       content: obj.message,
	                       btn: '我知道了'
	                 });
	    		}
        	});
		},
		topay:function(){
			layer.open({
                content: "您可下载嗨付app进行主动还款",
                btn: '下载嗨付APP',
                yes: function(index){
 			       layer.close(index);
 			       window.location.href="http://app.haiercash.com/appdeploy/haiercustomer/appdownload.html?token="+tk ;//下载app
 			    }
             });
		}
	 })
	var data ={};
	var url="data/QueryLoanDetailInfoServlet";
 	ajax(data,url,function succ(obj){
		if(obj.code=="0000"){
			vm.remainDays=obj.retObj.remainDays;
			vm.outSts=obj.retObj.outSts;
			vm.applSeq=obj.retObj.applSeq;
			vm.repayAccBankName=obj.retObj.repayAccBankName;
			vm.repayApplCardNo="****"+obj.retObj.repayApplCardNo.substr(-4,4);
			vm.applyDt=obj.retObj.applyDt;
			vm.goodsName=obj.retObj.goodsName;
			vm.apprvAmt=obj.retObj.apprvAmt;
			vm.applyTnrTyp=obj.retObj.applyTnrTyp;
			if(obj.retObj.applyTnrTyp == 'D'||obj.retObj.applyTnrTyp == 'd'){vm.totfee="每日"+obj.retObj.rlx+"元";}
			else{vm.totfee=obj.retObj.totfee;}
			if(obj.retObj.applyTnrTyp == 'D'||obj.retObj.applyTnrTyp == 'd'){vm.apprvTotal="本金+"+obj.retObj.rlx+"元/日";}
			else{vm.apprvTotal=obj.retObj.apprvTotal;}		
			vm.setlPrcpAmt=obj.retObj.setlPrcpAmt;
			vm.repayPrcpAmt=obj.retObj.repayPrcpAmt;
			if(obj.retObj.applyTnrTyp == 'D'||obj.retObj.applyTnrTyp == 'd'){vm.goodsName="现金随借随还";}
			else{vm.goodsName="现金支用";}
		}
		else{
			layer.open({
                   content: obj.message,
                   btn: '我知道了'
             });
		}
 	});	
 	var url="data/QueryApplListBySeqServlet";
 	ajax(data,url,function succ(obj){
		if(obj.code=="0000"&&obj!=undefined){
			vm.payListArr=obj.retObj;
			vm.allTerms=obj.retObj.length;
		}
		else{
			layer.open({
                   content: obj.message,
                   btn: '我知道了'
             });
		}
 	})

   	avalon.scan(document.body);
})