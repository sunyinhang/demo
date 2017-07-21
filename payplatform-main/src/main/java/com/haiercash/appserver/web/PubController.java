package com.haiercash.appserver.web;

import com.haiercash.appserver.gm.service.GmService;
import com.haiercash.common.config.EurekaServer;
import com.haiercash.commons.util.HttpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 整合商品管理系统的控制器
 */
@RestController
public class PubController extends BaseController {
    private static String MODULE_NO = "35";

    public PubController() {
        super(MODULE_NO);
    }

    @Autowired
    private GmService gmService;

    /**
     * 查询商品及列表.
     *
     * @return Map
     */
    @RequestMapping(value = "/app/appserver/pub/gm/getGoodsByMerchantCodeAndArea", method = RequestMethod.GET)
    public Map<String, Object> getGoodsByMerchantCodeAndArea(@RequestParam Map<String, Object> params) {
        if(StringUtils.isEmpty(params.get("merchantCode"))){
            return fail("15", "必传参数商户代码为空！");
        }
       StringBuffer url = new StringBuffer("/pub/gm/getGoodsByMerchantCodeAndArea?")
                .append(StringUtils.isEmpty(params.get("merchantCode")) ? "" : "merchantCode=" + params.get("merchantCode") + "&")
                .append(StringUtils.isEmpty(params.get("storeCode")) ? "" : "storeCode=" + params.get("storeCode") + "&")
                .append(StringUtils.isEmpty(params.get("goodsName")) ? "" : "goodsName=" + params.get("goodsName") + "&")
                .append(StringUtils.isEmpty(params.get("goodsKind")) ? "" : "goodsKind=" + params.get("goodsKind") + "&")
                .append(StringUtils.isEmpty(params.get("loanType")) ? "" : "loanType=" + params.get("loanType") + "&")
                .append(StringUtils.isEmpty(params.get("merchantArea")) ? "" : "merchantArea=" + params.get("merchantArea") + "&")
                .append(StringUtils.isEmpty(params.get("page")) ? "" : "page=" + params.get("page") + "&")
                .append(StringUtils.isEmpty(params.get("size")) ? "" : "size=" + params.get("size") + "&");
        logger.info(url.toString());
        Map<String, Object>  result = HttpUtil.restGetMap(EurekaServer.GM + url.toString());
        logger.info("返回结果result：" + result);
        if (StringUtils.isEmpty(result)) {
            return fail("15", "查询商品及列表失败");
        }
        return result;
    }

    /**
     * 修改商品.
     *
     * @return Map
     */
    @RequestMapping(value = "/app/appserver/pub/gm/updateGood", method = RequestMethod.PUT)
    public Map<String, Object> updateGood(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = HttpUtil.restPutMap(EurekaServer.GM + "/pub/gm/updateGood", params);
        logger.info("返回结果result：" + result);
        if (StringUtils.isEmpty(result)) {
            return fail("15", "修改商品失败");
        }
        return result;
    }

    /**
     * 新增商品.
     *
     * @return Map
     */
    @RequestMapping(value = "/app/appserver/pub/gm/savegood", method = RequestMethod.POST)
    public Map<String, Object> savegood(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = HttpUtil.restPostMap(EurekaServer.GM + "/pub/gm/savegood", params);
        logger.info("返回结果result：" + result);
        if (StringUtils.isEmpty(result)) {
            return fail("15", "新增商品失败");
        }
        return result;
    }

    /**
     * 商品查询.
     *
     * @return Map
     */
    @RequestMapping(value = "/app/appserver/pub/gm/getGoodsByCode", method = RequestMethod.GET)
    public Map<String, Object> getGoodsByCode(@RequestParam String goodsCode) {
        Map<String, Object> result = gmService.getGoodsByCode(goodsCode);
        return result;
    }

    /**
     * 查询商品品牌
     *
     * @return Map
     */
    @RequestMapping(value = "/app/appserver/pub/gm/getBrandDic", method = RequestMethod.GET)
    public Map<String, Object> getBrandDic(@RequestParam Map<String, Object> params) {
     //   Map<String, Object> result = HttpUtil.restExchangeMap(HttpMethod.GET, this.getGateUrl() + "/pub/gm/getBrandDic", null, params, null);
        if(StringUtils.isEmpty(params.get("merchantCode"))){
            return fail("15", "必传参数商户代码为空！");
        }
        StringBuffer url = new StringBuffer("/pub/gm/getBrandDic?")
                .append(StringUtils.isEmpty(params.get("merchantCode")) ? "" : "merchantCode=" + params.get("merchantCode") + "&")
                .append(StringUtils.isEmpty(params.get("storeCode")) ? "" : "storeCode=" + params.get("storeCode") + "&");
        logger.info(url.toString());
        Map<String, Object>  result = HttpUtil.restGetMap(EurekaServer.GM + url.toString());
        logger.info("返回结果result：" + result);
        if (StringUtils.isEmpty(result)) {
            return fail("15", "查询商品品牌失败");
        }
        return result;
    }

    /**
     * 查询商品类型
     *
     * @return Map
     */
    @RequestMapping(value = "/app/appserver/pub/gm/getKindDic", method = RequestMethod.GET)
    public Map<String, Object> getKindDic(@RequestParam Map<String, Object> params) {
        //Map<String, Object> result = HttpUtil.restExchangeMap(HttpMethod.GET, this.getGateUrl() + "/pub/gm/getKindDic", null, params, null);
        if(StringUtils.isEmpty(params.get("merchantCode"))){
            return fail("15", "必传参数商户代码为空！");
        }
        if(StringUtils.isEmpty(params.get("brandCode"))){
            return fail("15", "必传参数商品品牌代码为空！");
        }
        StringBuffer url = new StringBuffer("/pub/gm/getKindDic?")
                .append(StringUtils.isEmpty(params.get("merchantCode")) ? "" : "merchantCode=" + params.get("merchantCode") + "&")
                .append(StringUtils.isEmpty(params.get("storeCode")) ? "" : "storeCode=" + params.get("storeCode") + "&")
                .append(StringUtils.isEmpty(params.get("brandCode")) ? "" : "brandCode=" + params.get("brandCode") + "&");
        logger.info(url.toString());
        Map<String, Object>  result = HttpUtil.restGetMap(EurekaServer.GM + url.toString());
        logger.info("返回结果result：" + result);
        if (StringUtils.isEmpty(result)) {
            return fail("15", "查询商品类型失败");
        }
        return result;
    }

    /**
     * 查询门店信息
     *
     * @return Map
     */
    @RequestMapping(value = "/app/appserver/pub/gm/getStoreDic", method = RequestMethod.GET)
    public Map<String, Object> getStoreDic(@RequestParam Map<String, Object> params) {
        //Map<String, Object> result = HttpUtil.restExchangeMap(HttpMethod.GET, this.getGateUrl() + "/pub/gm/getStoreDic", null, params, null);
        if(StringUtils.isEmpty(params.get("merchantCode"))){
            return fail("15", "必传参数商户代码为空！");
        }
        if(StringUtils.isEmpty(params.get("kindCode"))){
            return fail("15", "必传参数商品类型为空！");
        }
        StringBuffer url = new StringBuffer("/pub/gm/getStoreDic?")
                .append(StringUtils.isEmpty(params.get("merchantCode")) ? "" : "merchantCode=" + params.get("merchantCode") + "&")
                .append(StringUtils.isEmpty(params.get("kindCode")) ? "" : "kindCode=" + params.get("kindCode") + "&")
                .append(StringUtils.isEmpty(params.get("loanCode")) ? "" : "loanCode=" + params.get("loanCode") + "&");
        logger.info(url.toString());
        Map<String, Object>  result = HttpUtil.restGetMap(EurekaServer.GM + url.toString());
        logger.info("返回结果result：" + result);
        if (StringUtils.isEmpty(result)) {
            return fail("15", "查询门店信息失败");
        }
        return result;
    }

    /**
     * 查询商品
     *
     * @return Map
     */
    @RequestMapping(value = "/app/appserver/pub/gm/getGoodsByNameAndStore", method = RequestMethod.GET)
    public Map<String, Object> getGoodsByNameAndStore(@RequestParam Map<String, Object> params) {
        //Map<String, Object> result = HttpUtil.restExchangeMap(HttpMethod.GET, this.getGateUrl() + "/pub/gm/getGoodsByNameAndStore", null, params, null);
        if(StringUtils.isEmpty(params.get("storeCode"))){
            return fail("15", "必传参数门店代码为空！");
        }
        if(StringUtils.isEmpty(params.get("goodsName"))){
            return fail("15", "必传参数商品名称为空！");
        }
        StringBuffer url = new StringBuffer("/pub/gm/getGoodsByNameAndStore?")
                .append(StringUtils.isEmpty(params.get("storeCode")) ? "" : "storeCode=" + params.get("storeCode") + "&")
                .append(StringUtils.isEmpty(params.get("goodsName")) ? "" : "goodsName=" + params.get("goodsName") + "&");
        logger.info(url.toString());
        Map<String, Object>  result = HttpUtil.restGetMap(EurekaServer.GM + url.toString());
        logger.info("返回结果result：" + result);
        if (StringUtils.isEmpty(result)) {
            return fail("15", "查询商品失败");
        }
        return result;
    }

    /**
     * 查询贷款品种
     *
     * @return Map
     */
    @RequestMapping(value = "/app/appserver/pub/gm/getLoanDic", method = RequestMethod.GET)
    public Map<String, Object> getLoanDic(@RequestParam Map<String, Object> params) {
       // Map<String, Object> result = HttpUtil.restExchangeMap(HttpMethod.GET, this.getGateUrl() + "/pub/gm/getLoanDic", null, params, null);
        if(StringUtils.isEmpty(params.get("merchantCode"))){
            return fail("15", "必传参数商户代码为空！");
        }
        StringBuffer url = new StringBuffer("/pub/gm/getLoanDic?")
                .append(StringUtils.isEmpty(params.get("merchantCode")) ? "" : "merchantCode=" + params.get("merchantCode") + "&")
                .append(StringUtils.isEmpty(params.get("storeCode")) ? "" : "storeCode=" + params.get("storeCode") + "&")
                .append(StringUtils.isEmpty(params.get("loanType")) ? "" : "loanType=" + params.get("loanType") + "&")
                .append(StringUtils.isEmpty(params.get("goodsCode")) ? "" : "goodsCode=" + params.get("goodsCode") + "&");
        logger.info(url.toString());
        Map<String, Object>  result = HttpUtil.restGetMap(EurekaServer.GM + url.toString());
        logger.info("返回结果result：" + result);
        if (StringUtils.isEmpty(result)) {
            return fail("15", "查询贷款品种失败");
        }
        return result;
    }

    /**
     * 查询贷款类型
     *
     * @return Map
     */
    @RequestMapping(value = "/app/appserver/pub/gm/getLoanTypeDic", method = RequestMethod.GET)
    public Map<String, Object> getLoanTypeDic(@RequestParam Map<String, Object> params) {
       // Map<String, Object> result = HttpUtil.restExchangeMap(HttpMethod.GET, this.getGateUrl() + "/pub/gm/getLoanTypeDic", null, params, null);
        if(StringUtils.isEmpty(params.get("merchantCode"))){
            return fail("15", "必传参数商户代码为空！");
        }
        StringBuffer url = new StringBuffer("/pub/gm/getLoanTypeDic?")
                .append(StringUtils.isEmpty(params.get("merchantCode")) ? "" : "merchantCode=" + params.get("merchantCode") + "&")
                .append(StringUtils.isEmpty(params.get("storeCode")) ? "" : "storeCode=" + params.get("storeCode") + "&");
        logger.info(url.toString());
        Map<String, Object>  result = HttpUtil.restGetMap(EurekaServer.GM + url.toString());
        logger.info(result);
        if (StringUtils.isEmpty(result)) {
            return fail("15", "查询贷款类型失败");
        }
        return result;
    }

    /**
     * 商品图片列表（商品编码）
     *
     * @return Map
     */
    @RequestMapping(value = "/app/appserver/pub/gm/getGoodsImgByCode", method = RequestMethod.GET)
    public Map<String, Object> getGoodsImgByCode(@RequestParam String goodsCode) {
        if(StringUtils.isEmpty(goodsCode)){
            return fail("15", "必传参数商品编码为空！");
        }
        StringBuffer url = new StringBuffer("/pub/gm/getGoodsImgByCode?goodsCode=" + goodsCode);
        logger.info("请求URL为：" + url.toString());
        Map<String, Object>  result = HttpUtil.restGetMap(EurekaServer.GM + url.toString());
        logger.info("返回结果为：" + result);
        if (StringUtils.isEmpty(result)) {
            return fail("15", "查询商品图片列表失败");
        }
        return result;
    }

    /**
     * 商品查询（包含图片）
     *
     * @return Map
     */
    @RequestMapping(value = "/app/appserver/pub/gm/getGoodsAddImgByCode", method = RequestMethod.GET)
    public Map<String, Object> getGoodsAddImgByCode(@RequestParam String goodsCode) {
        if(StringUtils.isEmpty(goodsCode)){
            return fail("15", "必传参数商品编码为空！");
        }
        StringBuffer url = new StringBuffer("/pub/gm/getGoodsAddImgByCode?goodsCode=" + goodsCode);
        logger.info("请求URL为：" + url.toString());
        Map<String, Object>  result = HttpUtil.restGetMap(EurekaServer.GM + url.toString());
        logger.info("返回结果为：" + result);
        if (StringUtils.isEmpty(result) || result.isEmpty()) {
            return fail("15", "查询商品及图片失败");
        }
        return result;
    }

    /**
     * 查询商品库存
     *
     * @return Map
     */
    @RequestMapping(value = "/app/appserver/pub/gm/getGoodsInventory", method = RequestMethod.GET)
    public Map<String, Object> getGoodsInventory(@RequestParam Map<String, Object> params) {
        if(StringUtils.isEmpty(params.get("goodsCode"))){
            return fail("15", "必传参数商品编码为空！");
        }
        if(StringUtils.isEmpty(params.get("provinceCode"))){
            return fail("15", "必传参数省编号为空！");
        }
        String url = EurekaServer.GM + "/pub/gm/getGoodsInventory";
        url = putParam2Url(url, params);
        logger.info(" 查询商品库存 url:" + url);
        Map<String, Object>  result = HttpUtil.restGetMap(url.toString());
        logger.info(" 查询商品库存 返回结果:" + result);
        if (StringUtils.isEmpty(result)) {
            return fail("15", "查询商品库存失败");
        }
        return result;
    }

    /**
     * 根据商品查询哪些地区有库存
     *
     * @return Map
     */
    @RequestMapping(value = "/app/appserver/pub/gm/getGoodsPleInventoryByCode", method = RequestMethod.GET)
    public Map<String, Object> getGoodsPleInventoryByCode(@RequestParam String goodsCode) {
         if(StringUtils.isEmpty(goodsCode)){
            return fail("15", "必传参数商品编码为空！");
        }
        StringBuffer url = new StringBuffer("/pub/gm/getGoodsPleInventoryByCode?goodsCode=" + goodsCode);
        logger.info("请求URL为：" + url.toString());
        Map<String, Object>  result = HttpUtil.restGetMap(EurekaServer.GM + url.toString());
        logger.info( "返回结果为："+ result);
        if (StringUtils.isEmpty(result)) {
            return fail("15", "查询商品库存地区失败");
        }
        return result;
    }

    /**
     * 根据SKU码和区编码查询库存
     *
     * @return Map
     */
    @RequestMapping(value = "/app/appserver/pub/gm/queryInventoryBySkucode", method = RequestMethod.GET)
    public Map<String, Object> queryInventoryBySkucode(@RequestParam Map<String, Object> params) {
        if(StringUtils.isEmpty(params.get("area"))){
            return fail("15", "必传参数区编码为空！");
        }
        if(StringUtils.isEmpty(params.get("skuCode")) && StringUtils.isEmpty(params.get("goodsCode"))){
            return fail("15", "Sku码和商品编码不能同时为空！");
        }
        String url = "/pub/gm/queryInventoryBySkucode";
        url = putParam2Url(url, params);
        logger.info("请求URL为：" + url);
        Map<String, Object>  result = HttpUtil.restGetMap(EurekaServer.GM + url);
        logger.info( "返回结果为："+ result);
        if (StringUtils.isEmpty(result)) {
            return fail("15", "根据SKU码和区编码查询库存失败");
        }
        return result;
    }

    /**
     * 把Map中的参数拼入url.
     *
     * @param url
     * @param params
     * @return
     */
    public String putParam2Url(String url, Map<String, Object> params) {
        if (params.isEmpty()) {
            return url;
        }
        StringBuilder sb = new StringBuilder(url);
        sb.append("?");
        for (String key : params.keySet()) {
            sb.append(key).append("=").append(params.get(key)).append("&");
        }
        String result = sb.toString();
        return result.substring(0, result.length() - 1);
    }

}
