package com.haiercash.appserver.web;

import com.haiercash.common.data.AppParam;
import com.haiercash.common.data.AppParamRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/app/appserver/appmanage/param")
public class AppParamController extends BaseController {
    public Log logger = LogFactory.getLog(getClass());
    
    @Autowired
    private AppParamRepository appParamRepository;
    
    public AppParamController() {
        super("92");
    }
    
    @RequestMapping(value = "/selectByParams", method = RequestMethod.GET)
    public Map<String, Object> selectByParams(@RequestParam String sysTyp) {
        List<AppParam> list=appParamRepository.findBySysTyp(sysTyp);
        List returnList=new ArrayList();
        for(AppParam  param:list){
            HashMap<String,Object> hm=new HashMap<String,Object>();
            hm.put("sysTyp",param.getSysTyp());
            hm.put("paramCode",param.getParamCode());
            hm.put("paramName",param.getParamName());
            hm.put("paramValue",param.getParamValue());
            returnList.add(hm);
        }
        return super.success(returnList);
    }



}
