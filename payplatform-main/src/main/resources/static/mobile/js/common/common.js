var function_id;//功能权限全局变量
//var queryUrl = "http://127.0.0.1:8080/PaymentPlatform/servlet/";
//var queryUrl = "http://10.164.23.188:80/ChannelPlatform/servlet/";
var queryUrl = "http://"+window.location.host+"/PaymentPlatform/servlet/";
var htmlUrl = "http://"+window.location.host+"/PaymentPlatform/page/";

//jquery调用ajax
function ajax(data,url,callbackFunction){

	 var jqueryUrl=queryUrl+url+".do?r=" + Math.random();
	 $.ajax(
			    {
			        url: jqueryUrl,
			        dataType: "json",
			        type:"post",
			        async:true,
			        data:data,
			        scriptCharset: "UTF-8",
			        success:function(result) {
			        	var obj;
						if (result) {
							obj = eval(result);
					    }
						
						callbackFunction(obj);
						
					},
					error: function (a,b,c) {
						callbackFunction();
				    }
				}
	);
}




//jquery上传调用ajax
function fileUploadajax(data,formdata,url,callbackFunction){
	 var elementIds=["flag"]; //flag为id、name属性名
	 var jqueryUrl=queryUrl+url+".do?r=" + Math.random();
	 debugger;
	 $.ajaxFileUpload({
	        url: jqueryUrl, 
	        type: 'post',
	        secureuri: false, //一般设置为false
	        fileElementId:data, // 上传文件的id、name属性名
	        data:formdata,
	        dataType: 'json', //返回值类型，一般设置为json、application/json
	        // data: {"operator" : $("#operator").val(),storeNum : $("#storeNum").val()},
	        elementIds: "", //传递参数到服务器
	        success: function(data, status){
	        	var obj;
				if (data) {
					obj = eval(data);
			    }
	        	callbackFunction(obj);
	        },
	        error: function(data, status, e){ 
	        	alert(data);
	        	alert(e);
	        	callbackFunction();
	        }
	    });
}


function analyzeJson(str,obj){
	var objstr = str.split(",");
	var result=[];
	var num=0;
	if (obj!=null) {
		for (var int = 0; int < obj.length; int++) {
			var tem = [];
			for (var int2 = 0; int2 < objstr.length; int2++) {
				tem[int2] = obj[int][objstr[int2]];
			}
			if(tem.length==0)
			{
				continue;
			}else{
				result[num] = tem;
				num++;
			}
			
		}
	}
	return result;
}


$.extend({
	  getUrlVars: function(){
	    var vars = [], hash;
	    var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
	    for(var i = 0; i < hashes.length; i++)
	    {
	      hash = hashes[i].split('=');
	      vars.push(hash[0]);
	      vars[hash[0]] = hash[1];
	    }
	    return vars;
	  },
	  getUrlVar: function(name){
	    return $.getUrlVars()[name];
	  }
	});

function getUrlParam(name){
	var reg = new RegExp("(^|&)"+ name +"=([^&]*)(&|$)");  
    var r = window.location.search.substr(1).match(reg);  
    if (r!=null) return unescape(r[2]); return null; 
}