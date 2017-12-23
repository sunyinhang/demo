package com.haiercash.payplatform.pc.shunguang.controller;

import com.haiercash.payplatform.common.dao.AppOrdernoTypgrpRelationDao;
import com.haiercash.payplatform.common.data.AppOrder;
import com.haiercash.payplatform.common.data.AppOrdernoTypgrpRelation;
import com.haiercash.payplatform.pc.shunguang.service.ShunguangService;
import com.haiercash.payplatform.service.AcquirerService;
import com.haiercash.payplatform.service.OrderService;
import com.haiercash.spring.controller.BaseController;
import com.haiercash.spring.utils.ConstUtil;
import com.haiercash.spring.utils.HttpUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * shunguang controller.
 *
 * @author yuan li
 * @since v1.0.1
 */
@RestController
public class ShunguangController extends BaseController {
    public Log logger = LogFactory.getLog(getClass());
    //模块编码  02
    private static String MODULE_NO = "02";

    public ShunguangController() {
        super(MODULE_NO);
    }

    @Autowired
    private ShunguangService shunguangService;
    @Autowired
    private AppOrdernoTypgrpRelationDao appOrdernoTypgrpRelationDao;
    @Autowired
    private OrderService orderService;
    @Autowired
    private AcquirerService acquirerService;

    /**
     * 微店主客户信息推送 Sg-10001.
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "/api/payment/shunguang/store/info", method = RequestMethod.POST)
    public Map<String, Object> storeInfo(@RequestBody Map<String, Object> map) {
        Map<String, Object> checkMap = this.confirmData(map);
        if (!HttpUtil.isSuccess(checkMap)) {
            return checkMap;
        }
        return shunguangService.saveStoreInfo(map);
    }

    /**
     * 普通用户信息推送 Sg-10002.
     *
     * @param map 请求报文
     * @return
     */
    @RequestMapping(value = "/api/payment/shunguang/ordinary/info", method = RequestMethod.POST)
    public Map<String, Object> ordinaryUserInfo(@RequestBody Map<String, Object> map) {
        Map<String, Object> checkMap = this.confirmData(map);
        if (!HttpUtil.isSuccess(checkMap)) {
            return checkMap;
        }
        return shunguangService.saveOrdinaryUserInfo(map);
    }

    /**
     * 4.	 白条支付申请接口
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/payment/shunguang/payApply", method = RequestMethod.POST)
    public Map<String, Object> payApply(@RequestBody Map<String, Object> map) throws Exception {
        // 参数非空校验
        Map<String, Object> confirmMsg = confirmData(map);
        if (!HttpUtil.isSuccess(confirmMsg)) {
            return confirmMsg;
        }
        return shunguangService.payApply(map);
    }

    /**
     * 5.	白条额度申请接口   Sg-10004
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/payment/shunguang/edApply", method = RequestMethod.POST)
    public Map<String, Object> edApply(@RequestBody Map<String, Object> map) throws Exception {
        // 参数非空校验
        Map<String, Object> confirmMsg = confirmData(map);
        if (!HttpUtil.isSuccess(confirmMsg)) {
            return confirmMsg;
        }
        return shunguangService.edApply(map);
    }


    /**
     * 7.白条额度申请状态查询    Sg-10006
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/payment/shunguang/checkEdAppl", method = RequestMethod.POST)
    public Map<String, Object> checkEdAppl(@RequestBody Map<String, Object> map) throws Exception {
        Map<String, Object> queryAppmap = confirmData(map);
        if (!HttpUtil.isSuccess(queryAppmap)) {
            return queryAppmap;
        }
        return shunguangService.checkEdAppl(map);
    }

    /**
     * 9.  白条额度进行贷款支付结果主动查询接口    Sg-10008
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/payment/shunguang/queryAppLoanAndGoods", method = RequestMethod.POST)
    public Map<String, Object> queryAppLoanAndGoods(@RequestBody Map<String, Object> map) throws Exception {
        Map<String, Object> queryAppmap = confirmData(map);
        if (!HttpUtil.isSuccess(queryAppmap)) {
            return queryAppmap;
        }
        return shunguangService.queryAppLoanAndGoods(map);
    }

    /**
     * 10.  白条支付实名认证同步接口    Sg-10009
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/payment/shunguang/queryAppLoanAndGoodsOne", method = RequestMethod.POST)
    public Map<String, Object> queryAppLoanAndGoodsOne(@RequestBody Map<String, Object> map) throws Exception {
        Map<String, Object> queryAppmap = confirmData(map);
        if (!HttpUtil.isSuccess(queryAppmap)) {
            return queryAppmap;
        }
        return shunguangService.queryAppLoanAndGoodsOne(map);
    }

    /**
     * 11.  白条额度进行主动查询接口    Sg-10010
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/payment/shunguang/edcheck", method = RequestMethod.POST)
    public Map<String, Object> edcheck(@RequestBody Map<String, Object> map) throws Exception {
        Map<String, Object> queryAppmap = confirmData(map);
        if (!HttpUtil.isSuccess(queryAppmap)) {
            return queryAppmap;
        }//{cooprCode=, applSeq=1265566, channelNo=46, outSts=06, tradeCode=100022, idNo=370682199107018123, msgTyp=01}
//        HashMap<Object, Object> map1 = new HashMap<>();
//        map1.put("cooprCode","");
//        map1.put("channelNo","46");
//        map1.put("applSeq","1703608");
////        map1.put("outSts","04");
//        map1.put("idNo","371102198011105713");
//        map1.put("msgTyp","01");
//        map1.put("tradeCode","100022");
//        String s = JSON.toJSONString(map1);
//        cmisMseeageHandler.consumeNodeMessage(s);
        return shunguangService.edcheck(map);
    }


    public Map<String, Object> confirmData(Map<String, Object> map) {
        if (StringUtils.isEmpty(map.get("applyNo"))) {
            return fail(ConstUtil.ERROR_CODE, "交易流水号(applyNo)不能为空");
        }
        if (StringUtils.isEmpty(map.get("channelNo"))) {
            return fail(ConstUtil.ERROR_CODE, "交易渠道(channelNo)不能为空");
        }
        if (StringUtils.isEmpty(map.get("tradeCode"))) {
            return fail(ConstUtil.ERROR_CODE, "交易编码(tradeCode)不能为空");
        }
        if (StringUtils.isEmpty(map.get("data"))) {
            return fail(ConstUtil.ERROR_CODE, "交易信息(data)不能为空");
        }
        if (StringUtils.isEmpty(map.get("key"))) {
            return fail(ConstUtil.ERROR_CODE, "key不能为空");
        }
        return success();
    }

    /**
     * 额度测试入口
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/payment/shunguang/edApplytest", method = RequestMethod.POST)
    public Map<String, Object> edApplytest(@RequestBody Map<String, Object> map) throws Exception {
        // 参数非空校验
        return shunguangService.edApplytest(map);
    }

    /**
     * 贷款测试入口
     *
     * @param appOrder
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/payment/shunguang/payApplytest", method = RequestMethod.POST)
    public Map<String, Object> payApplytest(@RequestBody AppOrder appOrder) throws Exception {
        // 参数非空校验
        return shunguangService.payApplytest(appOrder);
    }


    /**
     * 描述：根据主键（订单号）删除订单对象及该订单下的所有商品以及共同还款人
     *
     * @param orderNoMap
     * @return 处理成功json
     */
    @RequestMapping(value = "/api/payment/deleteOrderInfo", method = RequestMethod.POST)
    public Map<String, Object> deleteAppOrder(@RequestBody Map<String, Object> orderNoMap) {
        if (StringUtils.isEmpty(orderNoMap.get("orderNo"))) {
            return fail("9004", "系统未接收到订单编号");
        }
        String orderNo = (String) orderNoMap.get("orderNo");
        logger.info("开始删除订单：" + orderNo);
        AppOrdernoTypgrpRelation relation = appOrdernoTypgrpRelationDao.selectByOrderNo(orderNo);
        if (relation == null) {
            return fail("9005", "订单信息不存在");
        }
        Map<String, Object> resultMap;
        // 如果为商品贷，且未生成流水号，则调用订单系统取消订单接口.
        if ("01".equals(relation.getTypGrp()) && StringUtils.isEmpty(relation.getApplSeq())) {
            resultMap = orderService.cancelOrder(orderNo);
        } else {
            resultMap = acquirerService.cancelAppl(relation.getApplSeq());
        }
        if (HttpUtil.isSuccess(resultMap)) {
            HashMap<String, Object> hm = new HashMap<>();
            hm.put("msg", "订单已被取消！");
            return success(hm);
        }
        return resultMap;
    }

    /**
     * @Title
     * @Description:
     * @author yu jianwei
     * @date 2017/11/6 17:40
     */
    @RequestMapping(value = "/api/payment/shunguang/returnGoods", method = RequestMethod.POST)
    public Map<String, Object> returnGoods(@RequestBody Map<String, Object> map) throws Exception {
        Map<String, Object> checkMap = this.confirmData(map);
        if (!HttpUtil.isSuccess(checkMap)) {
            return checkMap;
        }
        return shunguangService.returnGoods(map);
    }

    /**
     * @Title getReturnGoodsInfo
     * @Description: 查询退货详情
     * @author yu jianwei
     * @date 2017/12/15 11:12
     */
    @RequestMapping(value = "/api/payment/shunguang/getReturnGoodsInfo", method = RequestMethod.POST)
    public Map<String, Object> getReturnGoodsInfo(@RequestBody Map<String, Object> map) throws Exception {
        Map<String, Object> checkMap = this.confirmData(map);
        if (!HttpUtil.isSuccess(checkMap)) {
            return checkMap;
        }
        return shunguangService.getReturnGoodsInfo(map);
    }
    @RequestMapping(value = "/api/payment/shunguang/shunguangth",method = RequestMethod.POST)
    public Map<String,Object> shunguangth(@RequestBody Map<String , Object> map){
//        HashMap<Object, Object> map1 = new HashMap<>();
//        HashMap<Object, Object> map2 = new HashMap<>();
//        HashMap<String, Object> map3 = new HashMap<>();
//        map1.put("cooprCode","");
//        map1.put("tradeTime","10:18:52");
//        map1.put("sysFlag","04");
//        map1.put("channelNo","46");
//        map1.put("serno","14894579327991");
//        map1.put("tradeCode","100026");
//        map1.put("tradeDate","2017-03-14");
//        map1.put("tradeType","");
//        map2.put("msgTyp","03");
//        map2.put("applSeq","12321");
//        map2.put("mallOrderNo","456765444");
//        map2.put("loanNo","32432");
//        map2.put("idNo","14894579327991");
//        map2.put("custName","测试");
//        map2.put("businessId","20323");
//        map2.put("businessType","RETURN_GOODS");
//        map2.put("status","05");
//        map2.put("content","成功");
//        map3.put("head",map1);
//        map3.put("body",map2);
        return shunguangService.ShunGuangth(map);
    }
}
