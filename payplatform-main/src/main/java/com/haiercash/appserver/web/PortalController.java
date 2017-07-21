package com.haiercash.appserver.web;

import com.haiercash.common.config.EurekaServer;
import com.haiercash.commons.util.HttpUtil;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Created by Yubing on 2017/2/4.
 * 整合portal接口的控制器
 */
@RestController
public class PortalController extends BaseController {

    private static String MODULE_NO = "77";
    public PortalController() {
        super(MODULE_NO);
    }

    /**
     * 手势密码验证.
     * @param params
     * @return
     */
    @RequestMapping(value = "/app/appserver/portal/mUser/checkGesture", method = RequestMethod.GET)
    public Map<String, Object> checkGesture(@RequestParam Map<String, Object> params){
        if(StringUtils.isEmpty(params.get("userAlias")) || StringUtils.isEmpty(params.get("gesturePass"))) {
            return fail("77","手势密码验证失败！用户名和手势密码不能为空！");
        }
        StringBuffer url = new StringBuffer("/app/portal/mUser/checkGesture?")
                .append(StringUtils.isEmpty(params.get("userAlias"))?"":"userAlias=" +params.get("userAlias")+"&")
                .append(StringUtils.isEmpty(params.get("gesturePass"))?"":"gesturePass=" +params.get("gesturePass")+"&");

        Map<String, Object> result = HttpUtil.restGetMap(EurekaServer.HCPORTAL + url.toString());

        logger.info("调用portal接口返回结果："+ result);
        if (StringUtils.isEmpty(result)) {
            return fail("77", "手势密码验证失败！");
        }
        return result;
    }
    /**
     * 查询商户用户信息.
     * @param params
     * @return
     */
    @RequestMapping(value = "/app/appserver/portal/mUser/info", method = RequestMethod.GET)
    public Map<String, Object> getInfo(@RequestParam Map<String, Object> params) {

        if(StringUtils.isEmpty(params.get("userAlias")) || StringUtils.isEmpty(params.get("deviceId"))) {
            return fail("77","查询商户用户信息失败：用户名和设备号不能为空");
        }
        StringBuffer url = new StringBuffer("/app/portal/mUser/info?")
                .append(StringUtils.isEmpty(params.get("userAlias"))?"":"userAlias=" +params.get("userAlias")+"&")
                .append(StringUtils.isEmpty(params.get("deviceId"))?"":"deviceId=" +params.get("deviceId")+"&");

        Map<String, Object> result = HttpUtil.restGetMap(EurekaServer.HCPORTAL + url.toString());

        logger.info("调用portal接口返回结果："+ result);
        if (StringUtils.isEmpty(result)) {
            return fail("77", "查询商户用户信息失败");
        }
        return result;
    }

    /**
     * 用户密码验证.
     * @param params
     * @return
     */

    @RequestMapping(value = "/app/appserver/portal/mUser/check", method = RequestMethod.GET)
    public Map<String, Object> check(@RequestParam Map<String, Object> params) {

        if(StringUtils.isEmpty(params.get("userAlias")) || StringUtils.isEmpty(params.get("userPass"))) {
            return fail("77","用户密码验证失败：用户名和用户密码不能为空");
        }
        StringBuffer url = new StringBuffer("/app/portal/mUser/check?")
                .append(StringUtils.isEmpty(params.get("userAlias"))?"":"userAlias=" +params.get("userAlias")+"&")
                .append(StringUtils.isEmpty(params.get("userPass"))?"":"userPass=" +params.get("userPass")+"&");

        Map<String, Object> result = HttpUtil.restGetMap(EurekaServer.HCPORTAL + url.toString());

        logger.info("调用portal接口返回结果："+ result);
        if (StringUtils.isEmpty(result)) {
            return fail("77", "用户密码验证失败");
        }
        return result;
    }

    /**
     * 手势密码设置.
     * @param params
     * @return
     */

    @RequestMapping(value = "/app/appserver/portal/mUser/setGesture", method = RequestMethod.PUT)
   // @ResponseBody
    public Map<String, Object> setGesture(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = HttpUtil.restPutMap(EurekaServer.HCPORTAL + "/app/portal/mUser/setGesture", super.getToken(), params);

        logger.info("调用portal接口返回结果："+ result);
        if (StringUtils.isEmpty(result)) {
            return fail("77", "手势密码设置失败");
        }
        return result;
    }

    /**
     * 用户密码设置.
     * @param params
     * @return
     */

   @RequestMapping(value = "/app/appserver/portal/mUser/setPass", method = RequestMethod.PUT)
   public Map<String, Object> setPass(@RequestBody Map<String, Object> params) {
       Map<String, Object> result = HttpUtil.restPutMap(EurekaServer.HCPORTAL + "/app/portal/mUser/setPass", super.getToken(), params);

       logger.info("调用portal接口返回结果："+ result);
       if (StringUtils.isEmpty(result)) {
           return fail("77", "用户密码设置失败");
       }
       return result;
   }

    /**
     * 用户信息查询.
     * @param userAlias
     * @return
     */
   @RequestMapping(value = "/app/appserver/pub/portal/mUser/queryByAlias", method = RequestMethod.GET)
   public Map<String, Object> queryByAlias(String userAlias) {
       if(StringUtils.isEmpty(userAlias)) {
           return fail("77","用户信息查询失败：用户名不能为空");
       }
       Map<String, Object> result = HttpUtil.restGetMap(EurekaServer.HCPORTAL + "/pub/portal/mUser/queryByAlias?userAlias=" + userAlias);

       logger.info(result);
       if (StringUtils.isEmpty(result)) {
           return fail("77", "用户信息查询失败");
       }
       return result;
   }

}
