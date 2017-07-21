package com.haiercash.appserver.service;

import com.haiercash.common.config.EurekaServer;
import com.haiercash.common.data.AppAd;
import com.haiercash.common.data.AppAdEventGoods;
import com.haiercash.common.data.AppAdEventGoodsRepository;
import com.haiercash.common.data.AppAdPhoto;
import com.haiercash.common.data.AppAdPhotoRepository;
import com.haiercash.common.data.AppAdRepository;
import com.haiercash.common.service.BaseService;
import com.haiercash.commons.util.HttpUtil;
import com.haiercash.commons.util.RestUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by yinjun on 2016/9/27.
 */
@Service
public class AppAdInfoService extends BaseService {
    @Autowired
    AppAdRepository appAdRepository;//广告dao
    @Autowired
    AppAdPhotoRepository appAdPhotoRepository;//广告图片dao
    @Autowired
    AppAdEventGoodsRepository appAdEventGoodsRepository;//活动详情dao


    public Map<String, Object> getAdImag( String id, HttpServletResponse response) {

        // 根据id 获取存储的对象
        AppAdPhoto adPhoto = appAdPhotoRepository.findOne(id);
        if (adPhoto == null) {
            return fail("20", "广告图片信息不存在！");
        }
        // 设置响应的数据类型;下载的文件名;打开方式
        response.setContentType("application/file");
        response.setHeader("content-Disposition", "attachment;filename=" + adPhoto.getId() + (String.valueOf(adPhoto.getAdImg()).contains(".") ? adPhoto.getAdImg().substring(adPhoto.getAdImg().lastIndexOf(".")) : ""));
        OutputStream os = null;
        try {
            // 从响应中获取一个输出流
            os = new BufferedOutputStream(response.getOutputStream());
            Files.copy(Paths.get(adPhoto.getAdImg()), os);
            os.flush();
            os.close();
        } catch (IOException e) {
            logger.error(e.getMessage());
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    logger.error("getAdImag方法关闭流失败！");
                }
            }
        }
        return success();
    }

    public Map<String, Object> selectByAd() {
        List<AppAd> list = appAdRepository.findByIsActive("Y");
        List returnList = new ArrayList();
        for (AppAd ad : list) {
            HashMap<String, Object> hm = new HashMap<String, Object>();
            hm.put("adId", ad.getId());
           // hm.put("adType", ad.getAdType());
          //  hm.put("adImg", ad.getAdImg());
          //  hm.put("goodsCode", ad.getGoodsCode());
            hm.put("remark", ad.getRemark());
            returnList.add(hm);
        }
        HashMap hm = new HashMap<String, Object>();
        hm.put("list", returnList);
        return success(hm);
    }


    public Map<String, Object> getActiveInfo(String adId, String sizeType) {
        //查询广告信息
        AppAdPhoto adPhoto = appAdPhotoRepository.findByadIdAndSizeType(adId, sizeType);
        String photoId = "";
        Integer photoHeight = null;
        Integer photoWidth  = null;
        if(adPhoto != null){
            photoId = StringUtils.isEmpty(adPhoto.getId()) ? "":adPhoto.getId();//主键
            photoHeight= StringUtils.isEmpty(adPhoto.getDisplayHeight())? 0:adPhoto.getDisplayHeight();
            photoWidth= StringUtils.isEmpty(adPhoto.getDisplayWidth())? 0 : adPhoto.getDisplayWidth();
        }
        //从 活动商品明细表（app_ad_event_goods）表中查询活动商品明细
        List<AppAdEventGoods> list = appAdEventGoodsRepository.findByAdId(adId);
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("imageId", photoId);
        resultMap.put("displayHeight", String.valueOf(photoHeight));
        resultMap.put("displayWidth", String.valueOf(photoWidth));
        List spList = new ArrayList<>();
        //从商品管理里面调商品详情
        for (AppAdEventGoods eventGoods : list) {
            HashMap<String, Object> spMap = new HashMap<String, Object>();//封装每个商品的信息
            String goodsCode = eventGoods.getGoodsCode();
            spMap.put("remark", eventGoods.getRemark());
            spMap.put("createTime", eventGoods.getCreateTime());
            spMap.put("goodsCode", goodsCode);
            spMap.put("salerCode", eventGoods.getSalerCode());
            spMap.put("tnrOpt", eventGoods.getTnrOpt()==null?"0":eventGoods.getTnrOpt().toString());
            spMap.put("repayTnrAmt", eventGoods.getRepayTnrAmt()==null?"0":eventGoods.getRepayTnrAmt().toString());
            if (StringUtils.isEmpty(goodsCode)) {//若商品编码不存在，则不封装返回了
                continue;
            }
            String url = EurekaServer.GM + "/pub/gm/getGoodsByCode?goodsCode=" + goodsCode;
            logger.info("GM 1.10接口请求地址：" + url);
            String json = HttpUtil.restGet(url, super.getToken());
            logger.info("GM 1.10接口返回：" + json);
            if (StringUtils.isEmpty(json)) {
                logger.info("GM 1.10接口【商品查询】请求获得结果为空！");
                return RestUtil.fail("A4010", "商品编码为" + goodsCode + "的商品在商品管理系统中查询失败！");
            }
            logger.info(json);
            //  logger.info(json.replaceAll("\"",null));
            Map<String, Object> jsonMap = HttpUtil.json2Map(json.replace("\"null\"","\"\"").replaceAll("null", "\"\""));
            JSONObject headMap = (JSONObject) jsonMap.get("head");
            String flag = String.valueOf(headMap.get("retFlag"));
            String msg = String.valueOf(headMap.get("retMsg"));
            if (!"00000".equals(flag)) {
                return fail(flag, msg + "!商品代码：" + goodsCode);
            }
            JSONObject bodyMap = (JSONObject) jsonMap.get("body");
            // String bodyMapJson=bodyMap.toString().replaceAll(":","=");
            // logger.info("bodyMapJson"+bodyMapJson);
            //Map<String,Object> newBodyMap= HttpUtil.json2Map(this.toHashMap(bodyMap));
            logger.info("bodyMap:" + bodyMap);
            spMap.put("goodsInfo", toHashMap(bodyMap));
            spList.add(spMap);

        }
        resultMap.put("list", spList);
        return success(resultMap);
    }
    public Map<String, Object> getAdInfoCheck(String sizeType) {
        Map<String, Object> resultMap = new HashMap<String, Object>();//返回的map
      //  List<AppAdPhoto> list = appAdPhotoRepository.findBySizeType(sizeType);
        //查询开屏广告
        List<Object[]> kpInfo=appAdPhotoRepository.findKpAdInfo(sizeType);
       // logger.info(kpInfo.size());
        if(kpInfo!=null&&kpInfo.size()>0){
            String kpAdid=String.valueOf(kpInfo.get(0)[0]);
            String kpShowTime=String.valueOf(kpInfo.get(0)[1]);
            String kpPhotoId=String.valueOf(kpInfo.get(0)[2]);
            String displayHeigh=String.valueOf(kpInfo.get(0)[4]);
            String displayWidth=String.valueOf(kpInfo.get(0)[5]);
            HashMap<String, Object> kpAdMap = new HashMap<String, Object>();
             kpAdMap.put("adId", kpAdid);//广告id
             kpAdMap.put("photoId", kpPhotoId);//图片id
             kpAdMap.put("showTime", kpShowTime);
            kpAdMap.put("displayHeight", displayHeigh);
            kpAdMap.put("displayWidth", displayWidth);
            resultMap.put("kpAd", kpAdMap);
            logger.debug("kpMap=="+kpAdMap);
        }else{
            logger.info("未查询出开屏广告信息，所有字段为空！");
            HashMap<String, Object> kpAdMap = new HashMap<String, Object>();
            kpAdMap.put("adId", "");//广告id
            kpAdMap.put("photoId", "");//图片id
            kpAdMap.put("showTime", "");
            kpAdMap.put("displayHeight", "");
            kpAdMap.put("displayWidth", "");
            resultMap.put("kpAd", kpAdMap);
          //  resultMap.put("kpAd", "");
        }

        //查询焦点广告
        List jdAdList = new ArrayList();
        List<Object[]> list = appAdPhotoRepository.findJdAdBySizeType(sizeType);
        for(Object[] strs:list){
            String jdAdid=String.valueOf(strs[0]);
            String jdShowTime=String.valueOf(strs[1]);
            String jdPhotoId=String.valueOf(strs[2]);
            String displayHeigh=String.valueOf(strs[4]);
            String displayWidth=String.valueOf(strs[5]);
            HashMap<String, Object> jdAdMap = new HashMap<String, Object>();
            jdAdMap.put("adId", jdAdid);//广告id
            jdAdMap.put("photoId", jdPhotoId);//图片id
            jdAdMap.put("showTime", jdShowTime);
            jdAdMap.put("displayHeight", displayHeigh);
            jdAdMap.put("displayWidth", displayWidth);
            jdAdList.add(jdAdMap);
          //  logger.info(jdAdList);
        }
        resultMap.put("jdAd", jdAdList);
        return success(resultMap);
    }
    private HashMap<String, Object> toHashMap(JSONObject json) {
        HashMap<String, Object> data = new HashMap<String, Object>();
        // 将json字符串转换成jsonObject
        Iterator it = json.keys();
        // 遍历jsonObject数据，添加到Map对象
        while (it.hasNext()) {
            String key = String.valueOf(it.next());
            Object value = json.get(key);
            if (value instanceof String) {
                data.put(key, value);
            } else if (value instanceof Collection) {
                ArrayList newList = new ArrayList();
                List<JSONObject> list = (ArrayList<JSONObject>) value;
                for (JSONObject jsonlist : list) {
                    newList.add(this.toHashMap(jsonlist));
                }
                data.put(key, newList);
            } else if (value instanceof JSONObject) {
                data.put(key, toHashMap((JSONObject) value));
            } else if (value instanceof JSONArray) {
                JSONArray newValue = (JSONArray) value;
                int size = newValue.length();
                ArrayList newList = new ArrayList();
                for (int i = 0; i < size; i++) {
                    newList.add(this.toHashMap(newValue.getJSONObject(i)));
                }
                data.put(key, newList);
            } else {
                data.put(key, value);
            }
        }
        return data;
    }

    public Map<String, Object> getActiveInfoForSC(String sizeType) {
        //查询广告信息
        String adId = "SC";
        List<AppAdPhoto> imglist = appAdPhotoRepository.findAllByadIdAndSizeType(adId, sizeType);
        //从 活动商品明细表（app_ad_event_goods）表中查询活动商品明细
        List<AppAdEventGoods> list = appAdEventGoodsRepository.findByAdId(adId);
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("imglist", imglist);
        List spList = new ArrayList<>();
        //从商品管理里面调商品详情
        for (AppAdEventGoods eventGoods : list) {
            HashMap<String, Object> spMap = new HashMap<String, Object>();//封装每个商品的信息
            String goodsCode = eventGoods.getGoodsCode();
            spMap.put("remark", eventGoods.getRemark());
            spMap.put("createTime", eventGoods.getCreateTime());
            spMap.put("goodsCode", goodsCode);
            spMap.put("salerCode", eventGoods.getSalerCode());
            spMap.put("tnrOpt", eventGoods.getTnrOpt()==null?"0":eventGoods.getTnrOpt().toString());
            spMap.put("repayTnrAmt", eventGoods.getRepayTnrAmt()==null?"0":eventGoods.getRepayTnrAmt().toString());
            if (StringUtils.isEmpty(goodsCode)) {//若商品编码不存在，则不封装返回了
                continue;
            }
            String url = EurekaServer.GM + "/pub/gm/getGoodsByCode?goodsCode=" + goodsCode;
            logger.info("GM 1.10接口请求地址：" + url);
            String json = HttpUtil.restGet(url, super.getToken());
            logger.info("GM 1.10接口返回：" + json);
            if (StringUtils.isEmpty(json)) {
                logger.info("GM 1.10接口【商品查询】请求获得结果为空！");
                return RestUtil.fail("A4010", "商品编码为" + goodsCode + "的商品在商品管理系统中查询失败！");
            }
            logger.info(json);
            //  logger.info(json.replaceAll("\"",null));
            Map<String, Object> jsonMap = HttpUtil.json2Map(json.replace("\"null\"","\"\"").replaceAll("null", "\"\""));
            JSONObject headMap = (JSONObject) jsonMap.get("head");
            String flag = String.valueOf(headMap.get("retFlag"));
            String msg = String.valueOf(headMap.get("retMsg"));
            if (!"00000".equals(flag)) {
                return fail(flag, msg + "!商品代码：" + goodsCode);
            }
            JSONObject bodyMap = (JSONObject) jsonMap.get("body");
            // String bodyMapJson=bodyMap.toString().replaceAll(":","=");
            // logger.info("bodyMapJson"+bodyMapJson);
            //Map<String,Object> newBodyMap= HttpUtil.json2Map(this.toHashMap(bodyMap));
            logger.info("bodyMap:" + bodyMap);
            spMap.put("goodsInfo", toHashMap(bodyMap));
            spList.add(spMap);

        }
        resultMap.put("list", spList);
        return success(resultMap);
    }
}
