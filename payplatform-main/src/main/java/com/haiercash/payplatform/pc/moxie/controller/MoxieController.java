package com.haiercash.payplatform.pc.moxie.controller;

import com.haiercash.payplatform.controller.BaseController;
import com.haiercash.payplatform.pc.moxie.service.MoxieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Map;

/**
 * Created by yuanli on 2017/10/9.
 */
@RestController
public class MoxieController extends BaseController{
    @Autowired
    private MoxieService moxieService;
    private static String MODULE_NO = "04";
    public MoxieController() {
        super(MODULE_NO);
    }


    /**
     * 魔蝎公积金回调接口
     * @param request
     * @param response
     * @param body
     */
    @RequestMapping(value = "/api/payment/moxieFund", method = RequestMethod.POST)
    public void moxieFund(HttpServletRequest request, HttpServletResponse response, @RequestBody String body) {
        logger.info("********************魔蝎公积金接口回调开始************************");
        logger.info("魔蝎公积金回调返回信息" + body);
        response.setStatus(201);
        try {
            if (body == null || "".equals(body)) {
                logger.info("魔蝎回调返回信息为空");
            } else {
                moxieService.moxie(body, "01");
            }
            logger.info("********************魔蝎公积金接口回调结束************************");
            PrintWriter printWriter = response.getWriter();
            printWriter.write("我成功了。");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 魔蝎网银回调接口
     * @param request
     * @param response
     * @param body
     */
    @RequestMapping(value = "/api/payment/moxieBank", method = RequestMethod.POST)
    public void moxieBank(HttpServletRequest request, HttpServletResponse response, @RequestBody String body) {
        logger.info("********************魔蝎网银接口回调开始************************");
        logger.info("魔蝎网银回调返回信息" + body);
        response.setStatus(201);
        try {
            if (body == null || "".equals(body)) {
                logger.info("魔蝎回调返回信息为空");
            } else {
                moxieService.moxie(body, "02");
            }
            logger.info("********************魔蝎网银接口回调结束************************");
            PrintWriter printWriter = response.getWriter();
            printWriter.write("我成功了。");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 魔蝎运营商回调接口
     * @param request
     * @param response
     * @param body
     */
    @RequestMapping(value = "/api/payment/moxieCarrier", method = RequestMethod.POST)
    public void moxieCarrier(HttpServletRequest request, HttpServletResponse response, @RequestBody String body) {
        logger.info("********************魔蝎运营商接口回调开始************************");
        logger.info("魔蝎运营商回调返回信息" + body);
        response.setStatus(201);
        try {

            if (body == null || "".equals(body)) {
                logger.info("魔蝎回调返回信息为空");
            } else {
                moxieService.moxie(body, "03");
            }
            logger.info("********************魔蝎运营商接口回调结束************************");
            PrintWriter printWriter = response.getWriter();
            printWriter.write("我成功了。");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    /**
     * 根据申请流水号查询是否做过魔蝎认证
     * @param applseq
     * @return
     */
    @RequestMapping(value = "/api/payment/getMoxieByApplseq", method = RequestMethod.GET)
    public Map<String, Object> getMoxieByApplseq(@RequestParam("applseq") String applseq) {
        logger.info("*************************魔蝎查询接口开始**********************");
        Map<String, Object> map = moxieService.getMoxieByApplseq(applseq);
        logger.info("*************************魔蝎查询接口结束**********************");
        return success(map);
    }
}
