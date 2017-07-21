package com.haiercash.appserver.web;

import com.haiercash.appserver.service.AppAdInfoService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Created by yinjun on 2016/9/21.
 */
@RestController
@RequestMapping("/app/appserver/ad")
public class AppAdInfoController extends BaseController {
    public Log logger = LogFactory.getLog(getClass());

    public AppAdInfoController() {
        super("40");
    }
    @Autowired
    AppAdInfoService appAdInfoService;



    @RequestMapping(value = "/selectByAd", method = RequestMethod.GET)
    @Deprecated
    public Map<String, Object> selectByAd() {
        return appAdInfoService.selectByAd();
    }

    //    @RequestMapping(value = "/getAdImag", method = RequestMethod.GET)
//    @ResponseBody
//    @Deprecated
//    public Map<String, Object> getAdImag(@RequestParam String adId, HttpServletResponse response) {
//
//
//        // 根据id 获取存储的对象
//        AppAd ad = appAdRepository.findOne(adId);
//        if (ad == null) {
//            return fail("20", "广告图片信息不存在！");
//        }
//        //   File file = new File(ad.getAdImg());
//        // 设置响应的数据类型;下载的文件名;打开方式
//        response.setContentType("application/file");
//        response.setHeader("content-Disposition", "attachment;filename=" + ad.getId() + (String.valueOf(ad.getAdImg()).contains(".") ? ad.getAdImg().substring(ad.getAdImg().lastIndexOf(".")) : ""));
//        try {
//            // 从下载文件中获取输入流
//            // InputStream is = new BufferedInputStream(new FileInputStream(file));
//            // 从响应中获取一个输出流
//            OutputStream os = new BufferedOutputStream(response.getOutputStream());
//            Files.copy(Paths.get(ad.getAdImg()), os);
//        } catch (IOException e) {
//            logger.error(e.getMessage());
//        }
//
//        return success();
//
//
//    }
    @RequestMapping(value = "/getAdImag", method = RequestMethod.GET)
    public Map<String, Object> getAdImag(@RequestParam String id, HttpServletResponse response) {
        return appAdInfoService.getAdImag(id,response);
    }

    /**
     * 活动详情查询接口
     * 输入参数：广告ID、尺寸类型（取值范围：AND480, AND720, AND1080, IOS568, IOS667, IOS736, IOS480）
     * 输出参数：介绍图片ID、list<商品编码、商品详情、销售代表编号>
     * 商品详情调商品管理接口/pub/gm/getGoodsByCode查询，查询结果的body直接放在商品详情里返回。
     *
     * @param adId     广告ID
     * @param sizeType 尺寸类型
     * @return
     */
    @RequestMapping(value = "/getActiveInfo", method = RequestMethod.GET)
    public Map<String, Object> getActiveInfo(@RequestParam String adId, String sizeType) {

        return appAdInfoService.getActiveInfo(adId,sizeType);
    }
    @RequestMapping(value = "/getAdInfoCheck", method = RequestMethod.GET)
    public Map<String, Object> getAdInfoCheck(@RequestParam String sizeType) {
        return appAdInfoService.getAdInfoCheck(sizeType);
    }
}
