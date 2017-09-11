var page=1; //页数
var size=3; //条数
var url='QueryPendingRepaymentInfoServlet.do';

require(['avalon', 'jquery', 'util', 'Const', 'layer','iscroll','myiscroll','common'], function(avalon, $, util, Const) {	
	
	var vm=avalon.define({
		$id: 'installmentBill3',
		arrList:[],
		pullUpAction:function(){
			
		}
	});
	
	//默认加载 第一页
	util.post({
		url: url,
		data:{
			page:1,
			size:size
		},
		success:function(res){
			vm.arrList=res.retObj;
		}
	});

	avalon.scan(document.body);

	//iscroll初始化
	var myScroll, generatedCount;
	var pullDownEl, pullDownOffset;
	var pullUpEl, pullUpOffset;
	
	setTimeout(function(){
		pullUpEl = document.getElementById('pullUp');
		pullUpOffset = pullUpEl.offsetHeight;

		myScroll = new iScroll('wrapper', {
		    useTransition: true,
		    onRefresh: function() {
		        if (pullUpEl.className.match('loading')) {
		            pullUpEl.className = '';
		            pullUpEl.querySelector('.pullUpLabel').innerHTML = 'Pull up to load more...';
		        }
		    },
		    onScrollMove: function() {
		        if (this.y < (this.maxScrollY - 5) && !pullUpEl.className.match('flip')) {
		            pullUpEl.className = 'flip';
		            pullUpEl.querySelector('.pullUpLabel').innerHTML = 'Release to refresh...';
		            this.maxScrollY = this.maxScrollY;
		        } else if (this.y > (this.maxScrollY + 5) && pullUpEl.className.match('flip')) {
		            pullUpEl.className = '';
		            pullUpEl.querySelector('.pullUpLabel').innerHTML = 'Pull up to load more...';
		            this.maxScrollY = pullUpOffset;
		        }
		    },
		    onScrollEnd: function() {
		        if (pullUpEl.className.match('flip')) {
		            pullUpEl.className = 'loading';
		            pullUpEl.querySelector('.pullUpLabel').innerHTML = 'Loading...';
	            	var el, li, i;
    			    generatedCount = 0;
    			    el = document.getElementById('thelist');
    			    util.post({
    			    	url: url,
    			    	data:{
    			    		page: ++page,
    			    		size: size      			    		
    			    	},
    			    	success:function(obj){
    			    		if(util.isEmpty(obj.retObj)){
    			    			$('#pullUp').html('已经是全部数据了，没有待加载的数据了。。。。');
    			    		}else{
        		       			var sizeTemp,num,fg;
        		       			if(obj.retObj.length<3){
        		       				sizeTemp=obj.retObj.length;
        		       			}else{
        		       				sizeTemp=3;
        		       			}
        		       			for(var i=0;i<sizeTemp;i++){
        		        		    var title,btnTextHtml;
        		        		    if(obj.retObj[i].applyTnrTyp=='D'||obj.retObj[i].applyTnrTyp=='d'){title="现金随借随还"}
        		        		    else{title="现金支用"}
        		        		    if(obj.retObj[i].outSts=="待提交"){btnTextHtml='<a href="javascript:void(0)" onclick="delOrder(\''+obj.retObj[i].orderNo+'\',this)" class="button payBlueBtn">取消订单</a><a href="javascript:void(0)" class="button payBlueBtn" onclick="resubmit(\''+obj.retObj[i].orderNo+'\')">继续提交</a>';}
        		        		    /*else if(obj.retObj[i].remainDays >=0){btnTextHtml='<a href="javascript:void(0)" onclick="topay(\''+obj.retObj[i].applSeq+'\')" class="button payBlueBtn">还款</a>';}*/
        		        		    else if(obj.retObj[i].outSts=="待还款"){btnTextHtml='<a href="javascript:void(0)" onclick="topay(\''+obj.retObj[i].applSeq+'\',\''+obj.retObj[i].outSts+'\')" class="button payBlueBtn">还款</a>';}
        			        		else if(obj.retObj[i].outSts=="审批中"||obj.retObj[i].outSts=="商户确认中"){btnTextHtml='<a href="javascript:void(0)" onclick="appprogress(\''+obj.retObj[i].applSeq+'\',\''+obj.retObj[i].outSts+'\')" class="button payBlueBtn">审批进度</a>';}
        			        		else if(obj.retObj[i].outSts=="商户退回"||obj.retObj[i].outSts=="审批退回"){btnTextHtml='<a href="javascript:void(0)" onclick="appprogress(\''+obj.retObj[i].applSeq+'\',\''+obj.retObj[i].outSts+'\')" class="button payBlueBtn">审批进度</a><a href="javascript:void(0)" onclick="modiSubmit(\''+obj.retObj[i].applSeq+'\',\''+obj.retObj[i].outSts+'\')" class="button payBlueBtn">修改提交</a>';}
        			        		/*else if(obj.retObj[i].remainDays <0){btnTextHtml='<a href="javascript:void(0)" onclick="topay(\''+obj.retObj[i].applSeq+'\')" class="button payBlueBtn">还款</a>';}*/
        			        		else if(obj.retObj[i].outSts=="已逾期"){btnTextHtml='<a href="javascript:void(0)" onclick="topay(\''+obj.retObj[i].applSeq+'\',\''+obj.retObj[i].outSts+'\')" class="button payBlueBtn">还款</a>';}
        		        		    else if(obj.retObj[i].outSts=="等待放款"){btnTextHtml='<a href="javascript:void(0)"  onclick="appprogress(\''+obj.retObj[i].applSeq+'\',\''+obj.retObj[i].outSts+'\')" class="button payBlueBtn">审批进度</a>';}
        			        		else if(obj.retObj[i].outSts=="放款审核中"){btnTextHtml='<a href="javascript:void(0)" onclick="appprogress(\''+obj.retObj[i].applSeq+'\',\''+obj.retObj[i].outSts+'\')" class="button payBlueBtn">审批进度</a>';}
        			        		else if(obj.retObj[i].outSts=="审批通过，等待放款"){btnTextHtml='<a href="javascript:void(0)" onclick="appprogress(\''+obj.retObj[i].applSeq+'\',\''+obj.retObj[i].outSts+'\')" class="button payBlueBtn">审批进度</a>';}
        			        		else if(obj.retObj[i].outSts=="已放款"){btnTextHtml='<a href="javascript:void(0)" onclick="appprogress(\''+obj.retObj[i].applSeq+'\',\''+obj.retObj[i].outSts+'\')" class="button payBlueBtn">审批进度</a>';}
        			        		else if(obj.retObj[i].outSts=="贷款被拒绝"){btnTextHtml='<a href="javascript:void(0)" onclick="appprogress(\''+obj.retObj[i].applSeq+'\',\''+obj.retObj[i].outSts+'\')" class="button payBlueBtn">审批进度</a>';}
        			        		else{btnTextHtml='';}
        		        		    if(obj.retObj[i].orderNo && obj.retObj[i].applSeq==undefined){num=obj.retObj[i].orderNo;fg=1;}
        		        		    else if(obj.retObj[i].applSeq){num=obj.retObj[i].applSeq;fg=2;}
        		        		    else{num="";}
        		        		    var rehtml='';
        		        		    if(obj.retObj[i].remainDays){
        		        		    	 rehtml='<li class="iscrollAdd"><div class="billListD"><div class="timeTag"><span class="time">'+obj.retObj[i].applyDt+'</span><span class="tag">'+obj.retObj[i].outSts+'</span></div><div class="billInfoD" onclick="loanOrdDetail(\''+num+'\',\''+fg+'\',\''+obj.retObj[i].outSts+'\')"><div class="div92"><div class="img"><img src="../mobile/themes/default/imgsb/installicon.png"/></div><div class="div92title"><span>'+title+'</span></div><div class="clear"></div></div></div><div class="installmentheji">'+i+'合计：&yen;<span class="blue028">'+obj.retObj[i].sybj+'</span></div><div class="botmLine"></div><div class="payBtnD">'+btnTextHtml+'<div class="clear"></div></div><div class="clear"></div></div></div></li>';	
        		        		    }
        		        		    else{
        		        		    	 rehtml='<li class="iscrollAdd"><div class="billListD"><div class="timeTag"><span class="time">'+obj.retObj[i].applyDt+'</span><span class="tag">'+obj.retObj[i].outSts+'</span></div><div class="billInfoD" onclick="loanOrdDetail(\''+num+'\',\''+fg+'\',\''+obj.retObj[i].outSts+'\')"><div class="div92"><div class="img"><img src="../mobile/themes/default/imgsb/installicon.png"/></div><div class="div92title"><span>'+title+'</span></div><div class="clear"></div></div></div><div class="installmentheji">'+i+'合计：&yen;<span class="blue028">'+obj.retObj[i].applyAmt+'</span></div><div class="botmLine"></div><div class="payBtnD">'+btnTextHtml+'<div class="clear"></div></div><div class="clear"></div></div></div></li>';
        		        		    }
        			        		
        			  	            $("#thelist").append(rehtml);
        			  	            myScroll.refresh(); // Remember to refresh when contents are loaded (ie: on ajax completion)
        			        	}
    			    		}
    			    	}
    			    });
		        }
		    }
		});

		setTimeout(function() {
		    document.getElementById('wrapper').style.left = '0';
		}, 800);
	}, 200);
	document.addEventListener('touchmove', function (e) { e.preventDefault(); }, false);



/*
	
	tk= util.gup('token');

	var page=1;
	var url,outSts="";
	var thisTabId=$(".buttons-tab > .active").attr("id");
	if(thisTabId=="tab1"){url="data/QueryLoanInfoServlet";}
	else if(thisTabId=="tab2"){url="data/QueryPendingLoanInfoServlet";}
	else if(thisTabId=="tab3"){url="data/QueryPendingRepaymentInfoServlet";}
	else if(thisTabId=="tab4"){url="data/QueryApplLoanInfoServlet";outSts="01"}
	else{url="";outSts=""}
	var vm=avalon.define({
		$id: 'oriTabs',
		orders:['1','2','3','4','5','6'],
		appprogressFn:function(seq,sts){
		    var data ={token:tk,applSeq:seq,outSts:sts};
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
		topayFn:function(seq,sts){
			var data ={token:tk,applSeq:seq,outSts:sts};
		    var url="data/SaveApplSeqInCacheServlet";
       	    ajax(data,url,function succ(obj){
	       		if(obj.code=="0000"){
	       			window.location.href="loanDetails.html?token="+tk;	
	       	      }
	       		else{
	    			layer.open({
	                       content: obj.message,
	                       btn: '我知道了'
	                 });
	    		}
        	});	
		},
		delOrderFn:function(seq,el){
			layer.open({
			    content: '您确定取消此条订单？',
			    btn: ['确定', '取消'],
			    yes: function(index){
			       layer.close(index);
			       var data ={token:tk,orderNo:seq};
				   var url="data/DeleteOrderInfoServlet";
		       	   ajax(data,url,function succ(obj){
			       		if(obj.code=="0000"){	
			       				vm.orders.remove(el);
			       				layer.open({
			                        content: "取消成功！",
			                        btn: '我知道了'
			                    });
			       	      }
			       		else{
	            			layer.open({
	                               content: obj.message,
	                               btn: '我知道了'
	                         });
	            		}
		        	});	
			    }
			  });	
		},
		resubmitFn:function(seq){		
			var data ={token:tk,orderNo:seq};
		    var url="data/OrderInfoApplyServlet";
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
		modiSubmitFn:function(seq,sts){
			var data ={token:tk,applSeq:seq,outSts:sts};
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
		loanDetailFn:function(seq,sts){
			var data ={token:tk,applSeq:seq,outSts:sts};
		    var url="data/SaveApplSeqInCacheServlet";
			    ajax(data,url,function succ(obj){
					if(obj.code=="0000"){
						window.location.href="loanDetails.html?token="+tk;
				      }
					else{
	        			layer.open({
	                           content: obj.message,
	                           btn: '我知道了'
	                     });
	        		}
			});
		},
		orderDetailFn:function(orderNum){
			var data ={token:tk,orderNo:orderNum};
		    var url="data/SaveOrderNoInCacheServlet";
			    ajax(data,url,function succ(obj){
					if(obj.code=="0000"){
						window.location.href="ordersDetails.html?token="+tk;
				      }
					else{
	        			layer.open({
	                           content: obj.message,
	                           btn: '我知道了'
	                     });
	        		}
			});
		},
		billJump:function(tab){
			if(tab=="tab1"){window.location.href="installmentBill.html?token="+tk;}
			else if(tab=="tab2"){window.location.href="installmentBill2.html?token="+tk;}
			else if(tab=="tab3"){window.location.href="installmentBill3.html?token="+tk;}
			else{window.location.href="installmentBill4.html?token="+tk;}		
		},
		pullUpAction:function(){
	           page++;
	           var data ={token:tk,page:page,size:3,outSts:outSts};
	       	   ajax(data,url,function succ(obj){
	       		if(obj.code=="0000"){
	       			var temp,num,fg;
	       			if(obj.retObj.length<3){temp=obj.retObj.length;}
	       			else{temp=3;}
	       			for(var i=0;i<temp;i++){
	        		    var title,btnTextHtml;
	        		    if(obj.retObj[i].applyTnrTyp=='D'||obj.retObj[i].applyTnrTyp=='d'){title="现金随借随还"}
	        		    else{title="现金支用"}
	        		    if(obj.retObj[i].outSts=="待提交"){btnTextHtml='<a href="javascript:void(0)" onclick="delOrder(\''+obj.retObj[i].orderNo+'\',this)" class="button payBlueBtn">取消订单</a><a href="javascript:void(0)" class="button payBlueBtn" onclick="resubmit(\''+obj.retObj[i].orderNo+'\')">继续提交</a>';}
	        		    /*else if(obj.retObj[i].remainDays >=0){btnTextHtml='<a href="javascript:void(0)" onclick="topay(\''+obj.retObj[i].applSeq+'\')" class="button payBlueBtn">还款</a>';}
	        		    else if(obj.retObj[i].outSts=="待还款"){btnTextHtml='<a href="javascript:void(0)" onclick="topay(\''+obj.retObj[i].applSeq+'\',\''+obj.retObj[i].outSts+'\')" class="button payBlueBtn">还款</a>';}
		        		else if(obj.retObj[i].outSts=="审批中"||obj.retObj[i].outSts=="商户确认中"){btnTextHtml='<a href="javascript:void(0)" onclick="appprogress(\''+obj.retObj[i].applSeq+'\',\''+obj.retObj[i].outSts+'\')" class="button payBlueBtn">审批进度</a>';}
		        		else if(obj.retObj[i].outSts=="商户退回"||obj.retObj[i].outSts=="审批退回"){btnTextHtml='<a href="javascript:void(0)" onclick="appprogress(\''+obj.retObj[i].applSeq+'\',\''+obj.retObj[i].outSts+'\')" class="button payBlueBtn">审批进度</a><a href="javascript:void(0)" onclick="modiSubmit(\''+obj.retObj[i].applSeq+'\',\''+obj.retObj[i].outSts+'\')" class="button payBlueBtn">修改提交</a>';}
		        		/*else if(obj.retObj[i].remainDays <0){btnTextHtml='<a href="javascript:void(0)" onclick="topay(\''+obj.retObj[i].applSeq+'\')" class="button payBlueBtn">还款</a>';}
		        		else if(obj.retObj[i].outSts=="已逾期"){btnTextHtml='<a href="javascript:void(0)" onclick="topay(\''+obj.retObj[i].applSeq+'\',\''+obj.retObj[i].outSts+'\')" class="button payBlueBtn">还款</a>';}
	        		    else if(obj.retObj[i].outSts=="等待放款"){btnTextHtml='<a href="javascript:void(0)"  onclick="appprogress(\''+obj.retObj[i].applSeq+'\',\''+obj.retObj[i].outSts+'\')" class="button payBlueBtn">审批进度</a>';}
		        		else if(obj.retObj[i].outSts=="放款审核中"){btnTextHtml='<a href="javascript:void(0)" onclick="appprogress(\''+obj.retObj[i].applSeq+'\',\''+obj.retObj[i].outSts+'\')" class="button payBlueBtn">审批进度</a>';}
		        		else if(obj.retObj[i].outSts=="审批通过，等待放款"){btnTextHtml='<a href="javascript:void(0)" onclick="appprogress(\''+obj.retObj[i].applSeq+'\',\''+obj.retObj[i].outSts+'\')" class="button payBlueBtn">审批进度</a>';}
		        		else if(obj.retObj[i].outSts=="已放款"){btnTextHtml='<a href="javascript:void(0)" onclick="appprogress(\''+obj.retObj[i].applSeq+'\',\''+obj.retObj[i].outSts+'\')" class="button payBlueBtn">审批进度</a>';}
		        		else if(obj.retObj[i].outSts=="贷款被拒绝"){btnTextHtml='<a href="javascript:void(0)" onclick="appprogress(\''+obj.retObj[i].applSeq+'\',\''+obj.retObj[i].outSts+'\')" class="button payBlueBtn">审批进度</a>';}
		        		else{btnTextHtml='';}
	        		    if(obj.retObj[i].orderNo && obj.retObj[i].applSeq==undefined){num=obj.retObj[i].orderNo;fg=1;}
	        		    else if(obj.retObj[i].applSeq){num=obj.retObj[i].applSeq;fg=2;}
	        		    else{num="";}
	        		    var rehtml
	        		    if(obj.retObj[i].remainDays){
	        		    	 rehtml='<li><div class="billListD"><div class="timeTag"><span class="time">'+obj.retObj[i].applyDt+'</span><span class="tag">'+obj.retObj[i].outSts+'</span></div><div class="billInfoD" onclick="loanOrdDetail(\''+num+'\',\''+fg+'\',\''+obj.retObj[i].outSts+'\')"><div class="div92"><div class="img"><img src="../mobile/themes/default/imgsb/installicon.png"/></div><div class="div92title"><span>'+title+'</span></div><div class="clear"></div></div></div><div class="installmentheji">合计：&yen;<span class="blue028">'+obj.retObj[i].sybj+'</span></div><div class="botmLine"></div><div class="payBtnD">'+btnTextHtml+'<div class="clear"></div></div><div class="clear"></div></div></div></li>';	
	        		    }
	        		    else{
	        		    	 rehtml='<li><div class="billListD"><div class="timeTag"><span class="time">'+obj.retObj[i].applyDt+'</span><span class="tag">'+obj.retObj[i].outSts+'</span></div><div class="billInfoD" onclick="loanOrdDetail(\''+num+'\',\''+fg+'\',\''+obj.retObj[i].outSts+'\')"><div class="div92"><div class="img"><img src="../mobile/themes/default/imgsb/installicon.png"/></div><div class="div92title"><span>'+title+'</span></div><div class="clear"></div></div></div><div class="installmentheji">合计：&yen;<span class="blue028">'+obj.retObj[i].applyAmt+'</span></div><div class="botmLine"></div><div class="payBtnD">'+btnTextHtml+'<div class="clear"></div></div><div class="clear"></div></div></div></li>';
	        		    }
		        		
		  	            $("#thelist").append(rehtml);
		        	}	       				
	       		}
	       		else{
	    			layer.open({
	                       content: obj.message,
	                       btn: '我知道了'
	                 });
	    		}
	        	});		
	            myScroll.refresh();	    
		}
	});	
	
	//默认加载“全部”
	/*util.loading();
	var data ={token:tk,page:page,size:6,outSts:outSts};
 	ajax(data,url,function succ(obj){
 		util.loading('close');
		if(obj.code=="0000"){
			vm.orders=obj.retObj;	
		}
		else{
			vm.orders=[];
			layer.open({
                   content: obj.message,
                   btn: '我知道了'
             });
		}
 	});	*/
 	//默认加载“全部”结束
   //	avalon.scan(document.body);
  
});
/*//加载数据的审批进度按钮
function appprogress(thisapplSeq,sts){
	var data ={token:tk,applSeq:thisapplSeq,outSts:sts};
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
}
//还款按钮
function topay(thisapplSeq,sts){
	var data ={token:tk,applSeq:thisapplSeq,outSts:sts};
    var url="data/SaveApplSeqInCacheServlet";
	    ajax(data,url,function succ(obj){
		if(obj.code=="0000"){
			window.location.href="loanDetails.html?token="+tk;	
	      }
		else{
			layer.open({
                   content: obj.message,
                   btn: '我知道了'
             });
		}
	});	
}
//删除订单
function delOrder(thisapplSeq,thisobj){
	layer.open({
	    content: '您确定取消此条订单？',
	    btn: ['确定', '取消'],
	    yes: function(index){
	       layer.close(index);
       		var data ={token:tk,orderNo:thisapplSeq};
       	    var url="data/DeleteOrderInfoServlet";
       		ajax(data,url,function succ(obj){
       			if(obj.code=="0000"){
   					$(thisobj).parent().parent().parent().remove();
   					layer.open({
                        content: "取消成功！",
                        btn: '我知道了'
                    });
       		      }
       			else{
	    			layer.open({
	                       content: obj.message,
	                       btn: '我知道了'
	                 });
	    		}
       		});	
	    }
	  });	
}
//继续提交
function resubmit(thisapplSeq){
        var data ={token:tk,orderNo:thisapplSeq};
        var url="data/OrderInfoApplyServlet";
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
}
//修改提交
function modiSubmit(thisapplSeq,sts){
	var data ={token:tk,applSeq:thisapplSeq,outSts:sts};
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
}
//点击列表进入贷款订单详情页
function loanOrdDetail(thisapplSeq,fg,sts){
	if(fg==1){//订单
		var data ={token:tk,orderNo:thisapplSeq};
		var url="data/SaveOrderNoInCacheServlet";
		ajax(data,url,function succ(obj){
			if(obj.code=="0000"){
				window.location.href="ordersDetails.html?token="+tk;
		      }
			else{
    			layer.open({
                       content: obj.message,
                       btn: '我知道了'
                 });
    		}
		});
		}
	else{//贷款   
		var data ={token:tk,applSeq:thisapplSeq,outSts:sts};
		var url="data/SaveApplSeqInCacheServlet";
		ajax(data,url,function succ(obj){
			if(obj.code=="0000"){
				window.location.href="loanDetails.html?token="+tk;
		      }
			else{
    			layer.open({
                       content: obj.message,
                       btn: '我知道了'
                 });
    		}
		});
	}
}*/


/*
function pullDownAction() {
    setTimeout(function() { // <-- Simulate network congestion, remove setTimeout from production!
        var el, li, i;
        generatedCount = 0;
        el = document.getElementById('thelist');

        for (i = 0; i < 3; i++) {
            li = document.createElement('li');
            li.innerText = 'Generated row ' + (++generatedCount);
            el.insertBefore(li, el.childNodes[0]);
        }

        myScroll.refresh(); // Remember to refresh when contents are loaded (ie: on ajax completion)
    }, 1000); // <-- Simulate network congestion, remove setTimeout from production!
}
*/
function pullUpAction() {
    
}

function loaded() {

}
