package com.haiercash.appserver.web;

import com.haiercash.common.config.EurekaServer;
import com.haiercash.appserver.util.ConstUtil;
import com.haiercash.common.data.LoanTypeModelRepository;
import com.haiercash.commons.util.HttpUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@EnableRedisHttpSession
public class LoanTypeModelController extends BaseController {
    private Log logger = LogFactory.getLog(LoanTypeModelController.class);
    @Autowired
    LoanTypeModelRepository loanTypeModelRepository;


    public static String MODULE_NO = "13";

    public LoanTypeModelController() {
        super(MODULE_NO);
    }


    @RequestMapping(value = "/app/appServer/getLoanModelNo", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getLoanModelNo(@RequestParam("typLevelTwo") String typLevelTwo) {
        String modelNo = loanTypeModelRepository.findByLoanType(typLevelTwo);
        logger.info(modelNo);
        HashMap<String, Object> map = new HashMap<String, Object>();
        if (!StringUtils.isEmpty(modelNo)) {
            map.put("modelNo", modelNo);
        } else {
            map.put("modelNo", '0');
        }

        return success(map);
    }

    /**
     * 根据门店编码查询贷款品种详情
     *
     * @param cooprCde
     * @return
     */
    @RequestMapping(value = "/app/appserver/xcd/{cooprCde}/loancodes", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> queryPLoanInfoByCooprCde(@PathVariable("cooprCde") String cooprCde) {
        if (StringUtils.isEmpty(cooprCde)){
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "门店编码不可为空");
        }
        String url = EurekaServer.CMISPROXY + "/api/pLoanTyp/queryPLoanInfoByCooprCde?cooprCde=" + cooprCde;
        String json = HttpUtil.restGet(url, super.getToken());
        List<Map<String, Object>> cmisList = new ArrayList<Map<String, Object>>();
        if (StringUtils.isEmpty(json)) {
            return fail("08", "查询失败");
        } else {
            cmisList = HttpUtil.json2List(json);
        }
        return success(cmisList);
    }
}
