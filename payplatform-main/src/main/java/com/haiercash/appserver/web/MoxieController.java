package com.haiercash.appserver.web;

import com.haiercash.appserver.service.*;
import com.haiercash.common.config.EurekaServer;
import com.haiercash.appserver.util.MoneyTool;
import com.haiercash.appserver.util.sign.FileSignUtil;
import com.haiercash.common.data.*;
import com.haiercash.commons.cmis.CmisTradeCode;
import com.haiercash.commons.util.CommonProperties;
import com.haiercash.commons.util.EncryptUtil;
import com.haiercash.commons.util.HttpUtil;
import com.haiercash.commons.util.ResultHead;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sun.misc.BASE64Decoder;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;


/**
 * moxie controller.
 *
 * @author Liu qingxiang
 * @see BaseController
 * @since v1.5.1
 */
@RestController
public class MoxieController extends BaseController {
    private Log logger = LogFactory.getLog(this.getClass());
    @Autowired
    MoxieInfoRepository moxieInfoRepository;
    @Autowired
    private QiaoRongService qiaoRongService;
    @Autowired
    private MoxieService moxieService;

    public MoxieController() {
        super("66");
    }

    /**
     * 魔蝎公积金回调接口
     * @param request
     * @param response
     * @param body
     */
    @RequestMapping(value = "/app/appserver/moxieFund", method = RequestMethod.POST)
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
    @RequestMapping(value = "/app/appserver/moxieBank", method = RequestMethod.POST)
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
    @RequestMapping(value = "/app/appserver/moxieCarrier", method = RequestMethod.POST)
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
    @RequestMapping(value = "/app/appserver/getMoxieByApplseq", method = RequestMethod.GET)
    public Map<String, Object> getMoxieByApplseq(@RequestParam("applseq") String applseq) {
        logger.info("*************************魔蝎查询接口开始**********************");
        Map<String, Object> map = qiaoRongService.getMoxieByApplseq(applseq);
        logger.info("*************************魔蝎查询接口结束**********************");
        return success(map);
    }

}