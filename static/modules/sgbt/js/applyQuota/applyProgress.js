require(['avalon', 'jquery', 'util', 'Const', 'layer'], function(avalon, $, util, Const) {

	var vm=avalon.redefine({
		$id: "applyProgres",
		approvalProgressArray:[],
		lastArray: []
	});
	//初始化
	var data={};
	var url="data/QueryApprovalProcessInfoServlet.do";
	util.post({
		url: url,
		data: data,
		success:function(obj){
			vm.approvalProgressArray=obj.retObj;
			if (vm.approvalProgressArray && vm.approvalProgressArray.length > 0) {
				vm.lastArray = vm.approvalProgressArray[vm.approvalProgressArray.length - 1];
			}
			vm.approvalProgressArray=vm.approvalProgressArray.reverse();
		}
	});
	//初始化 end
	avalon.scan(document.body);
});