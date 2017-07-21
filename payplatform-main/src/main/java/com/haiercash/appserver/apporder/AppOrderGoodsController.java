package com.haiercash.appserver.apporder;

import com.haiercash.appserver.service.AppOrderGoodsService;
import com.haiercash.appserver.web.BaseController;
import com.haiercash.common.data.AppOrderGoods;
import com.haiercash.commons.util.RestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
public class AppOrderGoodsController extends BaseController {
    private static String MODULE_NO = "11";

    public AppOrderGoodsController() {
        super(MODULE_NO);
    }

    @Autowired
    private AppOrderGoodsService appOrderGoodsService;

    /**
     * 描述：添加单个商品
     *
     * @param appOrderGoods
     * @return
     */
    @RequestMapping(value = "/app/appserver/apporder/saveAppOrderGoods", method = RequestMethod.POST)
    public Map<String, Object> saveAppOrderGoods(@RequestBody AppOrderGoods appOrderGoods) {
        appOrderGoods.setSeqNo(UUID.randomUUID() + "");
        if (!StringUtils.isEmpty(appOrderGoods.getGoodsPrice())) {
            if (!DataVerificationUtil.isNumber(appOrderGoods.getGoodsPrice())) {
                return fail(RestUtil.ERROR_INTERNAL_CODE, "商品价格只能填写数字！");
            }
        }
        if (!StringUtils.isEmpty(appOrderGoods.getGoodsNum())) {
            if (!DataVerificationUtil.isNumber(appOrderGoods.getGoodsNum())) {
                return fail(RestUtil.ERROR_INTERNAL_CODE, "商品数量只能填写数字！");
            }
        } else {
            appOrderGoods.setGoodsNum("0");
        }
        return appOrderGoodsService.saveAppOrderGoods(appOrderGoods);
    }

    /**
     * 描述：修改单个商品（修改错误码时需提醒安卓商户版开发人员）
     *
     * @param appOrderGoods
     * @return
     */
    @RequestMapping(value = "/app/appserver/apporder/updateAppOrderGoods", method = RequestMethod.POST)
    public Map<String, Object> updateAppOrderGoods(@RequestBody AppOrderGoods appOrderGoods) {
        String seqNo = appOrderGoods.getSeqNo();
        if (StringUtils.isEmpty(seqNo)) {
            return fail("9005", "要修改的商品id为空！");
        }
        if (!StringUtils.isEmpty(appOrderGoods.getGoodsPrice().trim())) {
            if (!DataVerificationUtil.isNumber(appOrderGoods.getGoodsPrice())) {
                return fail(RestUtil.ERROR_INTERNAL_CODE, "商品价格只能填写数字！");
            }
        }
        if (!StringUtils.isEmpty(appOrderGoods.getGoodsNum().trim())) {
            if (!DataVerificationUtil.isNumber(appOrderGoods.getGoodsNum())) {
                return fail(RestUtil.ERROR_INTERNAL_CODE, "商品数量只能填写数字！");
            }
        }
        return appOrderGoodsService.updateAppOrderGoods(appOrderGoods);
    }

    /**
     * 描述：根据主键，获取单个商品对象
     *
     * @param seqNo
     * @return
     */
    @Deprecated
    @RequestMapping(value = "/app/appserver/apporder/getAppOrderGoods", method = RequestMethod.GET)
    public Map<String, Object> getAppOrderGoods(String seqNo) {
        return fail(RestUtil.ERROR_INTERNAL_CODE, "该接口已废弃");
    }

    /**
     * 根据订单号，查询所有的商品信息
     *
     * @param orderNo
     * @return
     */
    @RequestMapping(value = "/app/appserver/apporder/getAppOrderGoodsByOrderNo", method = RequestMethod.GET)
    public Map<String, Object> getAppOrderGoodsByOrderNo(String orderNo) {
        return appOrderGoodsService.getAppOrderGoodsByOrderNo(orderNo);
    }

    /**
     * 描述：商品删除（根据id）
     *
     * @param seqNo
     * @return
     */
    @Deprecated
    @RequestMapping(value = "/app/appserver/apporder/deleteAppOrderGoods", method = RequestMethod.POST)
    public Map<String, Object> deleteAppOrderGoods(String seqNo) {
        //appOrderGoodsRepository.delete(seqNo);
        return fail(RestUtil.ERROR_INTERNAL_CODE, "该接口已废弃");
    }

    /**
     * 描述：根据订单号、商品序号、商品代码商品删除
     *
     * @param map2
     * @return 输出商品总额
     */
    @RequestMapping(value = "/app/appserver/apporder/deleteAppOrderGoodsByOrderNo", method = RequestMethod.POST)
    public Map<String, Object> deleteAppOrderGoodsByKeys(@RequestBody Map<String, Object> map2) {
        String orderNo = StringUtils.isEmpty(map2.get("orderNo")) ? null : (String) map2.get("orderNo");
        String seqNo = StringUtils.isEmpty(map2.get("seqNo")) ? null : (String) map2.get("seqNo");
        String goodsCode = map2.get("goodsCode") == null ? null : (String) map2.get("goodsCode");
        if (seqNo == null) {
            return fail("9004", "所删除的商品不存在！");
        }
        // 删除商品id，删除对应商品
        return appOrderGoodsService.deleteAppOrderGoodsByKeys(orderNo, seqNo, goodsCode);
    }



}
