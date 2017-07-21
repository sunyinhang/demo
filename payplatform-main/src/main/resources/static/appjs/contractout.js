//<script type="text/javascript">
$(function () {
    (function ($) {
        $.getUrlParam = function (name) {
            var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
            var r = window.location.search.substr(1).match(reg);
            if (r != null)
                return unescape(r[2]);
            return null;
        }
    })(jQuery);
    var code = $.getUrlParam('code');
    var applseq = "";
    $.ajax({
        type: "GET",
        url: "/app/appserver/ca/hdjrforward",
        data: {
            "code": code
        },
        dataType: "json",
        success: function (data) {
            applseq = data.body.applseq;
            $.ajax({
                type: "GET",
                url: "/app/appserver/getContractInfoFromCmis",
                data: {
                    "applseq": applseq
                },
                dataType: "json",
                success: function (data) {
                    console.info("data=" + data);
//				if(""==data.body.contractNo || null==data.body.contractNo){
//					$("#contractNo").text("");
//				}else{
//					$("#contractNo").text(data.body.contractNo);
//				}
//				if(""==data.body.custName || null==data.body.custName){
//					$("#custName").text("");
//				}else{
//					$("#custName").text(data.body.custName);
//				}
//				if(""==data.body.identifyNo || null==data.body.identifyNo){
//					$("#identifyNo").text("");
//				}else{
//					$("#identifyNo").text(data.body.identifyNo);
//				}
//				if(""==data.body.custAddress || null==data.body.custAddress){
//					$("#custAddress").text("");
//				}else{
//					$("#custAddress").text(data.body.custAddress);
//				}
//				if(""==data.body.custMobile || null==data.body.custMobile){
//					$("#custMobile").text("");
//				}else{
//					$("#custMobile").text(data.body.custMobile);
//				}
//				if(""==data.body.email || null==data.body.email){
//					$("#email").text("");
//				}else{
//					$('#email').text(data.body.email);
//				}
//				if(""==data.body.applyAmtSmall || null==data.body.applyAmtSmall){
//					$("#applyAmtSmall").text("");
//				}else{
//					$("#applyAmtSmall").text(data.body.applyAmtSmall);
//				}
//				if(""==data.body.applyAmtBig || null==data.body.applyAmtBig){
//					$("#applyAmtBig").text("");
//				}else{
//					$("#applyAmtBig").text(data.body.applyAmtBig);
//				}
//				if(""==data.body.applyTnr || null==data.body.applyTnr){
//					$("#applyTnr").text("");
//				}else{
//					$("#applyTnr").text(data.body.applyTnr);
//				}
//				if(""==data.body.applyTnrTyp || null==data.body.applyTnrTyp){
//					$("#applyTnrTyp").text("");
//				}else{
//					$("#applyTnrTyp").text(data.body.applyTnrTyp);
//				}
//				if(""==data.body.applAcNam || null==data.body.applAcNam){
//					$("#applAcNam").text("");
//				}else{
//					$("#applAcNam").text(data.body.applAcNam);
//				}
//				if(""==data.body.accBankCde || null==data.body.accBankCde){
//					$("#accBankCde").text("");
//				}else{
//					$("#accBankCde").text(data.body.accBankCde);
//				}
//				if(""==data.body.accBankName || null==data.body.accBankName){
//					$("#accBankName").text("");
//				}else{
//					$("#accBankName").text(data.body.accBankName);
//				}
//				if(""==data.body.applCardNo || null==data.body.applCardNo){
//					$("#applCardNo").text("");
//				}else{
//					$("#applCardNo").text(data.body.applCardNo);
//				}
//				if(""==data.body.mtdCde || null==data.body.mtdCde){
//					$("#mtdCde").text("");
//				}else{
//					$("#mtdCde").text(data.body.mtdCde);
//				}
//				if(""==data.body.purpose || null==data.body.purpose){
//					$("#purpose").text("");
//				}else{
//					$("#purpose").text(data.body.purpose);
//				}
//				if(""==data.body.repayApplAcNam || null==data.body.repayApplAcNam){
//					$("#repayApplAcNam").text("");
//				}else{
//					$("#repayApplAcNam").text(data.body.repayApplAcNam);
//				}
//				if(""==data.body.repayAccBankName || null==data.body.repayAccBankName){
//					$("#repayAccBankName").text("");
//				}else{
//					$("#repayAccBankName").text(data.body.repayAccBankName);
//				}
//				if(""==data.body.repayApplCardNo || null==data.body.repayApplCardNo){
//					$("#repayApplCardNo").text("");
//				}else{
//					$("#repayApplCardNo").text(data.body.repayApplCardNo);
//				}
//				if(""==data.body.accName || null==data.body.accName){
//					$("#accName").text("");
//				}else{
//					$("#accName").text(data.body.accName);
//				}
//				if(""==data.body.lenderName || null==data.body.lenderName){
//					$("#lenderName").text("");
//				}else{
//					$("#lenderName").text(data.body.lenderName);
//				}
//				if(""==data.body.custMobile || null==data.body.custMobile){
//					$("#custMobile").text("");
//				}else{
//					$("#custMobile").text(data.body.custMobile);
//				}
//				$("span [name=date]").text(data.body.date);


                    $("#contractNo").text(data.body.contractNo);
                    $("#custName").text(data.body.custName);
                    $("#identifyNo").text(data.body.identifyNo);
                    $("#custAddress").text(data.body.custAddress);
                    $("#expressAddress").text(data.body.custAddress);
                    $("#jiaAddress").text(data.body.custAddress);
                    $("#custMobile").text(data.body.custMobile);
                    $('#email').text(data.body.email);
                    $('#liveZip').text(data.body.email);
                    $("#applyAmtSmall").text(data.body.applyAmtSmall);
                    $("#applyAmtBig").text(data.body.applyAmtBig);
                    $("#fstPaySmall").text(data.body.fstPaySmall);
                    $("#fstPayBig").text(data.body.fstPayBig);
                    $("#applyTnr").text(data.body.applyTnr);
//				$("#applyTnrTyp").text(data.body.applyTnrTyp);
                    $("#applyType").text(data.body.mtdCde);
                    $("#applyTnrTyp").text(data.body.mtdCde);
                    $("#rate").text(data.body.rate);
                    $("#applAcNam").text(data.body.applAcNam);
                    $("#accBankCde").text(data.body.accBankCde);
                    $("#accBankName").text(data.body.accBankName);
                    $("#applCardNo").text(data.body.applCardNo);
                    $("#mtdCde").text(data.body.mtdCde);
                    $("#cooprName").text(data.body.cooprName);
                    $("#cooprCityName").text(data.body.cooprCityName);
                    $("#applyManagerRate").text(data.body.applyManagerRate);
                    $("#purpose").text(data.body.purpose);
                    $("#repayApplAcNam").text(data.body.repayApplAcNam);
                    $("#repayAccBankName").text(data.body.repayAccBankName);
                    $("#repayApplCardNo").text(data.body.repayApplCardNo);
                    $("#accName").text(data.body.accName);
                    $("#lenderName").text(data.body.lenderName);
                    $("#custMobile").text(data.body.custMobile);
                    $("#accAcBchCde").text(data.body.accAcBchCde);
                    $("#feeRate").text(data.body.feeRate);
                    $("#crdNorAvailAmt").text(data.body.crdNorAvailAmt);
                    $("#bigCrdNorAvailAmt").text(data.body.bigCrdNorAvailAmt);
                    $("span[name='date']").each(function () {
                        $(this).text(data.body.date);
                    });
                    $("span[name='depart']").each(function () {
                        $(this).text(data.body.cooprCityName);
                    });
                    $("span[name='year']").each(function () {
                        $(this).text(data.body.year);
                    });
                    $("span[name='month']").each(function () {
                        $(this).text(data.body.month);
                    });
                    $("span[name='day']").each(function () {
                        $(this).text(data.body.day);
                    });
                }
            });


        }
    });

    //var applseq = $.getUrlParam('applseq'); //获取url的流水号

});
//</script>
	
	
	
	
	
