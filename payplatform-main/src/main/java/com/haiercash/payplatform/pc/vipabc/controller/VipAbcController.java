package com.haiercash.payplatform.pc.vipabc.controller;

import com.haiercash.payplatform.pc.vipabc.service.VipAbcService;
import com.haiercash.payplatform.service.AppServerService;
import com.haiercash.spring.controller.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Created by Administrator on 2017/12/25.
 */
@RestController
public class VipAbcController extends BaseController {
    private static final String MODULE_NO = "03";
    @Autowired
    private VipAbcService vipAbcService;
    @Autowired
    private AppServerService appServerService;

    public VipAbcController() {
        super(MODULE_NO);
    }
    @RequestMapping(value = "/api/payment/shunguang/getIdCardInfo", method = RequestMethod.POST)
    public Map<String, Object> getIdCardInfo(@RequestBody Map<String, Object> map) {
        return vipAbcService.getIdCardInfo(map);
    }

    /**
     * 判断用户是否注册
     *
     * @param params
     * @return
     */
    @RequestMapping(value = "/api/payment/vipabc/vipabcisRegister", method = RequestMethod.POST)
    public Map<String, Object> isRegister(@RequestBody Map<String, Object> params) {
        return vipAbcService.isRegister(super.getToken(), super.getChannel(), super.getChannelNo(), params);
    }

    /**
     * 刷新海尔会员验证码
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    public Map<String, Object> haierCaptcha(@RequestBody Map<String, Object> map) {
        return vipAbcService.haierCaptcha(super.getChannel(), super.getChannelNo());
    }

    /**
     * 登录
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "/api/payment/vipabc/vipAbcLogin", method = RequestMethod.POST)
    public Map<String, Object> vipAbcLogin(@RequestBody Map<String, Object> map) {
        return vipAbcService.vipAbcLogin(super.getChannel(), super.getChannelNo(), map);
    }

    /**
     * 分期申请，订单保存
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/payment/vipabc/saveOrderServlet", method = RequestMethod.POST)
    public Map<String, Object> vipAbcsaveOrderServlet(@RequestBody Map<String, Object> map) throws Exception {
        return vipAbcService.vipAbcsaveOrderServlet(super.getToken(), super.getChannel(), super.getChannelNo(), map);
    }

    /**
     * 分期详情
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "/api/payment/vipabc/vipAbcpayApplyInfo", method = RequestMethod.POST)
    public Map<String, Object> vipAbcpayApplyInfo(@RequestBody Map<String, Object> map) {
        return vipAbcService.vipAbcpayApplyInfo(super.getToken(), super.getChannel(), super.getChannelNo(), map);
    }


    /**
     * 第三方数据入口
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "/servlet/vipabc/getvipAbcKindByStoreServlet.do", method = RequestMethod.POST)
    public Map<String, Object> vipAbcThirdPartyData(@RequestBody Map<String, Object> map) throws Exception {
        return vipAbcService.vipAbcThirdPartyData(super.getToken(), super.getChannel(), super.getChannelNo(), map);
    }

    /**
     * 二维码生成
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "/api/payment/vipabc/vipAbcPcStore", method = RequestMethod.POST)
    public Map<String, Object> vipAbcPcStore(@RequestBody Map<String, Object> map) {
        return vipAbcService.vipAbcPcStore(super.getToken(), super.getChannel(), super.getChannelNo(), map);
    }
}
